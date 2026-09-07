package com.sap.pp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.common.R;
import com.sap.pp.dto.*;
import com.sap.pp.entity.Bom;
import com.sap.pp.entity.BomItem;
import com.sap.pp.entity.Confirmation;
import com.sap.pp.entity.ProductionOrder;
import com.sap.pp.mapper.BomMapper;
import com.sap.pp.mapper.BomItemMapper;
import com.sap.pp.mapper.ProductionOrderMapper;
import com.sap.pp.mapper.ProductionOrderComponentMapper;
import com.sap.pp.service.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/pp")
@Validated
public class PpController {
    private final BomMapper boms;
    private final BomService bomService;
    private final ProductionOrderMapper orders;
    private final ProductionOrderService orderService;
    private final ProductionOrderComponentMapper components;
    private final BomItemMapper bomItems;

    public PpController(BomMapper boms, BomService bomService, ProductionOrderMapper orders,
                        ProductionOrderService orderService, ProductionOrderComponentMapper components,
                        BomItemMapper bomItems) {
        this.boms = boms; this.bomService = bomService; this.orders = orders; this.orderService = orderService;
        this.components = components; this.bomItems = bomItems;
    }

    @GetMapping("/bom")
    public R<List<Bom>> bom() { return R.ok(boms.selectList(null)); }

    @GetMapping("/bom/{matnr}")
    public R<Bom> bomDetail(@PathVariable String matnr, @RequestParam(defaultValue = "1000") String werks) {
        Bom result = bomService.find(matnr, werks);
        if (result != null) {
            result.setItems(bomItems.selectList(new LambdaQueryWrapper<BomItem>()
                    .eq(BomItem::getMatnr, matnr).eq(BomItem::getWerks, werks)));
        }
        return R.ok(result);
    }

    @PostMapping("/bom")
    public R<Bom> bom(@Valid @RequestBody BomRequest request) { return R.ok(bomService.create(request)); }

    @GetMapping("/orders")
    public R<List<ProductionOrder>> orders() { return R.ok(orders.selectList(null)); }

    @GetMapping("/orders/{aufnr}")
    public R<ProductionOrder> orderDetail(@PathVariable String aufnr) {
        return R.ok(orderService.one(aufnr));
    }

    @PostMapping("/orders")
    public R<ProductionOrder> order(@Valid @RequestBody ProductionOrderRequest request) { return R.ok(orderService.create(request)); }

    @PostMapping("/orders/{id}/release")
    public R<ProductionOrder> release(@PathVariable String id) { return R.ok(orderService.release(id)); }

    @PostMapping("/orders/{id}/issue")
    public R<ProductionOrder> issue(@PathVariable String id, @RequestParam(required = false, defaultValue = "CC1000") String kostl) {
        return R.ok(orderService.issue(id, kostl));
    }

    @PostMapping("/orders/{id}/confirm")
    public R<Confirmation> confirm(@PathVariable String id, @RequestParam(required = false, defaultValue = "1") BigDecimal qty) {
        return R.ok(orderService.confirm(id, qty));
    }

    @PostMapping("/orders/{id}/receipt")
    public R<com.sap.mm.entity.MaterialDocument> receipt(@PathVariable String id, @RequestParam(required = false) BigDecimal qty) {
        ProductionOrder order = orderService.one(id);
        return R.ok(orderService.receipt(id, qty == null ? order.getTargetQty() : qty));
    }

    @PostMapping("/orders/{id}/teco")
    public R<ProductionOrder> teco(@PathVariable String id) { return R.ok(orderService.teco(id)); }
}
