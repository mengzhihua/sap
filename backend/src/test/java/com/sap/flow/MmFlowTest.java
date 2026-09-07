package com.sap.flow;

import com.sap.TestSupport;
import com.sap.integration.service.SapBusinessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MmFlowTest extends TestSupport {
    @Autowired
    private SapBusinessService service;

    @Test
    void poGoodsReceiptAndMiroCreateDocuments() {
        Map<String, Object> line = new HashMap<>();
        line.put("matnr", "SKU001");
        line.put("werks", "P001");
        line.put("qty", 10);
        line.put("price", 45);
        Map<String, Object> body = new HashMap<>();
        body.put("supplierCode", "SUP01");
        body.put("items", Collections.singletonList(line));
        Map<String, Object> po = service.createPo(body, "MANUAL");
        Map<String, Object> gr = new HashMap<>();
        gr.put("refType", "PO");
        gr.put("refNo", po.get("ebeln"));
        gr.put("items", Collections.singletonList(new HashMap<String, Object>() {{
            put("matnr", "SKU001"); put("qty", 10);
        }}));
        assertTrue(String.valueOf(service.postMigo(gr).get("mblnr")).startsWith("50"));
        Map<String, Object> invoice = new HashMap<>();
        invoice.put("ebeln", po.get("ebeln"));
        invoice.put("lifnr", "SUP01");
        invoice.put("items", Collections.singletonList(new HashMap<String, Object>() {{
            put("ebelp", "10"); put("qty", 10); put("price", 45);
        }}));
        assertEquals("POSTED", service.postMiro(invoice).get("status"));
    }

    @Test
    void miroQuantityMismatchIsBlocked() {
        Map<String, Object> line = new HashMap<>();
        line.put("matnr", "SKU002"); line.put("werks", "1000"); line.put("qty", 2); line.put("price", 12);
        Map<String, Object> po = service.createPo(new HashMap<String, Object>() {{
            put("supplierCode", "SUP02"); put("items", Collections.singletonList(line));
        }}, "MANUAL");
        Map<String, Object> invoice = new HashMap<>();
        invoice.put("ebeln", po.get("ebeln")); invoice.put("lifnr", "SUP02");
        invoice.put("items", Collections.singletonList(new HashMap<String, Object>() {{
            put("ebelp", "10"); put("qty", 2); put("price", 12);
        }}));
        assertEquals("BLOCKED", service.postMiro(invoice).get("status"));
    }
}
