package com.sap.sd.controller;

import com.sap.common.PageResult;
import com.sap.common.R;
import com.sap.sd.dto.*;
import com.sap.sd.entity.*;
import com.sap.sd.mapper.BillingDocMapper;
import com.sap.sd.mapper.BillingDocItemMapper;
import com.sap.sd.mapper.CustomerMapper;
import com.sap.sd.mapper.DeliveryItemMapper;
import com.sap.sd.mapper.DeliveryMapper;
import com.sap.sd.mapper.SalesOrderMapper;
import com.sap.sd.mapper.SalesOrderItemMapper;
import com.sap.sd.service.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/sd")
@Validated
public class SdController {
    private final CustomerMapper customers;
    private final SalesOrderMapper salesOrders;
    private final BillingDocMapper billings;
    private final SalesOrderService sales;
    private final DeliveryService deliveries;
    private final BillingService billing;
    private final SalesOrderItemMapper salesOrderItems;
    private final DeliveryMapper deliveryMapper;
    private final DeliveryItemMapper deliveryItems;
    private final BillingDocItemMapper billingItems;

    public SdController(CustomerMapper customers, SalesOrderMapper salesOrders, BillingDocMapper billings,
                        SalesOrderService sales, DeliveryService deliveries, BillingService billing,
                        SalesOrderItemMapper salesOrderItems, DeliveryMapper deliveryMapper,
                        DeliveryItemMapper deliveryItems, BillingDocItemMapper billingItems) {
        this.customers = customers; this.salesOrders = salesOrders; this.billings = billings;
        this.sales = sales; this.deliveries = deliveries; this.billing = billing;
        this.salesOrderItems = salesOrderItems; this.deliveryMapper = deliveryMapper;
        this.deliveryItems = deliveryItems; this.billingItems = billingItems;
    }

    @GetMapping("/customers")
    public R<PageResult<Customer>> customers(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "20") long size) {
        return R.ok(page(customers.selectList(null), page, size));
    }

    @PostMapping("/customers")
    public R<Customer> createCustomer(@RequestBody Customer customer) {
        customers.insert(customer);
        return R.ok(customer);
    }

    @PutMapping("/customers/{kunnr}")
    public R<Customer> updateCustomer(@PathVariable String kunnr, @RequestBody Customer input) {
        input.setKunnr(kunnr);
        customers.updateById(input);
        return R.ok(customers.selectById(kunnr));
    }

    @PostMapping("/so")
    public R<SalesOrder> so(@Valid @RequestBody SoCreateRequest request) { return R.ok(sales.create(request)); }

    @GetMapping("/so")
    public R<List<SalesOrder>> sos() { return R.ok(sales.list()); }

    @GetMapping("/so/{vbeln}")
    public R<SalesOrder> soDetail(@PathVariable String vbeln) {
        SalesOrder order = salesOrders.selectById(vbeln);
        if (order == null) throw new com.sap.common.BizException("销售订单不存在: " + vbeln);
        order.setItems(salesOrderItems.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.sap.sd.entity.SalesOrderItem>()
                .eq(com.sap.sd.entity.SalesOrderItem::getVbeln, vbeln)));
        return R.ok(order);
    }

    @PostMapping("/dn")
    public R<Delivery> dn(@Valid @RequestBody DnCreateRequest request) { return R.ok(deliveries.create(request)); }

    @GetMapping("/dn")
    public R<List<Delivery>> dns() {
        List<Delivery> result = deliveryMapper.selectList(null);
        result.forEach(this::loadDelivery);
        return R.ok(result);
    }

    @GetMapping("/dn/{vbeln}")
    public R<Delivery> dnDetail(@PathVariable String vbeln) {
        Delivery result = deliveryMapper.selectById(vbeln);
        if (result == null) throw new com.sap.common.BizException("交货单不存在: " + vbeln);
        return R.ok(loadDelivery(result));
    }

    @PostMapping("/dn/{id}/pick")
    public R<Delivery> pick(@PathVariable String id) { return R.ok(deliveries.pick(id)); }

    @PostMapping("/dn/{id}/pgi")
    public R<Delivery> pgi(@PathVariable String id) { return R.ok(deliveries.pgi(id)); }

    @PostMapping("/billing")
    public R<BillingDoc> bill(@Valid @RequestBody BillingRequest request) { return R.ok(billing.create(request)); }

    @GetMapping("/billing")
    public R<List<BillingDoc>> bills() { return R.ok(billings.selectList(null)); }

    @GetMapping("/billing/{vbeln}")
    public R<BillingDoc> billDetail(@PathVariable String vbeln) {
        BillingDoc result = billings.selectById(vbeln);
        if (result == null) throw new com.sap.common.BizException("发票不存在: " + vbeln);
        result.setItems(billingItems.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.sap.sd.entity.BillingDocItem>()
                .eq(com.sap.sd.entity.BillingDocItem::getVbeln, vbeln)));
        return R.ok(result);
    }

    private Delivery loadDelivery(Delivery value) {
        value.setItems(deliveryItems.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.sap.sd.entity.DeliveryItem>()
                .eq(com.sap.sd.entity.DeliveryItem::getVbeln, value.getVbeln())));
        return value;
    }

    private static <T> PageResult<T> page(List<T> all, long page, long size) {
        long from = Math.max(0, (page - 1) * size);
        long to = Math.min(all.size(), from + size);
        List<T> records = from >= all.size() ? java.util.Collections.emptyList() : all.subList((int) from, (int) to);
        return new PageResult<>(all.size(), page, size, records);
    }
}
