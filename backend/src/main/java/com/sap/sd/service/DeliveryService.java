package com.sap.sd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.common.BizException;
import com.sap.common.NumberRangeService;
import com.sap.fi.service.AccountingDocumentService;
import com.sap.integration.service.BmsPushService;
import com.sap.mm.entity.Material;
import com.sap.mm.entity.MaterialDocument;
import com.sap.mm.entity.MaterialDocumentItem;
import com.sap.mm.mapper.MaterialDocumentItemMapper;
import com.sap.mm.mapper.MaterialDocumentMapper;
import com.sap.mm.mapper.MaterialMapper;
import com.sap.mm.service.ReferenceDataService;
import com.sap.mm.service.StockService;
import com.sap.sd.dto.DnCreateRequest;
import com.sap.sd.entity.*;
import com.sap.sd.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@Service
public class DeliveryService {
    private final DeliveryMapper deliveries;
    private final DeliveryItemMapper deliveryItems;
    private final SalesOrderMapper salesOrders;
    private final SalesOrderItemMapper salesOrderItems;
    private final MaterialMapper materials;
    private final NumberRangeService numbers;
    private final ReferenceDataService refs;
    private final StockService stock;
    private final AccountingDocumentService accounting;
    private final BmsPushService bms;
    private final MaterialDocumentMapper materialDocuments;
    private final MaterialDocumentItemMapper materialDocumentItems;

    public DeliveryService(DeliveryMapper deliveries, DeliveryItemMapper deliveryItems, SalesOrderMapper salesOrders,
                           SalesOrderItemMapper salesOrderItems, MaterialMapper materials, NumberRangeService numbers,
                           ReferenceDataService refs, StockService stock, AccountingDocumentService accounting,
                           BmsPushService bms, MaterialDocumentMapper materialDocuments,
                           MaterialDocumentItemMapper materialDocumentItems) {
        this.deliveries = deliveries; this.deliveryItems = deliveryItems; this.salesOrders = salesOrders;
        this.salesOrderItems = salesOrderItems; this.materials = materials; this.numbers = numbers;
        this.refs = refs; this.stock = stock; this.accounting = accounting; this.bms = bms;
        this.materialDocuments = materialDocuments; this.materialDocumentItems = materialDocumentItems;
    }

    @Transactional
    public Delivery create(DnCreateRequest request) {
        SalesOrder order = salesOrders.selectById(request.getSoVbeln());
        if (order == null) throw new BizException("销售订单不存在: " + request.getSoVbeln());
        Delivery delivery = new Delivery();
        delivery.setVbeln(numbers.next("DN")); delivery.setSoVbeln(order.getVbeln()); delivery.setKunnr(order.getKunnr());
        delivery.setWerks(value(request.getWerks(), "1000")); delivery.setStatus("OPEN"); delivery.setBmsSynced(0);
        deliveries.insert(delivery);
        List<SalesOrderItem> lines = salesOrderItems.selectList(new LambdaQueryWrapper<SalesOrderItem>()
                .eq(SalesOrderItem::getVbeln, order.getVbeln()));
        int index = 0;
        for (SalesOrderItem line : lines) {
            BigDecimal qty = zero(line.getKwmeng()).subtract(zero(line.getDeliveredQty()));
            if (request.getItems() != null && index < request.getItems().size()
                    && request.getItems().get(index).getQty() != null) qty = request.getItems().get(index).getQty();
            DeliveryItem item = new DeliveryItem();
            item.setVbeln(delivery.getVbeln()); item.setPosnr(line.getPosnr()); item.setMatnr(line.getMatnr());
            item.setQty(qty); item.setPickedQty(BigDecimal.ZERO); item.setPgiQty(BigDecimal.ZERO);
            deliveryItems.insert(item); index++;
        }
        return load(delivery);
    }

    @Transactional
    public Delivery pick(String id) {
        Delivery delivery = require(id); delivery.setStatus("PICKED"); deliveries.updateById(delivery);
        List<DeliveryItem> items = deliveryItems.selectList(new LambdaQueryWrapper<DeliveryItem>()
                .eq(DeliveryItem::getVbeln, id));
        for (DeliveryItem item : items) { item.setPickedQty(item.getQty()); deliveryItems.updateById(item); }
        return load(delivery);
    }

    @Transactional
    public Delivery pgi(String id) {
        Delivery delivery = require(id);
        List<DeliveryItem> lines = deliveryItems.selectList(new LambdaQueryWrapper<DeliveryItem>()
                .eq(DeliveryItem::getVbeln, id));
        BigDecimal total = BigDecimal.ZERO;
        for (DeliveryItem line : lines) {
            Material material = materials.selectById(line.getMatnr());
            BigDecimal amount = zero(line.getQty()).multiply(zero(material == null ? null : material.getStdPrice()));
            stock.change(line.getMatnr(), delivery.getWerks(), "0002", zero(line.getQty()).negate(), amount.negate());
            total = total.add(amount);
        }
        String fi = accounting.post("WE", "SD", id, Arrays.asList(
                new AccountingDocumentService.FiLine("6402", "S", total, null, null, null, "PGI成本"),
                new AccountingDocumentService.FiLine(refs.stockAccount(lines.get(0).getMatnr()), "H", total, null, null, null, "PGI库存")));
        String mblnr = numbers.next("MATERIAL");
        MaterialDocument document = new MaterialDocument();
        document.setMblnr(mblnr); document.setMjahr(String.valueOf(LocalDate.now().getYear()));
        document.setBudat(LocalDate.now()); document.setBwart("601"); document.setRefType("SO");
        document.setRefNo(id); document.setFiBelnr(fi); materialDocuments.insert(document);
        int lineNo = 1;
        for (DeliveryItem line : lines) {
            MaterialDocumentItem item = new MaterialDocumentItem();
            item.setMblnr(mblnr); item.setZeile(String.valueOf(lineNo++)); item.setMatnr(line.getMatnr());
            item.setWerks(delivery.getWerks()); item.setLgort("0002"); item.setMenge(line.getQty());
            item.setAmount(line.getQty().multiply(zero(materials.selectById(line.getMatnr()).getStdPrice())));
            item.setBwart("601"); item.setToLgort("0002"); materialDocumentItems.insert(item);
            line.setPgiQty(line.getQty()); deliveryItems.updateById(line);
        }
        delivery.setStatus("PGI"); delivery.setMaterialDoc(mblnr);
        Map<String, Object> payload = new HashMap<>();
        payload.put("extRef", id); payload.put("bizType", "OUTBOUND");
        payload.put("customerCode", refs.alias("sap_customer", "kunnr", delivery.getKunnr()));
        payload.put("warehouseCode", "WH-" + delivery.getWerks()); payload.put("bizDate", LocalDate.now().toString());
        payload.put("orders", 1); payload.put("lines", lines.size()); payload.put("qty", total);
        Map<String, Object> pushed = bms.push("/api/open/oms/docs", payload);
        delivery.setBmsSynced(1); delivery.setBmsDocNo(pushed.get("docNo") == null ? null : String.valueOf(pushed.get("docNo")));
        deliveries.updateById(delivery);
        document.setItems(materialDocumentItems.selectList(new LambdaQueryWrapper<MaterialDocumentItem>()
                .eq(MaterialDocumentItem::getMblnr, mblnr)));
        return load(delivery);
    }

    public Delivery one(String id) { return load(require(id)); }

    private Delivery require(String id) {
        Delivery delivery = deliveries.selectById(id);
        if (delivery == null) throw new BizException("交货单不存在: " + id);
        return delivery;
    }

    private Delivery load(Delivery delivery) {
        delivery.setItems(deliveryItems.selectList(new LambdaQueryWrapper<DeliveryItem>()
                .eq(DeliveryItem::getVbeln, delivery.getVbeln())));
        return delivery;
    }

    private static BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private static String value(String value, String fallback) { return value == null || value.trim().isEmpty() ? fallback : value; }
}
