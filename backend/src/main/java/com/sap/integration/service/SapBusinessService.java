package com.sap.integration.service;

import com.sap.common.BizException;
import com.sap.common.NumberRangeService;
import com.sap.integration.client.BmsClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class SapBusinessService {
    private final JdbcTemplate jdbc;
    private final NumberRangeService numbers;
    private final IntegrationLogService logs;
    private final BmsClient bms;

    public SapBusinessService(JdbcTemplate jdbc, NumberRangeService numbers,
                              IntegrationLogService logs, BmsClient bms) {
        this.jdbc = jdbc;
        this.numbers = numbers;
        this.logs = logs;
        this.bms = bms;
    }

    public List<Map<String, Object>> list(String table) {
        return jdbc.queryForList("SELECT * FROM " + table);
    }

    public List<Map<String, Object>> listMaterials(String q) {
        if (q == null || q.trim().isEmpty()) return jdbc.queryForList("SELECT * FROM sap_material ORDER BY matnr");
        String x = "%" + q.trim() + "%";
        return jdbc.queryForList("SELECT * FROM sap_material WHERE matnr LIKE ? OR alias_code LIKE ? OR maktx LIKE ?",
                x, x, x);
    }

    public List<Map<String, Object>> listVendors(String q) {
        if (q == null || q.trim().isEmpty()) return jdbc.queryForList("SELECT * FROM sap_vendor ORDER BY lifnr");
        String x = "%" + q.trim() + "%";
        return jdbc.queryForList("SELECT * FROM sap_vendor WHERE lifnr LIKE ? OR alias_code LIKE ? OR name LIKE ?", x, x, x);
    }

    @Transactional
    public Map<String, Object> saveMaster(String table, String key, Map<String, Object> body) {
        if ("sap_material".equals(table)) {
            String matnr = text(body, "matnr");
            require(matnr, "matnr不能为空");
            jdbc.update("MERGE INTO sap_material(matnr,maktx,meins,mtart,matkl,std_price,price_control,alias_code) KEY(matnr) VALUES(?,?,?,?,?,?,?,?)",
                    matnr, text(body, "maktx"), textOr(body, "meins", "EA"), textOr(body, "mtart", "ROH"),
                    text(body, "matkl"), bd(body, "stdPrice"), textOr(body, "priceControl", "S"), textOr(body, "aliasCode", matnr));
        } else if ("sap_vendor".equals(table)) {
            String lifnr = text(body, "lifnr");
            require(lifnr, "lifnr不能为空");
            jdbc.update("MERGE INTO sap_vendor(lifnr,name,alias_code,country,payment_term,recon_account) KEY(lifnr) VALUES(?,?,?,?,?,?)",
                    lifnr, text(body, "name"), textOr(body, "aliasCode", lifnr), text(body, "country"),
                    text(body, "paymentTerm"), textOr(body, "reconAccount", "2201"));
        } else if ("sap_customer".equals(table)) {
            String kunnr = text(body, "kunnr");
            require(kunnr, "kunnr不能为空");
            jdbc.update("MERGE INTO sap_customer(kunnr,name,alias_code,recon_account) KEY(kunnr) VALUES(?,?,?,?)",
                    kunnr, text(body, "name"), textOr(body, "aliasCode", kunnr), textOr(body, "reconAccount", "1122"));
        } else if ("sap_gl_account".equals(table)) {
            String saknr = text(body, "saknr");
            require(saknr, "saknr不能为空");
            jdbc.update("MERGE INTO sap_gl_account(saknr,txt,type,recon_type) KEY(saknr) VALUES(?,?,?,?)",
                    saknr, text(body, "txt"), text(body, "type"), text(body, "reconType"));
        } else if ("sap_cost_center".equals(table)) {
            String kostl = text(body, "kostl");
            require(kostl, "kostl不能为空");
            jdbc.update("MERGE INTO sap_cost_center(kostl,name,bukrs,responsible) KEY(kostl) VALUES(?,?,?,?)",
                    kostl, text(body, "name"), textOr(body, "bukrs", "1000"), text(body, "responsible"));
        }
        return jdbc.queryForMap("SELECT * FROM " + table + " WHERE " + key + "=?", keyValue(table, body));
    }

    @Transactional
    public Map<String, Object> createPo(Map<String, Object> body, String source) {
        String ebeln = numbers.next("PO");
        String lifnr = resolveVendor(firstNonBlank(text(body, "lifnr"), text(body, "supplierCode"),
                text(body, "supplier"), text(body, "Supplier")));
        String ekorg = textOr(body, "ekorg", "1000");
        String ekgrp = textOr(body, "ekgrp", "001");
        String bukrs = textOr(body, "bukrs", "1000");
        String waers = textOr(body, "waers", textOr(body, "documentCurrency", "CNY"));
        List<Map<String, Object>> lines = objectList(body.get("items"));
        if (lines.isEmpty()) lines = objectList(body.get("to_PurchaseOrderItem"));
        if (lines.isEmpty()) throw new BizException("采购订单至少需要一行");
        BigDecimal total = BigDecimal.ZERO;
        jdbc.update("INSERT INTO sap_purchase_order(ebeln,bsart,lifnr,ekorg,ekgrp,bukrs,waers,status,external_ref,source,total_amount)"
                        + " VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                ebeln, textOr(body, "bsart", "NB"), lifnr, ekorg, ekgrp, bukrs, waers,
                "OPEN", firstNonBlank(text(body, "externalRef"), text(body, "purchaseOrderExternalRef"), text(body, "YY1_ExternalRef")),
                source, BigDecimal.ZERO);
        int pos = 10;
        for (Map<String, Object> line : lines) {
            String matnr = resolveMaterial(textOr(line, "matnr", text(line, "Material")));
            BigDecimal qty = firstAmount(line, "menge", "OrderQuantity", "qty", "QuantityInBaseUnit");
            BigDecimal price = firstAmount(line, "netpr", "NetPriceAmount", "price", "PurchaseOrderItemPrice");
            if (qty.compareTo(BigDecimal.ZERO) <= 0) throw new BizException("采购数量必须大于0");
            String werks = textOr(line, "werks", textOr(body, "werks", text(body, "plant")));
            werks = resolvePlant(werks == null ? "1000" : werks);
            String lgort = textOr(line, "lgort", "0001");
            String date = textOr(line, "deliveryDate", text(line, "ScheduleLineDeliveryDate"));
            jdbc.update("INSERT INTO sap_purchase_order_item(ebeln,ebelp,matnr,werks,lgort,menge,netpr,delivered_qty,invoiced_qty,delivery_date)"
                            + " VALUES(?,?,?,?,?,?,?,0,0,?)",
                    ebeln, String.valueOf(pos), matnr, werks, lgort, qty, price, date == null ? null : date.substring(0, 10));
            total = total.add(qty.multiply(price));
            pos += 10;
        }
        jdbc.update("UPDATE sap_purchase_order SET total_amount=? WHERE ebeln=?", total, ebeln);
        return jdbc.queryForMap("SELECT * FROM sap_purchase_order WHERE ebeln=?", ebeln);
    }

    @Transactional
    public Map<String, Object> postMigo(Map<String, Object> body) {
        String bwart = textOr(body, "bwart", textOr(body, "GoodsMovementType", "101"));
        if ("101".equals(bwart) && ("PO".equalsIgnoreCase(textOr(body, "refType", ""))
                || body.containsKey("to_MaterialDocumentItem") || body.containsKey("GoodsMovementCode"))) {
            return receivePo(body);
        }
        if ("201".equals(bwart)) return issueCostCenter(body);
        if ("311".equals(bwart)) return transfer(body);
        if ("261".equals(bwart)) return issueProduction(body);
        if ("601".equals(bwart)) return issueDelivery(body);
        throw new BizException("不支持的移动类型: " + bwart);
    }

    @Transactional
    public Map<String, Object> receivePo(Map<String, Object> body) {
        List<Map<String, Object>> items = objectList(body.get("items"));
        if (items.isEmpty()) items = objectList(body.get("to_MaterialDocumentItem"));
        Map<String, Object> first = items.isEmpty() ? Collections.<String, Object>emptyMap() : items.get(0);
        String poInput = firstNonBlank(text(body, "refNo"), text(body, "PurchaseOrder"),
                text(first, "PurchaseOrder"), text(first, "ebeln"));
        Map<String, Object> po = findPo(poInput);
        String matnr = resolveMaterial(textOr(first, "matnr", text(first, "Material")));
        Map<String, Object> poItem = findPoItem(String.valueOf(po.get("ebeln")), matnr,
                textOr(first, "ebelp", text(first, "PurchaseOrderItem")));
        if (poItem == null) throw new BizException("采购订单行不存在");
        BigDecimal totalQty = BigDecimal.ZERO;
        String mblnr = numbers.next("MATERIAL");
        for (Map<String, Object> item : items) {
            String material = resolveMaterial(textOr(item, "matnr", text(item, "Material")));
            Map<String, Object> line = findPoItem(String.valueOf(po.get("ebeln")), material,
                    textOr(item, "ebelp", text(item, "PurchaseOrderItem")));
            if (line == null) throw new BizException("物料不在采购订单中: " + material);
            BigDecimal qty = bdOr(item, "qty", bd(item, "QuantityInBaseUnit"));
            BigDecimal open = bd(line, "menge").subtract(bd(line, "delivered_qty"));
            if (qty.compareTo(open) > 0) throw new BizException("收货数量超过未收数量");
            BigDecimal amount = qty.multiply(bd(line, "netpr"));
            totalQty = totalQty.add(qty);
            String werks = String.valueOf(line.get("werks"));
            String lgort = String.valueOf(line.get("lgort"));
            changeStock(material, werks, lgort, qty, amount);
            jdbc.update("UPDATE sap_purchase_order_item SET delivered_qty=delivered_qty+? WHERE id=?", qty, line.get("id"));
            upsertGrIr(String.valueOf(po.get("ebeln")), String.valueOf(line.get("ebelp")), qty, BigDecimal.ZERO, amount, BigDecimal.ZERO);
            jdbc.update("INSERT INTO sap_material_document_item(mblnr,zeile,matnr,werks,lgort,menge,amount,bwart,ebeln,ebelp)"
                            + " VALUES(?,?,?,?,?,?,?,?,?,?)",
                    mblnr, String.valueOf(totalQty), material, werks, lgort, qty, amount, "101", po.get("ebeln"), line.get("ebelp"));
        }
        List<FiLine> fi = new ArrayList<>();
        fi.add(new FiLine(stockAccount(matnr), "S", totalAmount(mblnr), null, null, null, "PO收货"));
        fi.add(new FiLine("2202", "H", totalAmount(mblnr), null, String.valueOf(po.get("lifnr")), null, "GR/IR"));
        String fiNo = postFi("WE", "MM", String.valueOf(po.get("ebeln")), fi);
        jdbc.update("INSERT INTO sap_material_document(mblnr,mjahr,budat,bwart,ref_type,ref_no,fi_belnr) VALUES(?,?,CURRENT_DATE,?,?,?,?)",
                mblnr, String.valueOf(LocalDate.now().getYear()), "101", "PO", po.get("ebeln"), fiNo);
        jdbc.update("UPDATE sap_purchase_order SET status=CASE WHEN EXISTS(SELECT 1 FROM sap_purchase_order_item WHERE ebeln=? AND delivered_qty<menge) THEN 'PARTIAL' ELSE 'COMPLETED' END WHERE ebeln=?",
                po.get("ebeln"), po.get("ebeln"));
        Map<String, Object> result = jdbc.queryForMap("SELECT * FROM sap_material_document WHERE mblnr=?", mblnr);
        pushBms("/api/open/wms/docs", bmsInboundPayload(po, totalQty));
        return result;
    }

    @Transactional
    public Map<String, Object> postMiro(Map<String, Object> body) {
        String lifnr = resolveVendor(firstNonBlank(text(body, "lifnr"), text(body, "InvoicingParty"),
                text(body, "Supplier"), text(body, "supplierCode")));
        List<Map<String, Object>> items = objectList(body.get("items"));
        if (items.isEmpty()) items = objectList(body.get("to_SuplrInvcItemPurOrdRef"));
        Map<String, Object> first = items.isEmpty() ? Collections.<String, Object>emptyMap() : items.get(0);
        String ebeln = firstNonBlank(text(body, "ebeln"), text(body, "PurchaseOrder"),
                text(first, "PurchaseOrder"), text(first, "ebeln"));
        Map<String, Object> po = findPo(ebeln);
        BigDecimal gross = bdOr(body, "grossAmount", bd(body, "InvoiceGrossAmount"));
        if (gross.compareTo(BigDecimal.ZERO) <= 0) gross = bd(body, "amount");
        BigDecimal net = BigDecimal.ZERO;
        boolean blocked = false;
        for (Map<String, Object> item : items) {
            String ref = textOr(item, "ebelp", text(item, "PurchaseOrderItem"));
            Map<String, Object> pi = findPoItem(String.valueOf(po.get("ebeln")), resolveMaybeMaterial(ref), ref);
            if (pi == null) throw new BizException("发票采购订单行不存在: " + ref);
            BigDecimal qty = bdOr(item, "qty", bd(item, "QuantityInPurchaseOrderUnit"));
            BigDecimal price = bdOr(item, "price", bd(item, "PurchaseOrderItemPrice"));
            BigDecimal open = bd(pi, "delivered_qty").subtract(bd(pi, "invoiced_qty"));
            if (qty.compareTo(open) > 0) blocked = true;
            BigDecimal poPrice = bd(pi, "netpr");
            if (poPrice.compareTo(BigDecimal.ZERO) > 0
                    && price.subtract(poPrice).abs().divide(poPrice, 6, RoundingMode.HALF_UP).compareTo(new BigDecimal("0.05")) > 0) {
                blocked = true;
            }
            net = net.add(qty.multiply(price));
        }
        if (gross.compareTo(BigDecimal.ZERO) <= 0) gross = net.multiply(new BigDecimal("1.13")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = gross.subtract(net).max(BigDecimal.ZERO);
        String belnr = numbers.next("INVOICE");
        String status = blocked ? "BLOCKED" : "POSTED";
        String fiNo = null;
        if (!blocked) {
            List<FiLine> fi = new ArrayList<>();
            fi.add(new FiLine("2202", "S", net, null, null, null, "GR/IR"));
            if (tax.compareTo(BigDecimal.ZERO) > 0) fi.add(new FiLine("2211", "S", tax, null, null, null, "进项税"));
            fi.add(new FiLine("2201", "H", gross, null, lifnr, null, "应付账款"));
            fiNo = postFi("RE", "MM", belnr, fi);
            for (Map<String, Object> item : items) {
                String ref = textOr(item, "ebelp", text(item, "PurchaseOrderItem"));
                Map<String, Object> pi = findPoItem(String.valueOf(po.get("ebeln")), resolveMaybeMaterial(ref), ref);
                BigDecimal qty = bdOr(item, "qty", bd(item, "QuantityInPurchaseOrderUnit"));
                jdbc.update("UPDATE sap_purchase_order_item SET invoiced_qty=invoiced_qty+? WHERE id=?", qty, pi.get("id"));
                upsertGrIr(String.valueOf(po.get("ebeln")), String.valueOf(pi.get("ebelp")), BigDecimal.ZERO, qty, BigDecimal.ZERO, qty.multiply(bd(pi, "netpr")));
            }
        }
        jdbc.update("INSERT INTO sap_supplier_invoice(belnr,gjahr,lifnr,ebeln,external_invoice_no,gross_amount,tax_amount,status,fi_belnr,match_result) VALUES(?,?,?,?,?,?,?,?,?,?)",
                belnr, String.valueOf(LocalDate.now().getYear()), lifnr, po.get("ebeln"),
                textOr(body, "externalInvoiceNo", text(body, "SupplierInvoice")), gross, tax, status, fiNo,
                blocked ? "BLOCKED" : "PASSED");
        for (Map<String, Object> item : items) {
            jdbc.update("INSERT INTO sap_supplier_invoice_item(belnr,ebelp,qty,price) VALUES(?,?,?,?)",
                    belnr, textOr(item, "ebelp", text(item, "PurchaseOrderItem")),
                    bdOr(item, "qty", bd(item, "QuantityInPurchaseOrderUnit")),
                    bdOr(item, "price", bd(item, "PurchaseOrderItemPrice")));
        }
        return jdbc.queryForMap("SELECT * FROM sap_supplier_invoice WHERE belnr=?", belnr);
    }

    @Transactional
    public Map<String, Object> createSo(Map<String, Object> body) {
        String vbeln = numbers.next("SO");
        String kunnr = resolveCustomer(textOr(body, "kunnr", text(body, "customerCode")));
        List<Map<String, Object>> items = objectList(body.get("items"));
        if (items.isEmpty()) throw new BizException("销售订单至少需要一行");
        jdbc.update("INSERT INTO sap_sales_order(vbeln,auart,kunnr,vkorg,waers,status) VALUES(?,?,?,?,?,?)",
                vbeln, "OR", kunnr, textOr(body, "vkorg", "1000"), textOr(body, "waers", "CNY"), "OPEN");
        int pos = 10;
        for (Map<String, Object> item : items) {
            String mat = resolveMaterial(textOr(item, "matnr", text(item, "Material")));
            BigDecimal qty = bdOr(item, "qty", bd(item, "OrderQuantity"));
            BigDecimal price = bdOr(item, "price", bd(item, "NetPriceAmount"));
            if (price.compareTo(BigDecimal.ZERO) == 0) price = jdbc.queryForObject("SELECT std_price FROM sap_material WHERE matnr=?", BigDecimal.class, mat);
            jdbc.update("INSERT INTO sap_sales_order_item(vbeln,posnr,matnr,werks,kwmeng,netpr,delivered_qty,billed_qty) VALUES(?,?,?,?,?,?,0,0)",
                    vbeln, String.valueOf(pos), mat, textOr(item, "werks", "1000"), qty, price);
            pos += 10;
        }
        return jdbc.queryForMap("SELECT * FROM sap_sales_order WHERE vbeln=?", vbeln);
    }

    @Transactional
    public Map<String, Object> createDn(Map<String, Object> body) {
        String so = textOr(body, "soVbeln", text(body, "vbeln"));
        Map<String, Object> order = jdbc.queryForMap("SELECT * FROM sap_sales_order WHERE vbeln=?", so);
        String dn = numbers.next("DN");
        jdbc.update("INSERT INTO sap_delivery(vbeln,so_vbeln,kunnr,werks,status,bms_synced) VALUES(?,?,?,?,?,0)",
                dn, so, order.get("kunnr"), textOr(body, "werks", "1000"), "OPEN");
        List<Map<String, Object>> requested = objectList(body.get("items"));
        List<Map<String, Object>> lines = jdbc.queryForList("SELECT * FROM sap_sales_order_item WHERE vbeln=?", so);
        int i = 0;
        for (Map<String, Object> line : lines) {
            BigDecimal qty = requested.isEmpty() ? bd(line, "kwmeng").subtract(bd(line, "delivered_qty"))
                    : bdOr(requested.get(i++), "qty", bd(line, "kwmeng"));
            jdbc.update("INSERT INTO sap_delivery_item(vbeln,posnr,matnr,qty,picked_qty,pgi_qty) VALUES(?,?,?, ?,0,0)",
                    dn, line.get("posnr"), line.get("matnr"), qty);
        }
        return jdbc.queryForMap("SELECT * FROM sap_delivery WHERE vbeln=?", dn);
    }

    @Transactional
    public Map<String, Object> pickDn(String id) {
        jdbc.update("UPDATE sap_delivery d SET status='PICKED' WHERE vbeln=?", id);
        jdbc.update("UPDATE sap_delivery_item SET picked_qty=qty WHERE vbeln=?", id);
        return jdbc.queryForMap("SELECT * FROM sap_delivery WHERE vbeln=?", id);
    }

    @Transactional
    public Map<String, Object> pgi(String id) {
        Map<String, Object> dn = jdbc.queryForMap("SELECT * FROM sap_delivery WHERE vbeln=?", id);
        List<Map<String, Object>> lines = jdbc.queryForList("SELECT * FROM sap_delivery_item WHERE vbeln=?", id);
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> line : lines) {
            BigDecimal qty = bd(line, "qty");
            BigDecimal price = jdbc.queryForObject("SELECT std_price FROM sap_material WHERE matnr=?", BigDecimal.class, line.get("matnr"));
            changeStock(String.valueOf(line.get("matnr")), String.valueOf(dn.get("werks")), "0002", qty.negate(), qty.multiply(price).negate());
            total = total.add(qty.multiply(price));
        }
        String mblnr = numbers.next("MATERIAL");
        String fiNo = postFi("WE", "SD", id, Arrays.asList(
                new FiLine("6402", "S", total, null, null, null, "PGI成本"),
                new FiLine(stockAccount(String.valueOf(lines.get(0).get("matnr"))), "H", total, null, null, null, "PGI库存")));
        jdbc.update("INSERT INTO sap_material_document(mblnr,mjahr,budat,bwart,ref_type,ref_no,fi_belnr) VALUES(?,?,CURRENT_DATE,'601','SO',?,?)",
                mblnr, String.valueOf(LocalDate.now().getYear()), id, fiNo);
        jdbc.update("UPDATE sap_delivery SET status='PGI',material_doc=? WHERE vbeln=?", mblnr, id);
        jdbc.update("UPDATE sap_delivery_item SET pgi_qty=qty WHERE vbeln=?", id);
        jdbc.update("UPDATE sap_sales_order_item i SET delivered_qty=delivered_qty+(SELECT qty FROM sap_delivery_item d WHERE d.vbeln=? AND d.posnr=i.posnr) WHERE vbeln=(SELECT so_vbeln FROM sap_delivery WHERE vbeln=?)",
                id, id);
        Map<String, Object> payload = new HashMap<>();
        payload.put("extRef", id);
        payload.put("bizType", "OUTBOUND");
        payload.put("customerCode", resolveCustomerAlias(String.valueOf(dn.get("kunnr"))));
        payload.put("warehouseCode", plantWarehouse(String.valueOf(dn.get("werks"))));
        payload.put("bizDate", LocalDate.now().toString());
        payload.put("orders", 1);
        payload.put("lines", lines.size());
        payload.put("qty", total);
        Map<String, Object> bmsResult = pushBms("/api/open/oms/docs", payload);
        jdbc.update("UPDATE sap_delivery SET bms_synced=?,bms_doc_no=? WHERE vbeln=?", 1, bmsResult.get("docNo"), id);
        return jdbc.queryForMap("SELECT * FROM sap_delivery WHERE vbeln=?", id);
    }

    @Transactional
    public Map<String, Object> bill(Map<String, Object> body) {
        String dnNo = textOr(body, "dnVbeln", text(body, "vbeln"));
        Map<String, Object> dn = jdbc.queryForMap("SELECT * FROM sap_delivery WHERE vbeln=?", dnNo);
        List<Map<String, Object>> lines = jdbc.queryForList("SELECT * FROM sap_delivery_item WHERE vbeln=?", dnNo);
        BigDecimal net = BigDecimal.ZERO;
        for (Map<String, Object> line : lines) {
            BigDecimal price = jdbc.queryForObject("SELECT std_price FROM sap_material WHERE matnr=?", BigDecimal.class, line.get("matnr"));
            net = net.add(bd(line, "qty").multiply(price));
        }
        BigDecimal tax = net.multiply(new BigDecimal("0.13")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gross = net.add(tax);
        String vbeln = numbers.next("BILLING");
        String fiNo = postFi("DR", "SD", vbeln, Arrays.asList(
                new FiLine("1122", "S", gross, null, null, String.valueOf(dn.get("kunnr")), "应收"),
                new FiLine("6001", "H", net, null, null, null, "收入"),
                new FiLine("2221", "H", tax, null, null, null, "销项税")));
        jdbc.update("INSERT INTO sap_billing_doc(vbeln,fkart,kunnr,net,tax,gross,fi_belnr,status) VALUES(?,?,?,?,?,?,?,?)",
                vbeln, "F2", dn.get("kunnr"), net, tax, gross, fiNo, "POSTED");
        return jdbc.queryForMap("SELECT * FROM sap_billing_doc WHERE vbeln=?", vbeln);
    }

    @Transactional
    public Map<String, Object> createFi(Map<String, Object> body) {
        List<FiLine> lines = new ArrayList<>();
        for (Map<String, Object> item : objectList(body.get("items"))) {
            lines.add(new FiLine(text(item, "saknr"), textOr(item, "shkzg", "S"),
                    bd(item, "amount"), text(item, "kostl"), text(item, "lifnr"),
                    text(item, "kunnr"), text(item, "text")));
        }
        String belnr = postFi(textOr(body, "blart", "SA"), "MANUAL", text(body, "refNo"), lines);
        return jdbc.queryForMap("SELECT * FROM sap_acc_document WHERE belnr=?", belnr);
    }

    @Transactional
    public Map<String, Object> reverseFi(String id) {
        Map<String, Object> old = jdbc.queryForMap("SELECT * FROM sap_acc_document WHERE id=? OR belnr=?", id, id);
        List<Map<String, Object>> items = jdbc.queryForList("SELECT * FROM sap_acc_document_item WHERE belnr=?", old.get("belnr"));
        List<FiLine> reverse = new ArrayList<>();
        for (Map<String, Object> item : items) {
            reverse.add(new FiLine(String.valueOf(item.get("saknr")),
                    "S".equals(item.get("shkzg")) ? "H" : "S", bd(item, "amount"),
                    text(item, "kostl"), text(item, "lifnr"), text(item, "kunnr"), "冲销"));
        }
        String newNo = postFi("SA", "MANUAL", String.valueOf(old.get("belnr")), reverse);
        jdbc.update("UPDATE sap_acc_document SET reversed_by=? WHERE belnr=?", newNo, old.get("belnr"));
        return jdbc.queryForMap("SELECT * FROM sap_acc_document WHERE belnr=?", newNo);
    }

    @Transactional
    public Map<String, Object> payment(Map<String, Object> body) {
        String type = textOr(body, "type", "AP");
        String partner = text(body, "partner");
        BigDecimal amount = bd(body, "amount");
        List<FiLine> lines = new ArrayList<>();
        if ("AR".equalsIgnoreCase(type)) {
            lines.add(new FiLine("1002", "S", amount, null, null, null, "收款"));
            lines.add(new FiLine("1122", "H", amount, null, null, partner, "应收清账"));
        } else {
            lines.add(new FiLine("2201", "S", amount, null, partner, null, "应付清账"));
            lines.add(new FiLine("1002", "H", amount, null, null, null, "付款"));
        }
        String fi = postFi("AR".equalsIgnoreCase(type) ? "DZ" : "KZ", "FI", null, lines);
        jdbc.update("INSERT INTO sap_payment(type,partner,amount,belnr,cleared_docs) VALUES(?,?,?,?,?)",
                type, partner, amount, fi, String.valueOf(body.get("clearDocs")));
        return jdbc.queryForMap("SELECT * FROM sap_payment WHERE belnr=?", fi);
    }

    @Transactional
    public Map<String, Object> createStatement(Map<String, Object> body) {
        String statementNo = text(body, "statementNo");
        List<Map<String, Object>> existing = jdbc.queryForList("SELECT * FROM sap_bms_statement WHERE statement_no=?", statementNo);
        if (!existing.isEmpty()) return existing.get(0);
        String direction = textOr(body, "direction", "AR");
        BigDecimal amount = bd(body, "amount");
        BigDecimal tax = bd(body, "taxAmount");
        String partner = text(body, "partnerCode");
        String fi;
        if ("AR".equalsIgnoreCase(direction)) {
            String customer = resolveCustomer(partner);
            fi = postFi("DR", "BMS", statementNo, Arrays.asList(
                    new FiLine("1122", "S", amount, null, null, customer, "BMS应收"),
                    new FiLine("6001", "H", amount.subtract(tax), null, null, null, "BMS收入"),
                    new FiLine("2221", "H", tax, null, null, null, "BMS销项税")));
        } else {
            String vendor = resolveVendor(partner);
            fi = postFi("KR", "BMS", statementNo, Arrays.asList(
                    new FiLine("6601", "S", amount.subtract(tax), null, null, null, "BMS费用"),
                    new FiLine("2211", "S", tax, null, null, null, "BMS进项税"),
                    new FiLine("2201", "H", amount, null, vendor, null, "BMS应付")));
        }
        jdbc.update("INSERT INTO sap_bms_statement(statement_no,direction,partner_code,amount,tax_amount,biz_date,remark,belnr) VALUES(?,?,?,?,?,?,?,?)",
                statementNo, direction, partner, amount, tax, textOr(body, "bizDate", LocalDate.now().toString()), text(body, "remark"), fi);
        return jdbc.queryForMap("SELECT * FROM sap_bms_statement WHERE statement_no=?", statementNo);
    }

    @Transactional
    public Map<String, Object> createPr(Map<String, Object> body) {
        String banfn = numbers.next("PR");
        jdbc.update("INSERT INTO sap_purchase_req(banfn,status,requester,bukrs,werks,total_amount) VALUES(?,?,?,?,?,?)",
                banfn, "CREATED", textOr(body, "requester", "admin"), textOr(body, "bukrs", "1000"),
                textOr(body, "werks", "1000"), bd(body, "totalAmount"));
        return jdbc.queryForMap("SELECT * FROM sap_purchase_req WHERE banfn=?", banfn);
    }

    @Transactional
    public String saveEvaluation(Map<String, Object> body) {
        String lifnr = resolveVendor(text(body, "Supplier"));
        String period = textOr(body, "EvaluationPeriod", LocalDate.now().toString().substring(0, 7));
        jdbc.update("DELETE FROM sap_vendor_evaluation WHERE lifnr=? AND period=?", lifnr, period);
        jdbc.update("INSERT INTO sap_vendor_evaluation(lifnr,period,score,grade) VALUES(?,?,?,?)",
                lifnr, period, bd(body, "Score"), text(body, "Grade"));
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM sap_vendor_evaluation", Integer.class);
        return "EV" + String.format("%08d", count == null ? 1 : count);
    }

    @Transactional
    public Map<String, Object> createBom(Map<String, Object> body) {
        String mat = resolveMaterial(text(body, "matnr"));
        String werks = resolvePlant(textOr(body, "werks", "1000"));
        jdbc.update("DELETE FROM sap_bom_item WHERE matnr=? AND werks=?", mat, werks);
        jdbc.update("DELETE FROM sap_bom WHERE matnr=? AND werks=?", mat, werks);
        jdbc.update("INSERT INTO sap_bom(matnr,werks,base_qty) VALUES(?,?,?)", mat, werks, bdOr(body, "baseQty", BigDecimal.ONE));
        for (Map<String, Object> item : objectList(body.get("items"))) {
            jdbc.update("INSERT INTO sap_bom_item(matnr,werks,component,qty) VALUES(?,?,?,?)",
                    mat, werks, resolveMaterial(textOr(item, "component", text(item, "matnr"))), bd(item, "qty"));
        }
        return jdbc.queryForMap("SELECT * FROM sap_bom WHERE matnr=? AND werks=?", mat, werks);
    }

    @Transactional
    public Map<String, Object> createProductionOrder(Map<String, Object> body) {
        String aufnr = numbers.next("PRODORD");
        String mat = resolveMaterial(text(body, "matnr"));
        String werks = resolvePlant(textOr(body, "werks", "1000"));
        BigDecimal target = bdOr(body, "targetQty", bd(body, "qty"));
        List<Map<String, Object>> bom = jdbc.queryForList("SELECT * FROM sap_bom_item WHERE matnr=? AND werks=?", mat, werks);
        BigDecimal planned = BigDecimal.ZERO;
        jdbc.update("INSERT INTO sap_production_order(aufnr,matnr,werks,target_qty,delivered_qty,status,planned_cost,actual_cost) VALUES(?,?,?,?,0,?,?,0)",
                aufnr, mat, werks, target, "CRTD", BigDecimal.ZERO);
        for (Map<String, Object> item : bom) {
            BigDecimal req = bd(item, "qty").multiply(target);
            BigDecimal price = jdbc.queryForObject("SELECT std_price FROM sap_material WHERE matnr=?", BigDecimal.class, item.get("component"));
            planned = planned.add(req.multiply(price));
            jdbc.update("INSERT INTO sap_production_order_component(aufnr,matnr,req_qty,issued_qty) VALUES(?,?,?,0)",
                    aufnr, item.get("component"), req);
        }
        jdbc.update("UPDATE sap_production_order SET planned_cost=? WHERE aufnr=?", planned, aufnr);
        return jdbc.queryForMap("SELECT * FROM sap_production_order WHERE aufnr=?", aufnr);
    }

    @Transactional
    public Map<String, Object> releaseProduction(String id) {
        jdbc.update("UPDATE sap_production_order SET status='REL' WHERE aufnr=?", id);
        return jdbc.queryForMap("SELECT * FROM sap_production_order WHERE aufnr=?", id);
    }

    @Transactional
    public Map<String, Object> issueProductionOrder(String id, Map<String, Object> body) {
        Map<String, Object> order = jdbc.queryForMap("SELECT * FROM sap_production_order WHERE aufnr=?", id);
        if (!"REL".equals(order.get("status")) && !"CNF".equals(order.get("status"))) throw new BizException("生产订单未下达");
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> item : jdbc.queryForList("SELECT * FROM sap_production_order_component WHERE aufnr=?", id)) {
            BigDecimal qty = bd(item, "req_qty").subtract(bd(item, "issued_qty"));
            Map<String, Object> fake = new HashMap<>();
            fake.put("matnr", item.get("matnr"));
            fake.put("werks", order.get("werks"));
            fake.put("lgort", "0001");
            fake.put("qty", qty);
            fake.put("kostl", textOr(body, "kostl", "CC1000"));
            issueStockLine(fake, "261", id);
            BigDecimal price = jdbc.queryForObject("SELECT std_price FROM sap_material WHERE matnr=?", BigDecimal.class, item.get("matnr"));
            total = total.add(qty.multiply(price));
            jdbc.update("UPDATE sap_production_order_component SET issued_qty=req_qty WHERE id=?", item.get("id"));
        }
        jdbc.update("UPDATE sap_production_order SET actual_cost=actual_cost+? WHERE aufnr=?", total, id);
        return jdbc.queryForMap("SELECT * FROM sap_production_order WHERE aufnr=?", id);
    }

    @Transactional
    public Map<String, Object> confirmProduction(String id, Map<String, Object> body) {
        BigDecimal qty = bdOr(body, "qty", BigDecimal.ONE);
        jdbc.update("INSERT INTO sap_confirmation(aufnr,qty,budat) VALUES(?,?,CURRENT_DATE)", id, qty);
        jdbc.update("UPDATE sap_production_order SET status='CNF' WHERE aufnr=?", id);
        return jdbc.queryForMap("SELECT * FROM sap_confirmation WHERE id=IDENTITY()");
    }

    @Transactional
    public Map<String, Object> receiptProduction(String id, Map<String, Object> body) {
        Map<String, Object> order = jdbc.queryForMap("SELECT * FROM sap_production_order WHERE aufnr=?", id);
        BigDecimal qty = bdOr(body, "qty", bd(order, "target_qty"));
        String mat = String.valueOf(order.get("matnr"));
        BigDecimal price = jdbc.queryForObject("SELECT std_price FROM sap_material WHERE matnr=?", BigDecimal.class, mat);
        BigDecimal amount = qty.multiply(price);
        changeStock(mat, String.valueOf(order.get("werks")), "0002", qty, amount);
        String fi = postFi("WE", "PP", id, Arrays.asList(
                new FiLine(stockAccount(mat), "S", amount, null, null, null, "完工入库"),
                new FiLine("5001", "H", amount, null, null, null, "生产成本")));
        String mblnr = numbers.next("MATERIAL");
        jdbc.update("INSERT INTO sap_material_document(mblnr,mjahr,budat,bwart,ref_type,ref_no,fi_belnr) VALUES(?,?,CURRENT_DATE,'101','PRODORD',?,?)",
                mblnr, String.valueOf(LocalDate.now().getYear()), id, fi);
        jdbc.update("UPDATE sap_production_order SET delivered_qty=delivered_qty+?,status='CNF' WHERE aufnr=?", qty, id);
        return jdbc.queryForMap("SELECT * FROM sap_material_document WHERE mblnr=?", mblnr);
    }

    public Map<String, Object> teco(String id) {
        jdbc.update("UPDATE sap_production_order SET status='TECO' WHERE aufnr=?", id);
        return jdbc.queryForMap("SELECT * FROM sap_production_order WHERE aufnr=?", id);
    }

    private String postFi(String blart, String source, String refNo, List<FiLine> lines) {
        if (lines.size() < 2) throw new BizException("会计凭证至少需要两行");
        BigDecimal debit = BigDecimal.ZERO;
        BigDecimal credit = BigDecimal.ZERO;
        for (FiLine line : lines) {
            if (line.amount.compareTo(BigDecimal.ZERO) <= 0) throw new BizException("凭证金额必须大于0");
            if ("S".equalsIgnoreCase(line.shkzg)) debit = debit.add(line.amount);
            else if ("H".equalsIgnoreCase(line.shkzg)) credit = credit.add(line.amount);
            else throw new BizException("借贷标识必须为 S/H");
        }
        if (debit.subtract(credit).abs().compareTo(new BigDecimal("0.005")) > 0) {
            throw new BizException("借贷不平衡: 借方=" + debit + ",贷方=" + credit);
        }
        String belnr = numbers.next("FI");
        jdbc.update("INSERT INTO sap_acc_document(belnr,gjahr,bukrs,blart,budat,bldat,waers,header_text,ref_no,source) VALUES(?,?,? ,?,CURRENT_DATE,CURRENT_DATE,'CNY',?,?,?)",
                belnr, String.valueOf(LocalDate.now().getYear()), "1000", blart, source, refNo, source);
        int i = 1;
        for (FiLine line : lines) {
            jdbc.update("INSERT INTO sap_acc_document_item(belnr,buzei,bschl,shkzg,saknr,lifnr,kunnr,kostl,amount,text) VALUES(?,?,?,?,?,?,?,?,?,?)",
                    belnr, String.valueOf(i++), "S".equalsIgnoreCase(line.shkzg) ? "40" : "50",
                    line.shkzg, line.saknr, line.lifnr, line.kunnr, line.kostl, line.amount, line.text);
            if (line.kostl != null && !line.kostl.trim().isEmpty()) {
                jdbc.update("INSERT INTO sap_co_document(fi_belnr,kostl,cost_element,amount,budat,text) VALUES(?,?,?, ?,CURRENT_DATE,?)",
                        belnr, line.kostl, line.saknr, line.amount, line.text);
            }
        }
        return belnr;
    }

    private void issueStockLine(Map<String, Object> line, String bwart, String ref) {
        String mat = resolveMaterial(text(line, "matnr"));
        BigDecimal qty = bd(line, "qty");
        BigDecimal price = jdbc.queryForObject("SELECT std_price FROM sap_material WHERE matnr=?", BigDecimal.class, mat);
        changeStock(mat, textOr(line, "werks", "1000"), textOr(line, "lgort", "0001"), qty.negate(), qty.multiply(price).negate());
        postFi("WE", "MM", ref, Arrays.asList(
                new FiLine(bwart.equals("261") ? "5001" : "6401", "S", qty.multiply(price), text(line, "kostl"), null, null, "发料"),
                new FiLine(stockAccount(mat), "H", qty.multiply(price), null, null, null, "库存减少")));
    }

    private Map<String, Object> issueCostCenter(Map<String, Object> body) {
        for (Map<String, Object> item : objectList(body.get("items"))) {
            item.put("qty", bdOr(item, "qty", bd(item, "QuantityInBaseUnit")));
            issueStockLine(item, "201", textOr(body, "refNo", "CC"));
        }
        return materialDocument(body, "201", "MANUAL");
    }

    private Map<String, Object> transfer(Map<String, Object> body) {
        for (Map<String, Object> item : objectList(body.get("items"))) {
            String mat = resolveMaterial(textOr(item, "matnr", text(item, "Material")));
            BigDecimal qty = bd(item, "qty");
            String werks = textOr(item, "werks", "1000");
            String from = textOr(item, "lgort", "0001");
            String to = textOr(item, "toLgort", "0002");
            changeStock(mat, werks, from, qty.negate(), BigDecimal.ZERO);
            changeStock(mat, werks, to, qty, BigDecimal.ZERO);
        }
        return materialDocument(body, "311", "MANUAL");
    }

    private Map<String, Object> issueDelivery(Map<String, Object> body) {
        return pgi(textOr(body, "refNo", text(body, "vbeln")));
    }

    private Map<String, Object> issueProduction(Map<String, Object> body) {
        return issueProductionOrder(textOr(body, "refNo", text(body, "aufnr")), body);
    }

    private Map<String, Object> materialDocument(Map<String, Object> body, String bwart, String refType) {
        String mblnr = numbers.next("MATERIAL");
        jdbc.update("INSERT INTO sap_material_document(mblnr,mjahr,budat,bwart,ref_type,ref_no) VALUES(?,?,CURRENT_DATE,?,?,?)",
                mblnr, String.valueOf(LocalDate.now().getYear()), bwart, refType, text(body, "refNo"));
        return jdbc.queryForMap("SELECT * FROM sap_material_document WHERE mblnr=?", mblnr);
    }

    private void changeStock(String matnr, String werks, String lgort, BigDecimal qty, BigDecimal value) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM sap_stock WHERE matnr=? AND werks=? AND lgort=?",
                matnr, werks, lgort);
        if (rows.isEmpty()) {
            if (qty.compareTo(BigDecimal.ZERO) < 0) throw new BizException("库存不足: " + matnr);
            jdbc.update("INSERT INTO sap_stock(matnr,werks,lgort,unrestricted_qty,value) VALUES(?,?,?,?,?)",
                    matnr, werks, lgort, qty, value);
            return;
        }
        BigDecimal current = bd(rows.get(0), "unrestricted_qty");
        if (current.add(qty).compareTo(BigDecimal.ZERO) < 0) throw new BizException("库存不足: " + matnr);
        jdbc.update("UPDATE sap_stock SET unrestricted_qty=unrestricted_qty+?,value=value+? WHERE matnr=? AND werks=? AND lgort=?",
                qty, value, matnr, werks, lgort);
    }

    private void upsertGrIr(String ebeln, String ebelp, BigDecimal grQty, BigDecimal irQty,
                            BigDecimal grAmount, BigDecimal irAmount) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM sap_gr_ir WHERE ebeln=? AND ebelp=?", ebeln, ebelp);
        if (rows.isEmpty()) {
            jdbc.update("INSERT INTO sap_gr_ir(ebeln,ebelp,gr_qty,ir_qty,gr_amount,ir_amount) VALUES(?,?,?,?,?,?)",
                    ebeln, ebelp, grQty, irQty, grAmount, irAmount);
        } else {
            jdbc.update("UPDATE sap_gr_ir SET gr_qty=gr_qty+?,ir_qty=ir_qty+?,gr_amount=gr_amount+?,ir_amount=ir_amount+? WHERE ebeln=? AND ebelp=?",
                    grQty, irQty, grAmount, irAmount, ebeln, ebelp);
        }
    }

    private BigDecimal totalAmount(String mblnr) {
        BigDecimal n = jdbc.queryForObject("SELECT COALESCE(SUM(amount),0) FROM sap_material_document_item WHERE mblnr=?",
                BigDecimal.class, mblnr);
        return n == null ? BigDecimal.ZERO : n;
    }

    private Map<String, Object> findPo(String input) {
        if (input != null) {
            List<Map<String, Object>> exact = jdbc.queryForList("SELECT * FROM sap_purchase_order WHERE ebeln=? OR external_ref=?",
                    input, input);
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

    private Map<String, Object> findPoItem(String ebeln, String matnr, String itemRef) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM sap_purchase_order_item WHERE ebeln=? AND (matnr=? OR ebelp=?)",
                ebeln, matnr, itemRef);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private String resolveVendor(String value) {
        if (value == null) throw new BizException("供应商不能为空");
        List<Map<String, Object>> r = jdbc.queryForList("SELECT lifnr FROM sap_vendor WHERE lifnr=? OR alias_code=?", value, value);
        if (r.isEmpty()) throw new BizException("供应商不存在: " + value);
        return String.valueOf(r.get(0).get("lifnr"));
    }

    private String resolveMaterial(String value) {
        if (value == null) throw new BizException("物料不能为空");
        List<Map<String, Object>> r = jdbc.queryForList("SELECT matnr FROM sap_material WHERE matnr=? OR alias_code=?", value, value);
        if (r.isEmpty()) throw new BizException("物料不存在: " + value);
        return String.valueOf(r.get(0).get("matnr"));
    }

    private String resolveMaybeMaterial(String value) {
        if (value == null) return null;
        List<Map<String, Object>> r = jdbc.queryForList("SELECT matnr FROM sap_material WHERE matnr=? OR alias_code=?", value, value);
        return r.isEmpty() ? value : String.valueOf(r.get(0).get("matnr"));
    }

    private String resolvePlant(String value) {
        if (value == null) return "1000";
        List<Map<String, Object>> r = jdbc.queryForList("SELECT werks FROM sap_plant WHERE werks=?", value);
        if (r.isEmpty() && "P001".equals(value)) return "1000";
        if (r.isEmpty()) throw new BizException("工厂不存在: " + value);
        return String.valueOf(r.get(0).get("werks"));
    }

    private String resolveCustomer(String value) {
        if (value == null) throw new BizException("客户不能为空");
        List<Map<String, Object>> r = jdbc.queryForList("SELECT kunnr FROM sap_customer WHERE kunnr=? OR alias_code=?", value, value);
        if (r.isEmpty()) throw new BizException("客户不存在: " + value);
        return String.valueOf(r.get(0).get("kunnr"));
    }

    private String resolveCustomerAlias(String value) {
        List<Map<String, Object>> r = jdbc.queryForList("SELECT alias_code FROM sap_customer WHERE kunnr=?", value);
        return r.isEmpty() ? value : String.valueOf(r.get(0).get("alias_code"));
    }

    private String plantWarehouse(String werks) {
        List<Map<String, Object>> r = jdbc.queryForList("SELECT bms_warehouse_code FROM sap_plant WHERE werks=?", werks);
        return r.isEmpty() ? werks : String.valueOf(r.get(0).get("bms_warehouse_code"));
    }

    private String stockAccount(String matnr) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT mtart FROM sap_material WHERE matnr=?", matnr);
        return !rows.isEmpty() && "ROH".equals(rows.get(0).get("mtart")) ? "1411" : "1405";
    }

    private Map<String, Object> bmsInboundPayload(Map<String, Object> po, BigDecimal qty) {
        Map<String, Object> p = new HashMap<>();
        p.put("extRef", po.get("ebeln"));
        p.put("bizType", "INBOUND");
        p.put("customerCode", "CUST-001");
        p.put("supplierCode", resolveVendorAlias(String.valueOf(po.get("lifnr"))));
        p.put("warehouseCode", plantWarehouse("1000"));
        p.put("bizDate", LocalDate.now().toString());
        p.put("orders", 1);
        p.put("lines", 1);
        p.put("qty", qty);
        return p;
    }

    private String resolveVendorAlias(String lifnr) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT alias_code FROM sap_vendor WHERE lifnr=?", lifnr);
        return rows.isEmpty() ? lifnr : String.valueOf(rows.get(0).get("alias_code"));
    }

    private Map<String, Object> pushBms(String path, Map<String, Object> payload) {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> result = bms.push(path, payload);
            logs.write("OUT", "BMS", "BMS_PUSH_DELIVERY", payload, result, true, null,
                    System.currentTimeMillis() - start);
            return result;
        } catch (RuntimeException e) {
            logs.write("OUT", "BMS", "BMS_PUSH_DELIVERY", payload, null, false, e.getMessage(),
                    System.currentTimeMillis() - start);
            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            return result;
        }
    }

    private static void require(String value, String message) {
        if (value == null || value.trim().isEmpty()) throw new BizException(message);
    }

    private static Object keyValue(String table, Map<String, Object> body) {
        if ("sap_material".equals(table)) return text(body, "matnr");
        if ("sap_vendor".equals(table)) return text(body, "lifnr");
        if ("sap_customer".equals(table)) return text(body, "kunnr");
        if ("sap_gl_account".equals(table)) return text(body, "saknr");
        return text(body, "kostl");
    }

    private static String text(Map<String, Object> body, String key) {
        Object value = body == null ? null : body.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static String textOr(Map<String, Object> body, String key, String fallback) {
        String value = text(body, key);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) if (value != null && !value.trim().isEmpty()) return value;
        return null;
    }

    private static BigDecimal bd(Map<String, Object> body, String key) {
        Object value = body == null ? null : body.get(key);
        if (value == null || String.valueOf(value).trim().isEmpty()) return BigDecimal.ZERO;
        return new BigDecimal(String.valueOf(value));
    }

    private static BigDecimal bdOr(Map<String, Object> body, String key, BigDecimal fallback) {
        return body != null && body.get(key) != null ? bd(body, key) : (fallback == null ? BigDecimal.ZERO : fallback);
    }

    private static BigDecimal firstAmount(Map<String, Object> body, String... keys) {
        for (String key : keys) {
            if (body != null && body.get(key) != null) return bd(body, key);
        }
        return BigDecimal.ZERO;
    }

    private static List<Map<String, Object>> objectList(Object value) {
        if (!(value instanceof List)) return new ArrayList<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : (List<?>) value) if (item instanceof Map) result.add((Map<String, Object>) item);
        return result;
    }

    private static final class FiLine {
        private final String saknr;
        private final String shkzg;
        private final BigDecimal amount;
        private final String kostl;
        private final String lifnr;
        private final String kunnr;
        private final String text;

        private FiLine(String saknr, String shkzg, BigDecimal amount, String kostl,
                       String lifnr, String kunnr, String text) {
            this.saknr = saknr;
            this.shkzg = shkzg;
            this.amount = amount == null ? BigDecimal.ZERO : amount;
            this.kostl = kostl;
            this.lifnr = lifnr;
            this.kunnr = kunnr;
            this.text = text;
        }
    }
}
