package com.sap.sd.controller;

import com.sap.common.PageResult;
import com.sap.common.R;
import com.sap.sd.dto.*;
import com.sap.sd.entity.*;
import com.sap.sd.mapper.BillingDocMapper;
import com.sap.sd.mapper.CustomerMapper;
import com.sap.sd.mapper.SalesOrderMapper;
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

    public SdController(CustomerMapper customers, SalesOrderMapper salesOrders, BillingDocMapper billings,
                        SalesOrderService sales, DeliveryService deliveries, BillingService billing) {
        this.customers = customers; this.salesOrders = salesOrders; this.billings = billings;
        this.sales = sales; this.deliveries = deliveries; this.billing = billing;
    }

    @GetMapping("/customers")
    public R<PageResult<Customer>> customers(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "20") long size) {
        return R.ok(page(customers.selectList(null), page, size));
    }

    @PostMapping("/so")
    public R<SalesOrder> so(@Valid @RequestBody SoCreateRequest request) { return R.ok(sales.create(request)); }

    @GetMapping("/so")
    public R<List<SalesOrder>> sos() { return R.ok(sales.list()); }

    @PostMapping("/dn")
    public R<Delivery> dn(@Valid @RequestBody DnCreateRequest request) { return R.ok(deliveries.create(request)); }

    @PostMapping("/dn/{id}/pick")
    public R<Delivery> pick(@PathVariable String id) { return R.ok(deliveries.pick(id)); }

    @PostMapping("/dn/{id}/pgi")
    public R<Delivery> pgi(@PathVariable String id) { return R.ok(deliveries.pgi(id)); }

    @PostMapping("/billing")
    public R<BillingDoc> bill(@Valid @RequestBody BillingRequest request) { return R.ok(billing.create(request)); }

    @GetMapping("/billing")
    public R<List<BillingDoc>> bills() { return R.ok(billings.selectList(null)); }

    private static <T> PageResult<T> page(List<T> all, long page, long size) {
        long from = Math.max(0, (page - 1) * size);
        long to = Math.min(all.size(), from + size);
        List<T> records = from >= all.size() ? java.util.Collections.emptyList() : all.subList((int) from, (int) to);
        return new PageResult<>(all.size(), page, size, records);
    }
}
