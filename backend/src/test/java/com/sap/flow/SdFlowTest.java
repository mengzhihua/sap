package com.sap.flow;

import com.sap.TestSupport;
import com.sap.integration.service.SapBusinessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SdFlowTest extends TestSupport {
    @Autowired
    private SapBusinessService service;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void salesDeliveryPgiBillingAreBalancedByConstruction() {
        Map<String, Object> item = new HashMap<>();
        item.put("matnr", "F2001"); item.put("qty", 1); item.put("werks", "1000");
        Map<String, Object> soBody = new HashMap<>();
        soBody.put("customerCode", "CUST-001");
        soBody.put("items", Collections.singletonList(item));
        Map<String, Object> so = service.createSo(soBody);
        Map<String, Object> dn = service.createDn(Collections.singletonMap("soVbeln", so.get("vbeln")));
        service.pickDn(String.valueOf(dn.get("vbeln")));
        Map<String, Object> pgi = service.pgi(String.valueOf(dn.get("vbeln")));
        assertEquals("PGI", pgi.get("status"));
        Map<String, Object> billing = service.bill(Collections.singletonMap("dnVbeln", dn.get("vbeln")));
        assertEquals("POSTED", billing.get("status"));
        assertTrue(jdbc.queryForObject("SELECT COUNT(*) FROM sap_integration_log WHERE direction='OUT' AND system_name='BMS'",
                Integer.class) > 0);
    }
}
