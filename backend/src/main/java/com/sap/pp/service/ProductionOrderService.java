package com.sap.pp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.common.BizException;
import com.sap.common.NumberRangeService;
import com.sap.fi.service.AccountingDocumentService;
import com.sap.mm.entity.Material;
import com.sap.mm.entity.MaterialDocument;
import com.sap.mm.entity.MaterialDocumentItem;
import com.sap.mm.mapper.MaterialDocumentItemMapper;
import com.sap.mm.mapper.MaterialDocumentMapper;
import com.sap.mm.mapper.MaterialMapper;
import com.sap.mm.service.ReferenceDataService;
import com.sap.mm.service.StockService;
import com.sap.pp.dto.ProductionOrderRequest;
import com.sap.pp.entity.Confirmation;
import com.sap.pp.entity.ProductionOrder;
import com.sap.pp.entity.ProductionOrderComponent;
import com.sap.pp.mapper.ConfirmationMapper;
import com.sap.pp.mapper.ProductionOrderComponentMapper;
import com.sap.pp.mapper.ProductionOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ProductionOrderService {
    private final ProductionOrderMapper orders;
    private final ProductionOrderComponentMapper components;
    private final ConfirmationMapper confirmations;
    private final MaterialMapper materials;
    private final NumberRangeService numbers;
    private final ReferenceDataService refs;
    private final StockService stock;
    private final AccountingDocumentService accounting;
    private final MaterialDocumentMapper materialDocuments;
    private final MaterialDocumentItemMapper materialDocumentItems;

    public ProductionOrderService(ProductionOrderMapper orders, ProductionOrderComponentMapper components,
                                  ConfirmationMapper confirmations, MaterialMapper materials,
                                  NumberRangeService numbers, ReferenceDataService refs, StockService stock,
                                  AccountingDocumentService accounting, MaterialDocumentMapper materialDocuments,
                                  MaterialDocumentItemMapper materialDocumentItems) {
        this.orders = orders; this.components = components; this.confirmations = confirmations;
        this.materials = materials; this.numbers = numbers; this.refs = refs; this.stock = stock;
        this.accounting = accounting; this.materialDocuments = materialDocuments;
        this.materialDocumentItems = materialDocumentItems;
    }

    @Transactional
    public ProductionOrder create(ProductionOrderRequest request) {
        String aufnr = numbers.next("PRODORD");
        String matnr = refs.material(request.getMatnr());
        String werks = refs.plant(request.getWerks());
        ProductionOrder order = new ProductionOrder();
        order.setAufnr(aufnr); order.setMatnr(matnr); order.setWerks(werks);
        order.setTargetQty(request.getTargetQty()); order.setDeliveredQty(BigDecimal.ZERO);
        order.setStatus("CRTD"); order.setPlannedCost(BigDecimal.ZERO); order.setActualCost(BigDecimal.ZERO);
        orders.insert(order);
        return load(order);
    }

    @Transactional
    public ProductionOrder release(String id) {
        ProductionOrder order = require(id); order.setStatus("REL"); orders.updateById(order); return load(order);
    }

    @Transactional
    public ProductionOrder issue(String id, String kostl) {
        ProductionOrder order = require(id);
        if (kostl == null || kostl.trim().isEmpty()) throw new BizException("成本中心不能为空");
        BigDecimal total = BigDecimal.ZERO;
        List<ProductionOrderComponent> lines = components.selectList(new LambdaQueryWrapper<ProductionOrderComponent>()
                .eq(ProductionOrderComponent::getAufnr, id));
        for (ProductionOrderComponent line : lines) {
            BigDecimal qty = zero(line.getReqQty()).subtract(zero(line.getIssuedQty()));
            if (qty.signum() <= 0) continue;
            Material material = materials.selectById(line.getMatnr());
            BigDecimal amount = qty.multiply(zero(material == null ? null : material.getStdPrice()));
            stock.change(line.getMatnr(), order.getWerks(), "0001", qty.negate(), amount.negate());
            line.setIssuedQty(zero(line.getReqQty())); components.updateById(line);
            total = total.add(amount);
        }
        if (total.signum() > 0) {
            accounting.post("WE", "PP", id, Arrays.asList(
                    new AccountingDocumentService.FiLine("5001", "S", total, kostl, null, null, "发料"),
                    new AccountingDocumentService.FiLine("1411", "H", total, null, null, null, "库存减少")));
        }
        order.setActualCost(zero(order.getActualCost()).add(total)); orders.updateById(order);
        return load(order);
    }

    @Transactional
    public Confirmation confirm(String id, BigDecimal qty) {
        require(id);
        Confirmation confirmation = new Confirmation();
        confirmation.setAufnr(id); confirmation.setQty(qty); confirmation.setBudat(LocalDate.now());
        confirmations.insert(confirmation);
        ProductionOrder order = require(id); order.setStatus("CNF"); orders.updateById(order);
        return confirmation;
    }

    @Transactional
    public MaterialDocument receipt(String id, BigDecimal qty) {
        ProductionOrder order = require(id);
        Material material = materials.selectById(order.getMatnr());
        BigDecimal amount = qty.multiply(zero(material == null ? null : material.getStdPrice()));
        stock.change(order.getMatnr(), order.getWerks(), "0002", qty, amount);
        String fi = accounting.post("WE", "PP", id, Arrays.asList(
                new AccountingDocumentService.FiLine("1405", "S", amount, null, null, null, "完工入库"),
                new AccountingDocumentService.FiLine("5001", "H", amount, null, null, null, "生产成本")));
        String mblnr = numbers.next("MATERIAL");
        MaterialDocument document = new MaterialDocument();
        document.setMblnr(mblnr); document.setMjahr(String.valueOf(LocalDate.now().getYear()));
        document.setBudat(LocalDate.now()); document.setBwart("101"); document.setRefType("PRODORD");
        document.setRefNo(id); document.setFiBelnr(fi); materialDocuments.insert(document);
        MaterialDocumentItem item = new MaterialDocumentItem();
        item.setMblnr(mblnr); item.setZeile("1"); item.setMatnr(order.getMatnr());
        item.setWerks(order.getWerks()); item.setLgort("0002"); item.setMenge(qty);
        item.setAmount(amount); item.setBwart("101"); item.setAufnr(id); materialDocumentItems.insert(item);
        order.setDeliveredQty(zero(order.getDeliveredQty()).add(qty)); order.setStatus("CNF"); orders.updateById(order);
        document.setItems(java.util.Collections.singletonList(item));
        return document;
    }

    @Transactional
    public ProductionOrder teco(String id) {
        ProductionOrder order = require(id); order.setStatus("TECO"); orders.updateById(order); return load(order);
    }

    public ProductionOrder one(String id) { return load(require(id)); }

    private ProductionOrder require(String id) {
        ProductionOrder order = orders.selectById(id);
        if (order == null) throw new BizException("生产订单不存在: " + id);
        return order;
    }

    private ProductionOrder load(ProductionOrder order) {
        order.setComponents(components.selectList(new LambdaQueryWrapper<ProductionOrderComponent>()
                .eq(ProductionOrderComponent::getAufnr, order.getAufnr())));
        return order;
    }

    private static BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
}
