package com.sap.flow;

import com.sap.TestSupport;
import com.sap.sd.dto.*;
import com.sap.sd.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class SdFlowTest extends TestSupport {
    @Autowired private SalesOrderService sales;
    @Autowired private DeliveryService deliveries;
    @Autowired private BillingService billing;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void salesDeliveryPgiBillingAreBalancedByConstruction() {
        SoCreateRequest soBody = new SoCreateRequest(); soBody.setCustomerCode("CUST-001");
        SoCreateRequest.Item item = new SoCreateRequest.Item(); item.setMatnr("F2001"); item.setQty(new BigDecimal("1")); item.setWerks("1000");
        soBody.setItems(Collections.singletonList(item));
        Map<String, Object> so = sales.create(soBody);
        DnCreateRequest dnRequest = new DnCreateRequest(); dnRequest.setSoVbeln(String.valueOf(so.get("vbeln")));
        Map<String, Object> dn = deliveries.create(dnRequest);
        deliveries.pick(String.valueOf(dn.get("vbeln")));
        Map<String, Object> pgi = deliveries.pgi(String.valueOf(dn.get("vbeln")));
        assertEquals("PGI", pgi.get("status"));
        BillingRequest billingRequest = new BillingRequest(); billingRequest.setDnVbeln(String.valueOf(dn.get("vbeln")));
        Map<String, Object> billing = this.billing.create(billingRequest);
        assertEquals("POSTED", billing.get("status"));
        assertTrue(jdbc.queryForObject("SELECT COUNT(*) FROM sap_integration_log WHERE direction='OUT' AND system_name='BMS'",
                Integer.class) > 0);
    }
}
