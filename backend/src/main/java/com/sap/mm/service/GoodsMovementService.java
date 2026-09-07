package com.sap.mm.service;

import com.sap.common.BizException;
import com.sap.common.NumberRangeService;
import com.sap.fi.service.AccountingDocumentService;
import com.sap.integration.service.BmsPushService;
import com.sap.mm.dto.MigoRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class GoodsMovementService {
    private final JdbcTemplate jdbc;
    private final NumberRangeService numbers;
    private final ReferenceDataService refs;
    private final PurchaseOrderService purchaseOrders;
    private final StockService stock;
    private final AccountingDocumentService accounting;
    private final BmsPushService bms;

    public GoodsMovementService(JdbcTemplate jdbc, NumberRangeService numbers, ReferenceDataService refs,
                                PurchaseOrderService purchaseOrders, StockService stock,
                                AccountingDocumentService accounting, BmsPushService bms) {
        this.jdbc = jdbc; this.numbers = numbers; this.refs = refs; this.purchaseOrders = purchaseOrders;
        this.stock = stock; this.accounting = accounting; this.bms = bms;
    }

    @Transactional
    public Map<String, Object> post(MigoRequest request) {
        String bwart = request.getBwart() == null ? "101" : request.getBwart();
        if ("101".equals(bwart) && ("PO".equalsIgnoreCase(request.getRefType()) || request.getRefNo() != null)) {
            return receivePo(request);
        }
        if ("201".equals(bwart)) return issueCostCenter(request);
        if ("311".equals(bwart)) return transfer(request);
        throw new BizException("不支持的移动类型: " + bwart);
    }

    public Map<String, Object> postMigo(MigoRequest request) {
        return post(request);
    }

    @Transactional
    public Map<String, Object> receivePo(MigoRequest request) {
        MigoRequest.MigoItemRequest first = request.getItems().get(0);
        Map<String, Object> po = purchaseOrders.find(request.getRefNo());
        String mblnr = numbers.next("MATERIAL");
        BigDecimal total = BigDecimal.ZERO;
        int lineNo = 1;
        for (MigoRequest.MigoItemRequest item : request.getItems()) {
            String matnr = refs.material(item.getMatnr());
            Map<String, Object> poItem = purchaseOrders.item(String.valueOf(po.get("ebeln")), matnr, item.getEbelp());
            if (poItem == null) throw new BizException("物料不在采购订单中: " + matnr);
            BigDecimal qty = item.getQty() == null ? item.getQuantityInBaseUnit() : item.getQty();
            BigDecimal open = StockService.decimal(poItem, "menge").subtract(StockService.decimal(poItem, "delivered_qty"));
            if (qty.compareTo(open) > 0) throw new BizException("收货数量超过未收数量");
            BigDecimal amount = qty.multiply(StockService.decimal(poItem, "netpr"));
            stock.change(matnr, String.valueOf(poItem.get("werks")), String.valueOf(poItem.get("lgort")), qty, amount);
            jdbc.update("UPDATE sap_purchase_order_item SET delivered_qty=delivered_qty+? WHERE id=?", qty, poItem.get("id"));
            upsertGrIr(String.valueOf(po.get("ebeln")), String.valueOf(poItem.get("ebelp")), qty, BigDecimal.ZERO, amount, BigDecimal.ZERO);
            jdbc.update("INSERT INTO sap_material_document_item(mblnr,zeile,matnr,werks,lgort,menge,amount,bwart,ebeln,ebelp) VALUES(?,?,?,?,?,?,?,?,?,?)",
                    mblnr, String.valueOf(lineNo++), matnr, poItem.get("werks"), poItem.get("lgort"), qty, amount, "101", po.get("ebeln"), poItem.get("ebelp"));
            total = total.add(amount);
        }
        String fi = accounting.post("WE", "MM", String.valueOf(po.get("ebeln")), Arrays.asList(
                new AccountingDocumentService.FiLine(refs.stockAccount(refs.material(first.getMatnr())), "S", total, null, null, null, "PO收货"),
                new AccountingDocumentService.FiLine("2202", "H", total, null, String.valueOf(po.get("lifnr")), null, "GR/IR")));
        jdbc.update("INSERT INTO sap_material_document(mblnr,mjahr,budat,bwart,ref_type,ref_no,fi_belnr) VALUES(?,?,CURRENT_DATE,'101','PO',?,?)",
                mblnr, String.valueOf(LocalDate.now().getYear()), po.get("ebeln"), fi);
        jdbc.update("UPDATE sap_purchase_order SET status=CASE WHEN EXISTS(SELECT 1 FROM sap_purchase_order_item WHERE ebeln=? AND delivered_qty<menge) THEN 'PARTIAL' ELSE 'COMPLETED' END WHERE ebeln=?",
                po.get("ebeln"), po.get("ebeln"));
        Map<String, Object> result = jdbc.queryForMap("SELECT * FROM sap_material_document WHERE mblnr=?", mblnr);
        bms.pushInbound(String.valueOf(po.get("ebeln")), total);
        return result;
    }

    @Transactional
    public Map<String, Object> issueCostCenter(MigoRequest request) {
        for (MigoRequest.MigoItemRequest item : request.getItems()) {
            String mat = refs.material(item.getMatnr());
            BigDecimal qty = item.getQty() == null ? item.getQuantityInBaseUnit() : item.getQty();
            BigDecimal price = jdbc.queryForObject("SELECT std_price FROM sap_material WHERE matnr=?", BigDecimal.class, mat);
            stock.change(mat, value(item.getWerks(), "1000"), value(item.getLgort(), "0001"), qty.negate(), qty.multiply(price).negate());
            accounting.post("WE", "MM", value(request.getRefNo(), "CC"), Arrays.asList(
                    new AccountingDocumentService.FiLine("6401", "S", qty.multiply(price), item.getKostl(), null, null, "发料"),
                    new AccountingDocumentService.FiLine(refs.stockAccount(mat), "H", qty.multiply(price), null, null, null, "库存减少")));
        }
        return materialDocument(request, "201", "MANUAL");
    }

    @Transactional
    public Map<String, Object> transfer(MigoRequest request) {
        for (MigoRequest.MigoItemRequest item : request.getItems()) {
            String mat = refs.material(item.getMatnr());
            BigDecimal qty = item.getQty() == null ? item.getQuantityInBaseUnit() : item.getQty();
            String werks = value(item.getWerks(), "1000");
            stock.change(mat, werks, value(item.getLgort(), "0001"), qty.negate(), BigDecimal.ZERO);
            stock.change(mat, werks, value(item.getToLgort(), "0002"), qty, BigDecimal.ZERO);
        }
        return materialDocument(request, "311", "MANUAL");
    }

    private Map<String, Object> materialDocument(MigoRequest request, String bwart, String refType) {
        String mblnr = numbers.next("MATERIAL");
        jdbc.update("INSERT INTO sap_material_document(mblnr,mjahr,budat,bwart,ref_type,ref_no) VALUES(?,?,CURRENT_DATE,?,?,?)",
                mblnr, String.valueOf(LocalDate.now().getYear()), bwart, refType, request.getRefNo());
        return jdbc.queryForMap("SELECT * FROM sap_material_document WHERE mblnr=?", mblnr);
    }

    private void upsertGrIr(String ebeln, String ebelp, BigDecimal grQty, BigDecimal irQty,
                            BigDecimal grAmount, BigDecimal irAmount) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM sap_gr_ir WHERE ebeln=? AND ebelp=?", ebeln, ebelp);
        if (rows.isEmpty()) jdbc.update("INSERT INTO sap_gr_ir(ebeln,ebelp,gr_qty,ir_qty,gr_amount,ir_amount) VALUES(?,?,?,?,?,?)",
                ebeln, ebelp, grQty, irQty, grAmount, irAmount);
        else jdbc.update("UPDATE sap_gr_ir SET gr_qty=gr_qty+?,ir_qty=ir_qty+?,gr_amount=gr_amount+?,ir_amount=ir_amount+? WHERE ebeln=? AND ebelp=?",
                grQty, irQty, grAmount, irAmount, ebeln, ebelp);
    }
    private static String value(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
