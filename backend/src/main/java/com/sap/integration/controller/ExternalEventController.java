package com.sap.integration.controller;

import com.sap.common.BizException;
import com.sap.common.R;
import com.sap.mm.dto.MigoRequest;
import com.sap.mm.entity.MaterialDocument;
import com.sap.mm.service.GoodsMovementService;
import com.sap.sd.entity.Delivery;
import com.sap.sd.service.DeliveryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/** SRM 收货和 OMS 发货直接过账。同一外部单号重复推送返回原凭证。 */
@RestController
@RequestMapping("/api/open/events")
public class ExternalEventController {
    private final GoodsMovementService goods;
    private final DeliveryService deliveries;
    private final String apiKey;

    public ExternalEventController(GoodsMovementService goods, DeliveryService deliveries,
                                   @Value("${sap.open.api-key:sap-open-key}") String apiKey) {
        this.goods = goods;
        this.deliveries = deliveries;
        this.apiKey = apiKey;
    }

    @PostMapping("/goods-receipt")
    public R<MaterialDocument> goodsReceipt(
            @RequestHeader(value = "X-Api-Key", required = false) String key,
            @RequestBody MigoRequest request) {
        check(key);
        if (request.getBwart() == null) {
            request.setBwart("101");
        }
        if (request.getRefType() == null) {
            request.setRefType("PO");
        }
        return R.ok(goods.post(request));
    }

    @PostMapping("/delivery")
    public R<Delivery> delivery(
            @RequestHeader(value = "X-Api-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        check(key);
        String orderNo = text(body.get("orderNo"));
        String matnr = text(body.get("matnr"));
        if (matnr == null && body.get("items") instanceof java.util.List) {
            java.util.List<?> items = (java.util.List<?>) body.get("items");
            if (!items.isEmpty() && items.get(0) instanceof Map) {
                matnr = text(((Map<?, ?>) items.get(0)).get("sku"));
            }
        }
        BigDecimal qty = decimal(body.get("qty"));
        return R.ok(deliveries.postExternal(orderNo, text(body.get("kunnr")), matnr, qty));
    }

    private void check(String key) {
        if (apiKey == null || apiKey.trim().isEmpty() || !apiKey.equals(key)) {
            throw new BizException("X-Api-Key 无效");
        }
    }

    private static String text(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() || "null".equals(text) ? null : text;
    }

    private static BigDecimal decimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
