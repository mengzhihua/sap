package com.sap.flow;

import com.sap.TestSupport;
import com.sap.pp.dto.ProductionOrderRequest;
import com.sap.pp.service.ProductionOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PpFlowTest extends TestSupport {
    @Autowired
    private ProductionOrderService service;

    @Test
    void productionOrderIssueAndReceipt() {
        ProductionOrderRequest request = new ProductionOrderRequest();
        request.setMatnr("F2001"); request.setWerks("1000"); request.setTargetQty(new java.math.BigDecimal("1"));
        com.sap.pp.entity.ProductionOrder order = service.create(request);
        String id = order.getAufnr();
        assertEquals("REL", service.release(id).getStatus());
        assertEquals("REL", service.issue(id, "CC1000").getStatus());
        assertNotNull(service.receipt(id, new java.math.BigDecimal("1")).getMblnr());
    }
}
