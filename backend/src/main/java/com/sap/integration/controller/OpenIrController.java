package com.sap.integration.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.common.BizException;
import com.sap.common.R;
import com.sap.fi.entity.AccountingDocument;
import com.sap.fi.entity.AccountingDocumentItem;
import com.sap.fi.mapper.AccountingDocumentItemMapper;
import com.sap.fi.mapper.AccountingDocumentMapper;
import com.sap.mm.dto.PurchaseReqRequest;
import com.sap.mm.entity.Material;
import com.sap.mm.entity.PurchaseOrder;
import com.sap.mm.entity.PurchaseReq;
import com.sap.mm.entity.Stock;
import com.sap.mm.mapper.MaterialMapper;
import com.sap.mm.mapper.PurchaseOrderMapper;
import com.sap.mm.mapper.StockMapper;
import com.sap.mm.service.PurchaseReqService;
import com.sap.pp.entity.ProductionOrder;
import com.sap.pp.mapper.ProductionOrderMapper;
import com.sap.pp.service.ProductionOrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** IR 控制塔开放口：库存 / PR / PO / 生产订单 / 财务未清项快照与指令。 */
@RestController
@RequestMapping("/api/open/ir")
public class OpenIrController {
    private final StockMapper stocks;
    private final MaterialMapper materials;
    private final PurchaseReqService purchaseReqs;
    private final PurchaseOrderMapper purchaseOrders;
    private final ProductionOrderMapper productionOrders;
    private final ProductionOrderService productionOrderService;
    private final AccountingDocumentMapper documents;
    private final AccountingDocumentItemMapper documentItems;
    private final String apiKey;
    private final ConcurrentHashMap<String, Object> actionCache = new ConcurrentHashMap<String, Object>();

    public OpenIrController(
            StockMapper stocks,
            MaterialMapper materials,
            PurchaseReqService purchaseReqs,
            PurchaseOrderMapper purchaseOrders,
            ProductionOrderMapper productionOrders,
            ProductionOrderService productionOrderService,
            AccountingDocumentMapper documents,
            AccountingDocumentItemMapper documentItems,
            @Value("${sap.open.api-key:sap-open-key}") String apiKey) {
        this.stocks = stocks;
        this.materials = materials;
        this.purchaseReqs = purchaseReqs;
        this.purchaseOrders = purchaseOrders;
        this.productionOrders = productionOrders;
        this.productionOrderService = productionOrderService;
        this.documents = documents;
        this.documentItems = documentItems;
        this.apiKey = apiKey;
    }

    @GetMapping("/snapshots")
    public R<Map<String, Object>> snapshots(
            @RequestHeader(value = "X-Api-Key", required = false) String key) {
        checkKey(key);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Stock stock : stocks.selectList(null)) {
            BigDecimal qty = nz(stock.getUnrestrictedQty());
            String sku = aliasOf(stock.getMatnr());
            rows.add(row("STOCK", stock.getMatnr() + "/" + stock.getWerks() + "/" + stock.getLgort(),
                    qty.compareTo(BigDecimal.TEN) < 0 ? "LOW" : "OK",
                    sku, qty, stock.getStockValue(), stock.getWerks(),
                    sku + " " + stock.getWerks() + "/" + stock.getLgort()));
        }
        for (PurchaseReq pr : purchaseReqs.list()) {
            rows.add(row("PR", pr.getBanfn(), pr.getStatus(), firstMatnr(pr),
                    BigDecimal.ONE, pr.getTotalAmount(), pr.getWerks(),
                    "采购申请 " + pr.getBanfn()));
        }
        for (PurchaseOrder po : purchaseOrders.selectList(null)) {
            rows.add(row("PO", po.getEbeln(), po.getStatus(), null,
                    BigDecimal.ONE, po.getTotalAmount(), po.getBukrs(),
                    "采购订单 " + po.getEbeln()));
        }
        for (ProductionOrder mo : productionOrders.selectList(null)) {
            rows.add(row("MO", mo.getAufnr(), moStatus(mo.getStatus()), mo.getMatnr(),
                    mo.getTargetQty(), mo.getPlannedCost(), mo.getWerks(),
                    "生产订单 " + mo.getAufnr()));
        }
        appendOpenFi(rows);
        return R.ok(payload("SAP", rows));
    }

    @PostMapping("/actions")
    public R<Object> actions(
            @RequestHeader(value = "X-Api-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        checkKey(key);
        String type = str(body.get("type"));
        String targetKey = str(body.get("targetKey"));
        Map<String, Object> params = params(body);
        return R.ok(executeOnce(cacheKey(type, targetKey, body.get("idempotencyKey")), () -> {
            if ("SAP_CREATE_PR".equals(type)) {
                return purchaseReqs.create(prRequest(targetKey, params));
            }
            if ("SAP_RELEASE_PR".equals(type)) {
                return purchaseReqs.release(first(str(params.get("banfn")), targetKey));
            }
            if ("SAP_RELEASE_MO".equals(type)) {
                return productionOrderService.release(
                        first(str(params.get("aufnr")), targetKey));
            }
            throw new BizException("不支持的 IR 指令: " + type);
        }));
    }

    private Object executeOnce(String cacheKey, Supplier<Object> work) {
        if (cacheKey == null) {
            return work.get();
        }
        Object cached = actionCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        synchronized (actionCache) {
            cached = actionCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
            Object created = work.get();
            actionCache.put(cacheKey, created);
            return created;
        }
    }

    private static String cacheKey(String type, String targetKey, Object idempotencyKey) {
        String key = str(idempotencyKey);
        if (key == null || key.trim().isEmpty() || "null".equals(key)) {
            return null;
        }
        return type + "|" + (targetKey == null ? "" : targetKey) + "|" + key.trim();
    }

    private void appendOpenFi(List<Map<String, Object>> rows) {
        List<AccountingDocument> openDocs = documents.selectList(new LambdaQueryWrapper<AccountingDocument>()
                .isNull(AccountingDocument::getClearedBy)
                .isNull(AccountingDocument::getReversedBy)
                .ne(AccountingDocument::getBlart, "AB"));
        if (openDocs.isEmpty()) {
            return;
        }
        Map<String, AccountingDocument> byBelnr = new LinkedHashMap<String, AccountingDocument>();
        for (AccountingDocument document : openDocs) {
            byBelnr.put(document.getBelnr(), document);
        }
        appendOpenItems(rows, "AP_OPEN", "2201", true, byBelnr);
        appendOpenItems(rows, "AR_OPEN", "1122", false, byBelnr);
    }

    private void appendOpenItems(
            List<Map<String, Object>> rows, String dataType, String saknr, boolean ap,
            Map<String, AccountingDocument> byBelnr) {
        List<AccountingDocumentItem> openItems = documentItems.selectList(
                new LambdaQueryWrapper<AccountingDocumentItem>()
                        .eq(AccountingDocumentItem::getSaknr, saknr)
                        .in(AccountingDocumentItem::getBelnr, byBelnr.keySet()));
        for (AccountingDocumentItem item : openItems) {
            AccountingDocument document = byBelnr.get(item.getBelnr());
            String partner = ap ? item.getLifnr() : item.getKunnr();
            String title = (ap ? "应付未清 " : "应收未清 ") + item.getBelnr();
            rows.add(row(dataType, item.getBelnr() + "/" + nzStr(item.getBuzei()),
                    "OPEN", partner, BigDecimal.ONE, nz(item.getAmount()),
                    document == null ? null : document.getBukrs(), title));
        }
    }

    private PurchaseReqRequest prRequest(String targetKey, Map<String, Object> params) {
        PurchaseReqRequest request = new PurchaseReqRequest();
        request.setRequester("IR");
        request.setWerks(first(str(params.get("werks")), str(params.get("plantCode")), "1000"));
        PurchaseReqRequest.Item item = new PurchaseReqRequest.Item();
        item.setMatnr(first(str(params.get("sku")), str(params.get("matnr")), targetKey));
        item.setMenge(decimal(params.get("qty"), BigDecimal.ONE));
        item.setNetpr(decimal(params.get("netpr"), BigDecimal.ZERO));
        item.setWerks(request.getWerks());
        request.setItems(Collections.singletonList(item));
        return request;
    }

    private void checkKey(String key) {
        if (apiKey == null || apiKey.trim().isEmpty() || !apiKey.equals(key)) {
            throw new BizException("无效的 API Key");
        }
    }

    private static Map<String, Object> payload(String system, List<Map<String, Object>> snapshots) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("system", system);
        data.put("snapshots", snapshots);
        return data;
    }

    private static Map<String, Object> row(
            String dataType, String bizKey, String status, String sku,
            BigDecimal qty, BigDecimal amount, String plantCode, String title) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("dataType", dataType);
        row.put("bizKey", bizKey);
        row.put("status", status);
        row.put("sku", sku);
        row.put("qty", qty);
        row.put("amount", amount);
        row.put("plantCode", plantCode);
        row.put("title", title);
        return row;
    }

    private static String moStatus(String status) {
        if ("CRTD".equals(status)) {
            return "CREATED";
        }
        if ("REL".equals(status)) {
            return "RELEASED";
        }
        return status;
    }

    private String aliasOf(String matnr) {
        Material material = materials.selectById(matnr);
        if (material != null && material.getAliasCode() != null
                && !material.getAliasCode().trim().isEmpty()) {
            return material.getAliasCode();
        }
        return matnr;
    }

    private static String firstMatnr(PurchaseReq pr) {
        if (pr.getItems() == null || pr.getItems().isEmpty()) {
            return null;
        }
        return pr.getItems().get(0).getMatnr();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> params(Map<String, Object> body) {
        Object value = body.get("params");
        return value instanceof Map ? (Map<String, Object>) value : new LinkedHashMap<String, Object>();
    }

    private static String first(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty() && !"null".equals(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String nzStr(String value) {
        return value == null ? "" : value;
    }

    private static BigDecimal decimal(Object value, BigDecimal fallback) {
        if (value == null) {
            return fallback;
        }
        return new BigDecimal(String.valueOf(value));
    }
}
