package com.sap.mm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.common.BizException;
import com.sap.common.NumberRangeService;
import com.sap.mm.dto.PoCreateRequest;
import com.sap.mm.entity.PurchaseOrder;
import com.sap.mm.entity.PurchaseOrderItem;
import com.sap.mm.entity.PurchaseReq;
import com.sap.mm.entity.PurchaseReqItem;
import com.sap.mm.mapper.PurchaseOrderItemMapper;
import com.sap.mm.mapper.PurchaseOrderMapper;
import com.sap.mm.mapper.PurchaseReqItemMapper;
import com.sap.mm.mapper.PurchaseReqMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseOrderService {
    private final PurchaseOrderMapper orders;
    private final PurchaseOrderItemMapper items;
    private final NumberRangeService numbers;
    private final ReferenceDataService refs;
    private final PurchaseReqMapper requests;
    private final PurchaseReqItemMapper requestItems;

    public PurchaseOrderService(PurchaseOrderMapper orders, PurchaseOrderItemMapper items,
                                NumberRangeService numbers, ReferenceDataService refs,
                                PurchaseReqMapper requests, PurchaseReqItemMapper requestItems) {
        this.orders = orders;
        this.items = items;
        this.numbers = numbers;
        this.refs = refs;
        this.requests = requests;
        this.requestItems = requestItems;
    }

    @Transactional
    public PurchaseOrder create(PoCreateRequest request, String source) {
        String ebeln = numbers.next("PO");
        String supplier = request.getLifnr() == null ? request.getSupplierCode() : request.getLifnr();
        if (supplier == null || supplier.trim().isEmpty()) throw new BizException("供应商不能为空");
        String lifnr = refs.vendor(supplier);
        PurchaseOrder order = new PurchaseOrder();
        order.setEbeln(ebeln);
        order.setBsart(value(request.getBsart(), "NB"));
        order.setLifnr(lifnr);
        order.setEkorg(value(request.getEkorg(), "1000"));
        order.setEkgrp(value(request.getEkgrp(), "001"));
        order.setBukrs(value(request.getBukrs(), "1000"));
        order.setWaers(value(request.getWaers(), "CNY"));
        order.setStatus("OPEN");
        order.setExternalRef(request.getExternalRef());
        order.setSource(source);
        order.setTotalAmount(BigDecimal.ZERO);
        orders.insert(order);

        BigDecimal total = BigDecimal.ZERO;
        int pos = 10;
        List<PoCreateRequest.PoItemRequest> inputItems = request.getItems();
        if ((inputItems == null || inputItems.isEmpty()) && request.getPrBanfn() != null) {
            inputItems = new ArrayList<>();
            List<PurchaseReqItem> prItems = requestItems.selectList(new LambdaQueryWrapper<PurchaseReqItem>()
                    .eq(PurchaseReqItem::getBanfn, request.getPrBanfn()));
            for (PurchaseReqItem prItem : prItems) {
                PoCreateRequest.PoItemRequest poItem = new PoCreateRequest.PoItemRequest();
                poItem.setMatnr(prItem.getMatnr());
                poItem.setWerks(prItem.getWerks());
                poItem.setLgort(prItem.getLgort());
                poItem.setQty(prItem.getMenge());
                poItem.setPrice(prItem.getNetpr());
                inputItems.add(poItem);
            }
        }
        if (inputItems == null || inputItems.isEmpty()) throw new BizException("采购订单至少需要一行");
        for (PoCreateRequest.PoItemRequest requestItem : inputItems) {
            String matnr = refs.material(requestItem.getMatnr());
            String werks = refs.plant(requestItem.getWerks());
            BigDecimal qty = requestItem.getQty() == null ? requestItem.getMenge() : requestItem.getQty();
            BigDecimal price = requestItem.getPrice() == null ? requestItem.getNetpr() : requestItem.getPrice();
            if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException("采购数量必须大于0");
            }
            if (price == null) price = BigDecimal.ZERO;
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setEbeln(ebeln);
            item.setEbelp(String.valueOf(pos));
            item.setMatnr(matnr);
            item.setWerks(werks);
            item.setLgort(value(requestItem.getLgort(), "0001"));
            item.setMenge(qty);
            item.setNetpr(price);
            item.setDeliveredQty(BigDecimal.ZERO);
            item.setInvoicedQty(BigDecimal.ZERO);
            if (requestItem.getDeliveryDate() != null && !requestItem.getDeliveryDate().isEmpty()) {
                item.setDeliveryDate(java.time.LocalDate.parse(requestItem.getDeliveryDate()));
            }
            items.insert(item);
            total = total.add(qty.multiply(price));
            pos += 10;
        }
        order.setTotalAmount(total);
        orders.updateById(order);
        if (request.getPrBanfn() != null) {
            PurchaseReq pr = requests.selectOne(new LambdaQueryWrapper<PurchaseReq>()
                    .eq(PurchaseReq::getBanfn, request.getPrBanfn()));
            if (pr == null) throw new BizException("采购申请不存在: " + request.getPrBanfn());
            pr.setStatus("ORDERED");
            requests.updateById(pr);
        }
        return load(ebeln);
    }

    public PurchaseOrder find(String input) {
        PurchaseOrder order = null;
        if (input != null) {
            order = orders.selectOne(new LambdaQueryWrapper<PurchaseOrder>()
                    .eq(PurchaseOrder::getEbeln, input)
                    .or().eq(PurchaseOrder::getExternalRef, input));
        }
        if (order == null && input != null && input.startsWith("PO")) {
            order = orders.selectList(new LambdaQueryWrapper<PurchaseOrder>()
                            .eq(PurchaseOrder::getSource, "SRM")
                            .in(PurchaseOrder::getStatus, "OPEN", "PARTIAL")
                            .orderByDesc(PurchaseOrder::getEbeln))
                    .stream().findFirst().orElse(null);
            if (order != null) {
                order.setExternalRef(input);
                orders.updateById(order);
            }
        }
        if (order == null) throw new BizException("采购订单不存在: " + input);
        return load(order.getEbeln());
    }

    public PurchaseOrderItem item(String ebeln, String matnr, String itemRef) {
        return items.selectOne(new LambdaQueryWrapper<PurchaseOrderItem>()
                .eq(PurchaseOrderItem::getEbeln, ebeln)
                .and(w -> w.eq(PurchaseOrderItem::getMatnr, matnr)
                        .or().eq(PurchaseOrderItem::getEbelp, itemRef)));
    }

    public List<PurchaseOrder> list() {
        List<PurchaseOrder> result = orders.selectList(null);
        result.forEach(order -> order.setItems(items.selectList(new LambdaQueryWrapper<PurchaseOrderItem>()
                .eq(PurchaseOrderItem::getEbeln, order.getEbeln()))));
        return result;
    }

    public PurchaseOrder one(String ebeln) {
        PurchaseOrder order = orders.selectById(ebeln);
        if (order == null) throw new BizException("采购订单不存在: " + ebeln);
        return load(ebeln);
    }

    public void updateStatus(PurchaseOrder order) {
        orders.updateById(order);
    }

    private PurchaseOrder load(String ebeln) {
        PurchaseOrder order = orders.selectById(ebeln);
        if (order != null) {
            order.setItems(items.selectList(new LambdaQueryWrapper<PurchaseOrderItem>()
                    .eq(PurchaseOrderItem::getEbeln, ebeln)));
        }
        return order;
    }

    private static String value(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
