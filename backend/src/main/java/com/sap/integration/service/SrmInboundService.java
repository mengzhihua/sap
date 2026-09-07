package com.sap.integration.service;
import com.sap.integration.dto.*;
import com.sap.mm.dto.*;
import com.sap.mm.service.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class SrmInboundService {
    private final PurchaseOrderService purchaseOrders; private final GoodsMovementService goods; private final InvoiceVerificationService invoices;
    private final ReferenceDataService refs;
    private final JdbcTemplate jdbc;
    public SrmInboundService(PurchaseOrderService purchaseOrders,GoodsMovementService goods,InvoiceVerificationService invoices,ReferenceDataService refs,JdbcTemplate jdbc){
        this.purchaseOrders=purchaseOrders;this.goods=goods;this.invoices=invoices;this.refs=refs;this.jdbc=jdbc;
    }
    public Map<String,Object> po(SrmPurchaseOrderPayload payload){
        PoCreateRequest request=new PoCreateRequest(); request.setBsart(payload.getPurchaseOrderType());request.setSupplierCode(payload.getSupplier());
        request.setEkorg(payload.getPurchasingOrganization());request.setEkgrp(payload.getPurchasingGroup());request.setBukrs(payload.getCompanyCode());request.setWaers(payload.getDocumentCurrency());
        List<PoCreateRequest.PoItemRequest> items=new ArrayList<>();
        for(SrmPurchaseOrderPayload.Item item:payload.getItems()){
            PoCreateRequest.PoItemRequest x=new PoCreateRequest.PoItemRequest();x.setMatnr(item.getMaterial());x.setQty(item.getOrderQuantity());x.setPrice(item.getNetPriceAmount());x.setWerks(item.getPlant());x.setDeliveryDate(item.getScheduleLineDeliveryDate());items.add(x);
        } request.setItems(items);return purchaseOrders.create(request,"SRM");
    }
    public Map<String,Object> gr(SrmGoodsReceiptPayload payload){
        MigoRequest request=new MigoRequest();request.setBwart("101");request.setRefType("PO");
        List<MigoRequest.MigoItemRequest> items=new ArrayList<>();
        for(SrmGoodsReceiptPayload.Item item:payload.getItems()){MigoRequest.MigoItemRequest x=new MigoRequest.MigoItemRequest();x.setMatnr(item.getMaterial());x.setEbelp(item.getPurchaseOrderItem());x.setQty(item.getQuantityInBaseUnit());if(request.getRefNo()==null)request.setRefNo(item.getPurchaseOrder());items.add(x);}
        request.setItems(items);return goods.receivePo(request);
    }
    public Map<String,Object> invoice(SrmInvoicePayload payload){
        MiroRequest request=new MiroRequest();request.setLifnr(payload.getInvoicingParty()==null?payload.getSupplier():payload.getInvoicingParty());request.setEbeln(payload.getPurchaseOrder());request.setGrossAmount(payload.getInvoiceGrossAmount());request.setExternalInvoiceNo(payload.getSupplierInvoice());
        List<MiroRequest.MiroItemRequest> items=new ArrayList<>();
        for(SrmInvoicePayload.Item item:payload.getItems()){MiroRequest.MiroItemRequest x=new MiroRequest.MiroItemRequest();x.setEbelp(item.getPurchaseOrderItem());x.setQty(item.getQuantityInPurchaseOrderUnit());x.setPrice(item.getPurchaseOrderItemPrice());if(request.getEbeln()==null)request.setEbeln(item.getPurchaseOrder());items.add(x);}request.setItems(items);return invoices.post(request);
    }
    public String evaluation(SrmEvaluationPayload payload){
        String lifnr=refs.vendor(payload.getSupplier());
        String period=payload.getEvaluationPeriod()==null?"":payload.getEvaluationPeriod();
        jdbc.update("DELETE FROM sap_vendor_evaluation WHERE lifnr=? AND period=?",lifnr,period);
        jdbc.update("INSERT INTO sap_vendor_evaluation(lifnr,period,score,grade) VALUES(?,?,?,?)",lifnr,period,payload.getScore(),payload.getGrade());
        Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM sap_vendor_evaluation",Integer.class);
        return "EV"+String.format("%08d",count==null?1:count);
    }
}
