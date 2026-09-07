package com.sap.flow;

import com.sap.TestSupport;
import com.sap.integration.service.SapBusinessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PpFlowTest extends TestSupport {
    @Autowired
    private SapBusinessService service;

    @Test
    void productionOrderIssueAndReceipt() {
        Map<String, Object> order = service.createProductionOrder(new HashMap<String, Object>() {{
            put("matnr", "F2001"); put("werks", "1000"); put("targetQty", 1);
        }});
        String id = String.valueOf(order.get("aufnr"));
        assertEquals("REL", service.releaseProduction(id).get("status"));
        assertEquals("REL", service.issueProductionOrder(id, new HashMap<String, Object>()).get("status"));
        assertNotNull(service.receiptProduction(id, Collections.singletonMap("qty", 1)).get("mblnr"));
    }
}
