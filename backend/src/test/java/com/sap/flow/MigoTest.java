package com.sap.flow;

import com.sap.TestSupport;
import com.sap.mm.dto.MigoRequest;
import com.sap.mm.entity.MaterialDocument;
import com.sap.mm.service.GoodsMovementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class MigoTest extends TestSupport {
    @Autowired private GoodsMovementService goods;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void issueToCostCenterWritesOneFiAndCoDocument() {
        BigDecimal before = jdbc.queryForObject("SELECT unrestricted_qty FROM sap_stock WHERE matnr='M1001' AND werks='1000' AND lgort='0001'", BigDecimal.class);
        MigoRequest request = new MigoRequest();
        request.setBwart("201");
        MigoRequest.MigoItemRequest item = new MigoRequest.MigoItemRequest();
        item.setMatnr("SKU001"); item.setWerks("1000"); item.setLgort("0001");
        item.setQty(new BigDecimal("2")); item.setKostl("CC1000");
        request.setItems(Collections.singletonList(item));
        MaterialDocument document = goods.post(request);
        BigDecimal after = jdbc.queryForObject("SELECT unrestricted_qty FROM sap_stock WHERE matnr='M1001' AND werks='1000' AND lgort='0001'", BigDecimal.class);
        assertEquals(0, before.subtract(after).compareTo(new BigDecimal("2")));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM sap_acc_document WHERE belnr=?", Integer.class, document.getFiBelnr()));
        assertEquals(0, jdbc.queryForObject("SELECT SUM(CASE WHEN shkzg='S' THEN amount ELSE -amount END) FROM sap_acc_document_item WHERE belnr=?", BigDecimal.class, document.getFiBelnr()).compareTo(BigDecimal.ZERO));
        assertTrue(jdbc.queryForObject("SELECT COUNT(*) FROM sap_co_document WHERE fi_belnr=?", Integer.class, document.getFiBelnr()) > 0);
        assertFalse(document.getItems().isEmpty());
    }
}
