package com.sap.integration.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.integration.dto.*;
import com.sap.integration.entity.VendorEvaluation;
import com.sap.integration.mapper.VendorEvaluationMapper;
import com.sap.mm.dto.*;
import com.sap.mm.entity.MaterialDocument;
import com.sap.mm.entity.PurchaseOrder;
import com.sap.mm.entity.SupplierInvoice;
import com.sap.mm.service.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SrmInboundService {
    private final PurchaseOrderService purchaseOrders;
    private final GoodsMovementService goods;
    private final InvoiceVerificationService invoices;
    private final ReferenceDataService refs;
    private final VendorEvaluationMapper evaluations;

    public SrmInboundService(PurchaseOrderService purchaseOrders, GoodsMovementService goods,
                             InvoiceVerificationService invoices, ReferenceDataService refs,
                             VendorEvaluationMapper evaluations) {
        this.purchaseOrders = purchaseOrders;
        this.goods = goods;
        this.invoices = invoices;
        this.refs = refs;
        this.evaluations = evaluations;
    }

    public PurchaseOrder po(SrmPurchaseOrderPayload payload) {
        PoCreateRequest request = new PoCreateRequest();
        request.setBsart(payload.getPurchaseOrderType());
        request.setSupplierCode(payload.getSupplier());
        request.setEkorg(payload.getPurchasingOrganization());
        request.setEkgrp(payload.getPurchasingGroup());
        request.setBukrs(payload.getCompanyCode());
        request.setWaers(payload.getDocumentCurrency());
        List<PoCreateRequest.PoItemRequest> items = new ArrayList<>();
        for (SrmPurchaseOrderPayload.Item item : payload.getItems()) {
            PoCreateRequest.PoItemRequest x = new PoCreateRequest.PoItemRequest();
            x.setMatnr(item.getMaterial()); x.setQty(item.getOrderQuantity());
            x.setPrice(item.getNetPriceAmount()); x.setWerks(item.getPlant());
            x.setDeliveryDate(item.getScheduleLineDeliveryDate()); items.add(x);
        }
        request.setItems(items);
        return purchaseOrders.create(request, "SRM");
    }

    public MaterialDocument gr(SrmGoodsReceiptPayload payload) {
        MigoRequest request = new MigoRequest();
        request.setBwart("101"); request.setRefType("PO");
        List<MigoRequest.MigoItemRequest> items = new ArrayList<>();
        for (SrmGoodsReceiptPayload.Item item : payload.getItems()) {
            MigoRequest.MigoItemRequest x = new MigoRequest.MigoItemRequest();
            x.setMatnr(item.getMaterial()); x.setEbelp(item.getPurchaseOrderItem());
            x.setQty(item.getQuantityInBaseUnit());
            if (request.getRefNo() == null) request.setRefNo(item.getPurchaseOrder());
            items.add(x);
        }
        request.setItems(items);
        return goods.receivePo(request);
    }

    public SupplierInvoice invoice(SrmInvoicePayload payload) {
        MiroRequest request = new MiroRequest();
        request.setLifnr(payload.getInvoicingParty() == null ? payload.getSupplier() : payload.getInvoicingParty());
        request.setEbeln(payload.getPurchaseOrder()); request.setGrossAmount(payload.getInvoiceGrossAmount());
        request.setExternalInvoiceNo(payload.getSupplierInvoice());
        List<MiroRequest.MiroItemRequest> items = new ArrayList<>();
        for (SrmInvoicePayload.Item item : payload.getItems()) {
            MiroRequest.MiroItemRequest x = new MiroRequest.MiroItemRequest();
            x.setEbelp(item.getPurchaseOrderItem()); x.setQty(item.getQuantityInPurchaseOrderUnit());
            x.setPrice(item.getPurchaseOrderItemPrice());
            if (request.getEbeln() == null) request.setEbeln(item.getPurchaseOrder());
            items.add(x);
        }
        request.setItems(items);
        return invoices.post(request);
    }

    public String evaluation(SrmEvaluationPayload payload) {
        String lifnr = refs.vendor(payload.getSupplier());
        String period = payload.getEvaluationPeriod() == null ? "" : payload.getEvaluationPeriod();
        VendorEvaluation evaluation = evaluations.selectOne(new LambdaQueryWrapper<VendorEvaluation>()
                .eq(VendorEvaluation::getLifnr, lifnr).eq(VendorEvaluation::getPeriod, period));
        if (evaluation == null) {
            evaluation = new VendorEvaluation();
            evaluation.setLifnr(lifnr); evaluation.setPeriod(period);
            evaluations.insert(evaluation);
        }
        evaluation.setScore(payload.getScore()); evaluation.setGrade(payload.getGrade());
        evaluations.updateById(evaluation);
        return "EV" + String.format("%08d", evaluation.getId());
    }
}
