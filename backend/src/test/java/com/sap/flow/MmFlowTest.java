package com.sap.flow;

import com.sap.TestSupport;
import com.sap.mm.dto.*;
import com.sap.mm.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class MmFlowTest extends TestSupport {
    @Autowired private PurchaseOrderService purchaseOrders;
    @Autowired private GoodsMovementService goods;
    @Autowired private InvoiceVerificationService invoices;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void poGoodsReceiptAndMiroCreateDocuments() {
        PoCreateRequest body = new PoCreateRequest(); body.setSupplierCode("SUP01");
        PoCreateRequest.PoItemRequest line = new PoCreateRequest.PoItemRequest();
        line.setMatnr("SKU001"); line.setWerks("P001"); line.setQty(new BigDecimal("10")); line.setPrice(new BigDecimal("45"));
        body.setItems(Collections.singletonList(line));
        com.sap.mm.entity.PurchaseOrder po = purchaseOrders.create(body, "MANUAL");
        MigoRequest gr = new MigoRequest(); gr.setRefType("PO"); gr.setRefNo(po.getEbeln());
        MigoRequest.MigoItemRequest grLine = new MigoRequest.MigoItemRequest(); grLine.setMatnr("SKU001"); grLine.setQty(new BigDecimal("10"));
        gr.setItems(Collections.singletonList(grLine));
        com.sap.mm.entity.MaterialDocument materialDocument = goods.postMigo(gr);
        assertTrue(materialDocument.getMblnr().startsWith("50"));
        assertFalse(materialDocument.getItems().isEmpty());
        assertTrue(jdbc.queryForObject("SELECT COUNT(*) FROM sap_gr_ir WHERE ebeln=? AND gr_qty > 0",
                Integer.class, po.getEbeln()) > 0);
        assertTrue(jdbc.queryForObject("SELECT unrestricted_qty FROM sap_stock WHERE matnr='M1001' AND werks='1000' AND lgort='0001'",
                BigDecimal.class).compareTo(BigDecimal.ZERO) > 0);
        MiroRequest invoice = new MiroRequest(); invoice.setEbeln(po.getEbeln()); invoice.setLifnr("SUP01");
        MiroRequest.MiroItemRequest invoiceLine = new MiroRequest.MiroItemRequest();
        invoiceLine.setEbelp("10"); invoiceLine.setQty(new BigDecimal("10")); invoiceLine.setPrice(new BigDecimal("45"));
        invoice.setItems(Collections.singletonList(invoiceLine));
        assertEquals("POSTED", invoices.post(invoice).getStatus());
    }

    @Test
    void miroQuantityMismatchIsBlockedWithoutFiDocument() {
        PoCreateRequest body = new PoCreateRequest(); body.setSupplierCode("SUP02");
        PoCreateRequest.PoItemRequest line = new PoCreateRequest.PoItemRequest();
        line.setMatnr("SKU002"); line.setWerks("1000"); line.setQty(new BigDecimal("2")); line.setPrice(new BigDecimal("12"));
        body.setItems(Collections.singletonList(line));
        com.sap.mm.entity.PurchaseOrder po = purchaseOrders.create(body, "MANUAL");
        MiroRequest invoice = new MiroRequest(); invoice.setEbeln(po.getEbeln()); invoice.setLifnr("SUP02");
        MiroRequest.MiroItemRequest invoiceLine = new MiroRequest.MiroItemRequest();
        invoiceLine.setEbelp("10"); invoiceLine.setQty(new BigDecimal("2")); invoiceLine.setPrice(new BigDecimal("12"));
        invoice.setItems(Collections.singletonList(invoiceLine));
        com.sap.mm.entity.SupplierInvoice saved = invoices.post(invoice);
        assertEquals("BLOCKED", saved.getStatus());
        assertNull(saved.getFiBelnr());
    }
}
