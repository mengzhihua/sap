package com.sap.flow;

import com.sap.TestSupport;
import com.sap.common.BizException;
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
        com.sap.sd.entity.SalesOrder so = sales.create(soBody);
        DnCreateRequest dnRequest = new DnCreateRequest(); dnRequest.setSoVbeln(so.getVbeln());
        com.sap.sd.entity.Delivery dn = deliveries.create(dnRequest);
        deliveries.pick(dn.getVbeln());
        com.sap.sd.entity.Delivery pgi = deliveries.pgi(dn.getVbeln());
        assertEquals("PGI", pgi.getStatus());
        BigDecimal stockAfterPgi = jdbc.queryForObject(
                "SELECT unrestricted_qty FROM sap_stock WHERE matnr='F2001' AND werks='1000' AND lgort='0002'",
                BigDecimal.class);
        int materialDocsAfterPgi = jdbc.queryForObject("SELECT COUNT(*) FROM sap_material_document WHERE ref_no=?",
                Integer.class, dn.getVbeln());
        assertThrows(BizException.class, () -> deliveries.pgi(dn.getVbeln()));
        assertEquals(0, stockAfterPgi.compareTo(jdbc.queryForObject(
                "SELECT unrestricted_qty FROM sap_stock WHERE matnr='F2001' AND werks='1000' AND lgort='0002'",
                BigDecimal.class)));
        assertEquals(materialDocsAfterPgi, jdbc.queryForObject(
                "SELECT COUNT(*) FROM sap_material_document WHERE ref_no=?", Integer.class, dn.getVbeln()));

        com.sap.sd.entity.Delivery open = deliveries.create(dnRequest);
        assertThrows(BizException.class, () -> deliveries.pgi(open.getVbeln()));
        BillingRequest billingRequest = new BillingRequest(); billingRequest.setDnVbeln(dn.getVbeln());
        com.sap.sd.entity.BillingDoc billing = this.billing.create(billingRequest);
        assertEquals("POSTED", billing.getStatus());
        assertTrue(jdbc.queryForObject("SELECT COUNT(*) FROM sap_integration_log WHERE direction='OUT' AND system_name='BMS'",
                Integer.class) > 0);
    }
}
