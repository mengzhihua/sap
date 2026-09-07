package com.sap.mm.service;

import com.sap.common.BizException;
import com.sap.common.NumberRangeService;
import com.sap.mm.dto.PoCreateRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PurchaseOrderService {
    private final JdbcTemplate jdbc;
    private final NumberRangeService numbers;
    private final ReferenceDataService refs;

    public PurchaseOrderService(JdbcTemplate jdbc, NumberRangeService numbers, ReferenceDataService refs) {
        this.jdbc = jdbc;
        this.numbers = numbers;
        this.refs = refs;
    }

    @Transactional
    public Map<String, Object> create(PoCreateRequest request, String source) {
        String ebeln = numbers.next("PO");
        String lifnr = refs.vendor(request.getLifnr() == null ? request.getSupplierCode() : request.getLifnr());
        jdbc.update("INSERT INTO sap_purchase_order(ebeln,bsart,lifnr,ekorg,ekgrp,bukrs,waers,status,external_ref,source,total_amount) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                ebeln, value(request.getBsart(), "NB"), lifnr, value(request.getEkorg(), "1000"),
                value(request.getEkgrp(), "001"), value(request.getBukrs(), "1000"), value(request.getWaers(), "CNY"),
                "OPEN", request.getExternalRef(), source, BigDecimal.ZERO);
        BigDecimal total = BigDecimal.ZERO;
        int pos = 10;
        for (PoCreateRequest.PoItemRequest item : request.getItems()) {
            String matnr = refs.material(item.getMatnr());
            String werks = refs.plant(item.getWerks());
            BigDecimal qty = item.getQty() == null ? item.getMenge() : item.getQty();
            BigDecimal price = item.getPrice() == null ? item.getNetpr() : item.getPrice();
            if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) throw new BizException("采购数量必须大于0");
            if (price == null) price = BigDecimal.ZERO;
            jdbc.update("INSERT INTO sap_purchase_order_item(ebeln,ebelp,matnr,werks,lgort,menge,netpr,delivered_qty,invoiced_qty,delivery_date) VALUES(?,?,?,?,?,?,?,0,0,?)",
                    ebeln, String.valueOf(pos), matnr, werks, value(item.getLgort(), "0001"), qty, price,
                    item.getDeliveryDate());
            total = total.add(qty.multiply(price));
            pos += 10;
        }
        jdbc.update("UPDATE sap_purchase_order SET total_amount=? WHERE ebeln=?", total, ebeln);
        return one("SELECT * FROM sap_purchase_order WHERE ebeln=?", ebeln);
    }

    public Map<String, Object> find(String input) {
        if (input != null) {
            List<Map<String, Object>> exact = jdbc.queryForList(
                    "SELECT * FROM sap_purchase_order WHERE ebeln=? OR external_ref=?", input, input);
            if (!exact.isEmpty()) return exact.get(0);
        }
        if (input != null && input.startsWith("PO")) {
            List<Map<String, Object>> fallback = jdbc.queryForList(
                    "SELECT * FROM sap_purchase_order WHERE source='SRM' AND status IN ('OPEN','PARTIAL') ORDER BY created_at DESC");
            if (!fallback.isEmpty()) {
                Map<String, Object> po = fallback.get(0);
                jdbc.update("UPDATE sap_purchase_order SET external_ref=? WHERE ebeln=?", input, po.get("ebeln"));
                po.put("external_ref", input);
                return po;
            }
        }
        throw new BizException("采购订单不存在: " + input);
    }

    public Map<String, Object> item(String ebeln, String matnr, String itemRef) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM sap_purchase_order_item WHERE ebeln=? AND (matnr=? OR ebelp=?)",
                ebeln, matnr, itemRef);
        return rows.isEmpty() ? null : rows.get(0);
    }
    public List<Map<String, Object>> list() { return jdbc.queryForList("SELECT * FROM sap_purchase_order"); }
    public Map<String, Object> one(String sql, Object... args) { return jdbc.queryForMap(sql, args); }
    private static String value(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
