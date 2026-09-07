package com.sap.mm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.co.entity.CostCenter;
import com.sap.co.mapper.CostCenterMapper;
import com.sap.common.BizException;
import com.sap.common.NumberRangeService;
import com.sap.fi.service.AccountingDocumentService;
import com.sap.integration.service.BmsPushService;
import com.sap.mm.dto.MigoRequest;
import com.sap.mm.entity.*;
import com.sap.mm.mapper.*;
import com.sap.pp.entity.ProductionOrder;
import com.sap.pp.mapper.ProductionOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

@Service
public class GoodsMovementService {
    private final NumberRangeService numbers;
    private final ReferenceDataService refs;
    private final PurchaseOrderService purchaseOrders;
    private final PurchaseOrderItemMapper purchaseOrderItems;
    private final MaterialDocumentMapper documents;
    private final MaterialDocumentItemMapper documentItems;
    private final GrIrMapper grIr;
    private final StockService stock;
    private final AccountingDocumentService accounting;
    private final BmsPushService bms;
    private final MaterialMapper materials;
    private final CostCenterMapper costCenters;
    private final ProductionOrderMapper productionOrders;

    public GoodsMovementService(NumberRangeService numbers, ReferenceDataService refs,
                                PurchaseOrderService purchaseOrders, PurchaseOrderItemMapper purchaseOrderItems,
                                MaterialDocumentMapper documents, MaterialDocumentItemMapper documentItems,
                                GrIrMapper grIr, StockService stock, AccountingDocumentService accounting,
                                BmsPushService bms, MaterialMapper materials, CostCenterMapper costCenters,
                                ProductionOrderMapper productionOrders) {
        this.numbers = numbers;
        this.refs = refs;
        this.purchaseOrders = purchaseOrders;
        this.purchaseOrderItems = purchaseOrderItems;
        this.documents = documents;
        this.documentItems = documentItems;
        this.grIr = grIr;
        this.stock = stock;
        this.accounting = accounting;
        this.bms = bms;
        this.materials = materials;
        this.costCenters = costCenters;
        this.productionOrders = productionOrders;
    }

    @Transactional
    public MaterialDocument post(MigoRequest request) {
        String bwart = request.getBwart() == null ? "101" : request.getBwart();
        if ("101".equals(bwart) && ("PO".equalsIgnoreCase(request.getRefType()) || request.getRefNo() != null)) {
            return receivePo(request);
        }
        if ("201".equals(bwart)) return issueCostCenter(request);
        if ("261".equals(bwart)) return issueProduction(request);
        if ("311".equals(bwart)) return transfer(request);
        throw new BizException("不支持的移动类型: " + bwart);
    }

    public MaterialDocument postMigo(MigoRequest request) {
        return post(request);
    }

    @Transactional
    public MaterialDocument receivePo(MigoRequest request) {
        PurchaseOrder order = purchaseOrders.find(request.getRefNo());
        String mblnr = numbers.next("MATERIAL");
        BigDecimal total = BigDecimal.ZERO;
        int lineNo = 1;
        for (MigoRequest.MigoItemRequest requestItem : request.getItems()) {
            String matnr = refs.material(requestItem.getMatnr());
            PurchaseOrderItem poItem = purchaseOrders.item(order.getEbeln(), matnr, requestItem.getEbelp());
            if (poItem == null) throw new BizException("物料不在采购订单中: " + matnr);
            BigDecimal qty = quantity(requestItem);
            BigDecimal delivered = zero(poItem.getDeliveredQty());
            BigDecimal open = poItem.getMenge().subtract(delivered);
            if (qty.compareTo(open) > 0) throw new BizException("收货数量超过未收数量");
            BigDecimal amount = qty.multiply(zero(poItem.getNetpr()));
            stock.change(matnr, poItem.getWerks(), poItem.getLgort(), qty, amount);
            poItem.setDeliveredQty(delivered.add(qty));
            purchaseOrderItems.updateById(poItem);
            upsertGrIr(order.getEbeln(), poItem.getEbelp(), qty, BigDecimal.ZERO, amount, BigDecimal.ZERO);
            insertItem(mblnr, lineNo++, matnr, poItem.getWerks(), poItem.getLgort(), qty, amount,
                    "101", order.getEbeln(), poItem.getEbelp(), null, null, null);
            total = total.add(amount);
        }
        AccountingDocumentService.FiLine debit = new AccountingDocumentService.FiLine(
                refs.stockAccount(request.getItems().get(0).getMatnr()), "S", total, null, null, null, "PO收货");
        AccountingDocumentService.FiLine credit = new AccountingDocumentService.FiLine(
                "2202", "H", total, null, null, order.getLifnr(), "GR/IR");
        String fi = accounting.post("WE", "MM", order.getEbeln(), Arrays.asList(debit, credit));
        MaterialDocument document = saveDocument(mblnr, "101", "PO", order.getEbeln(), fi);
        recomputeStatus(order);
        bms.pushInbound(order.getEbeln(), total);
        return document;
    }

    @Transactional
    public MaterialDocument issueCostCenter(MigoRequest request) {
        BigDecimal total = BigDecimal.ZERO;
        for (MigoRequest.MigoItemRequest item : request.getItems()) {
            if (item.getKostl() == null || item.getKostl().trim().isEmpty()) {
                throw new BizException("201移动类型必须指定成本中心");
            }
            CostCenter center = costCenters.selectById(item.getKostl());
            if (center == null) throw new BizException("成本中心不存在: " + item.getKostl());
            String mat = refs.material(item.getMatnr());
            BigDecimal qty = quantity(item);
            BigDecimal price = zero(materials.selectById(mat).getStdPrice());
            stock.change(mat, value(item.getWerks(), "1000"), value(item.getLgort(), "0001"),
                    qty.negate(), qty.multiply(price).negate());
            total = total.add(qty.multiply(price));
        }
        String fi = accounting.post("WE", "MM", value(request.getRefNo(), "CC"),
                Arrays.asList(new AccountingDocumentService.FiLine("6401", "S", total,
                                request.getItems().get(0).getKostl(), null, null, "发料"),
                        new AccountingDocumentService.FiLine(refs.stockAccount(request.getItems().get(0).getMatnr()),
                                "H", total, null, null, null, "库存减少")));
        return saveMovementDocument(request, "201", fi);
    }

    @Transactional
    public MaterialDocument issueProduction(MigoRequest request) {
        String aufnr = request.getAufnr();
        if (aufnr == null || aufnr.trim().isEmpty()) throw new BizException("261移动类型必须指定生产订单");
        ProductionOrder order = productionOrders.selectById(aufnr);
        if (order == null) throw new BizException("生产订单不存在: " + aufnr);
        BigDecimal total = BigDecimal.ZERO;
        for (MigoRequest.MigoItemRequest item : request.getItems()) {
            String mat = refs.material(item.getMatnr());
            BigDecimal qty = quantity(item);
            BigDecimal price = zero(materials.selectById(mat).getStdPrice());
            stock.change(mat, value(item.getWerks(), order.getWerks()), value(item.getLgort(), "0001"),
                    qty.negate(), qty.multiply(price).negate());
            total = total.add(qty.multiply(price));
        }
        String fi = accounting.post("WE", "MM", aufnr,
                Arrays.asList(new AccountingDocumentService.FiLine("5001", "S", total,
                                null, null, null, "生产领料"),
                        new AccountingDocumentService.FiLine(refs.stockAccount(request.getItems().get(0).getMatnr()),
                                "H", total, null, null, null, "库存减少")));
        return saveMovementDocument(request, "261", fi);
    }

    @Transactional
    public MaterialDocument transfer(MigoRequest request) {
        String mblnr = numbers.next("MATERIAL");
        BigDecimal total = BigDecimal.ZERO;
        int lineNo = 1;
        for (MigoRequest.MigoItemRequest item : request.getItems()) {
            String mat = refs.material(item.getMatnr());
            BigDecimal qty = quantity(item);
            String werks = value(item.getWerks(), "1000");
            BigDecimal price = zero(materials.selectById(mat).getStdPrice());
            stock.change(mat, werks, value(item.getLgort(), "0001"), qty.negate(), qty.multiply(price).negate());
            stock.change(mat, werks, value(item.getToLgort(), "0002"), qty, qty.multiply(price));
            insertItem(mblnr, lineNo++, mat, werks, value(item.getLgort(), "0001"), qty,
                    qty.multiply(price), "311", request.getRefNo(), item.getEbelp(), null, null, item.getToLgort());
            total = total.add(qty.multiply(price));
        }
        String fi = accounting.post("WE", "MM", value(request.getRefNo(), "TRANSFER"),
                Arrays.asList(new AccountingDocumentService.FiLine("1405", "S", total, null, null, null, "转储入库"),
                        new AccountingDocumentService.FiLine("1405", "H", total, null, null, null, "转储出库")));
        return saveDocument(mblnr, "311", "MANUAL", request.getRefNo(), fi);
    }

    private MaterialDocument saveMovementDocument(MigoRequest request, String bwart, String fi) {
        String mblnr = numbers.next("MATERIAL");
        int lineNo = 1;
        for (MigoRequest.MigoItemRequest item : request.getItems()) {
            String mat = refs.material(item.getMatnr());
            BigDecimal qty = quantity(item);
            BigDecimal price = zero(materials.selectById(mat).getStdPrice());
            insertItem(mblnr, lineNo++, mat, value(item.getWerks(), "1000"), value(item.getLgort(), "0001"),
                    qty, qty.multiply(price), bwart, request.getRefNo(), item.getEbelp(), item.getKostl(),
                    request.getAufnr(), item.getToLgort());
        }
        return saveDocument(mblnr, bwart, "MANUAL", request.getRefNo(), fi);
    }

    private MaterialDocument saveDocument(String mblnr, String bwart, String refType,
                                          String refNo, String fi) {
        MaterialDocument document = new MaterialDocument();
        document.setMblnr(mblnr);
        document.setMjahr(String.valueOf(LocalDate.now().getYear()));
        document.setBudat(LocalDate.now());
        document.setBwart(bwart);
        document.setRefType(refType);
        document.setRefNo(refNo);
        document.setFiBelnr(fi);
        documents.insert(document);
        document.setItems(documentItems.selectList(new LambdaQueryWrapper<MaterialDocumentItem>()
                .eq(MaterialDocumentItem::getMblnr, mblnr)));
        return document;
    }

    private void insertItem(String mblnr, int lineNo, String matnr, String werks, String lgort,
                            BigDecimal qty, BigDecimal amount, String bwart, String ebeln, String ebelp,
                            String kostl, String aufnr, String toLgort) {
        MaterialDocumentItem item = new MaterialDocumentItem();
        item.setMblnr(mblnr);
        item.setZeile(String.valueOf(lineNo));
        item.setMatnr(matnr);
        item.setWerks(werks);
        item.setLgort(lgort);
        item.setMenge(qty);
        item.setAmount(amount);
        item.setBwart(bwart);
        item.setEbeln(ebeln);
        item.setEbelp(ebelp);
        item.setKostl(kostl);
        item.setAufnr(aufnr);
        item.setToLgort(toLgort);
        documentItems.insert(item);
    }

    private void upsertGrIr(String ebeln, String ebelp, BigDecimal grQty, BigDecimal irQty,
                            BigDecimal grAmount, BigDecimal irAmount) {
        GrIr current = grIr.selectOne(new LambdaQueryWrapper<GrIr>()
                .eq(GrIr::getEbeln, ebeln).eq(GrIr::getEbelp, ebelp));
        if (current == null) {
            current = new GrIr();
            current.setEbeln(ebeln); current.setEbelp(ebelp);
            current.setGrQty(grQty); current.setIrQty(irQty);
            current.setGrAmount(grAmount); current.setIrAmount(irAmount);
            grIr.insert(current);
        } else {
            current.setGrQty(zero(current.getGrQty()).add(grQty));
            current.setIrQty(zero(current.getIrQty()).add(irQty));
            current.setGrAmount(zero(current.getGrAmount()).add(grAmount));
            current.setIrAmount(zero(current.getIrAmount()).add(irAmount));
            grIr.update(current, new LambdaQueryWrapper<GrIr>()
                    .eq(GrIr::getEbeln, ebeln).eq(GrIr::getEbelp, ebelp));
        }
    }

    private void recomputeStatus(PurchaseOrder order) {
        boolean open = purchaseOrderItems.selectList(new LambdaQueryWrapper<PurchaseOrderItem>()
                .eq(PurchaseOrderItem::getEbeln, order.getEbeln())).stream()
                .anyMatch(item -> zero(item.getDeliveredQty()).compareTo(zero(item.getMenge())) < 0);
        order.setStatus(open ? "PARTIAL" : "COMPLETED");
        purchaseOrders.updateStatus(order);
    }

    private BigDecimal quantity(MigoRequest.MigoItemRequest item) {
        BigDecimal qty = item.getQty() == null ? item.getQuantityInBaseUnit() : item.getQty();
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) throw new BizException("移动数量必须大于0");
        return qty;
    }

    private static BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String value(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
