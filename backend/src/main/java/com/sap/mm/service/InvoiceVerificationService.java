package com.sap.mm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.common.BizException;
import com.sap.common.NumberRangeService;
import com.sap.fi.service.AccountingDocumentService;
import com.sap.mm.dto.MiroRequest;
import com.sap.mm.entity.*;
import com.sap.mm.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class InvoiceVerificationService {
    private final SupplierInvoiceMapper invoices;
    private final SupplierInvoiceItemMapper invoiceItems;
    private final GrIrMapper grIr;
    private final PurchaseOrderService purchaseOrders;
    private final ReferenceDataService refs;
    private final NumberRangeService numbers;
    private final AccountingDocumentService accounting;

    public InvoiceVerificationService(SupplierInvoiceMapper invoices, SupplierInvoiceItemMapper invoiceItems,
                                      GrIrMapper grIr, PurchaseOrderService purchaseOrders,
                                      ReferenceDataService refs, NumberRangeService numbers,
                                      AccountingDocumentService accounting) {
        this.invoices = invoices;
        this.invoiceItems = invoiceItems;
        this.grIr = grIr;
        this.purchaseOrders = purchaseOrders;
        this.refs = refs;
        this.numbers = numbers;
        this.accounting = accounting;
    }

    @Transactional
    public SupplierInvoice post(MiroRequest request) {
        String lifnr = refs.vendor(request.getLifnr());
        String ebeln = request.getEbeln();
        if (ebeln == null && !request.getItems().isEmpty()) ebeln = request.getItems().get(0).getEbeln();
        if (ebeln == null) throw new BizException("采购订单不能为空");
        PurchaseOrder order = purchaseOrders.find(ebeln);

        String belnr = numbers.next("INVOICE");
        SupplierInvoice invoice = new SupplierInvoice();
        invoice.setBelnr(belnr);
        invoice.setGjahr(String.valueOf(LocalDate.now().getYear()));
        invoice.setLifnr(lifnr);
        invoice.setEbeln(order.getEbeln());
        invoice.setExternalInvoiceNo(request.getExternalInvoiceNo());
        invoice.setTaxAmount(BigDecimal.ZERO);

        boolean blocked = false;
        BigDecimal net = BigDecimal.ZERO;
        List<SupplierInvoiceItem> savedItems = new ArrayList<>();
        for (MiroRequest.MiroItemRequest line : request.getItems()) {
            PurchaseOrderItem poItem = purchaseOrders.item(order.getEbeln(), null, line.getEbelp());
            if (poItem == null) throw new BizException("采购订单行不存在: " + line.getEbelp());
            BigDecimal qty = zero(line.getQty());
            BigDecimal price = zero(line.getPrice());
            BigDecimal gr = grIrValue(order.getEbeln(), poItem.getEbelp(), true);
            BigDecimal ir = grIrValue(order.getEbeln(), poItem.getEbelp(), false);
            if (qty.compareTo(gr.subtract(ir)) > 0) blocked = true;
            BigDecimal poPrice = zero(poItem.getNetpr());
            if (poPrice.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal variance = price.subtract(poPrice).abs()
                        .divide(poPrice, 8, RoundingMode.HALF_UP);
                if (variance.compareTo(new BigDecimal("0.05")) > 0) blocked = true;
            }
            SupplierInvoiceItem item = new SupplierInvoiceItem();
            item.setBelnr(belnr); item.setEbelp(poItem.getEbelp()); item.setQty(qty); item.setPrice(price);
            invoiceItems.insert(item);
            savedItems.add(item);
            net = net.add(qty.multiply(price));
        }
        BigDecimal gross = request.getGrossAmount() == null ? net : request.getGrossAmount();
        invoice.setGrossAmount(gross);
        invoice.setItems(savedItems);
        if (blocked) {
            invoice.setStatus("BLOCKED");
            invoice.setMatchResult("三方匹配失败: 数量或价格超差");
            invoices.insert(invoice);
            return invoice;
        }

        BigDecimal tax = gross.subtract(net).max(BigDecimal.ZERO);
        invoice.setTaxAmount(tax);
        List<AccountingDocumentService.FiLine> fiLines = new ArrayList<>();
        fiLines.add(new AccountingDocumentService.FiLine("1405", "S", net, null, null, null, "采购发票"));
        if (tax.signum() > 0) fiLines.add(new AccountingDocumentService.FiLine("2221", "S", tax, null, null, null, "进项税"));
        fiLines.add(new AccountingDocumentService.FiLine("2201", "H", gross, null, lifnr, null, "应付账款"));
        String fi = accounting.post("KR", "MM", belnr, fiLines);
        invoice.setStatus("POSTED");
        invoice.setFiBelnr(fi);
        invoice.setMatchResult("三方匹配通过");
        invoices.insert(invoice);
        for (SupplierInvoiceItem item : savedItems) {
            GrIr row = grIr.selectOne(new LambdaQueryWrapper<GrIr>()
                    .eq(GrIr::getEbeln, order.getEbeln()).eq(GrIr::getEbelp, item.getEbelp()));
            if (row != null) {
                row.setIrQty(zero(row.getIrQty()).add(item.getQty()));
                row.setIrAmount(zero(row.getIrAmount()).add(item.getQty().multiply(item.getPrice())));
                grIr.update(row, new LambdaQueryWrapper<GrIr>()
                        .eq(GrIr::getEbeln, row.getEbeln()).eq(GrIr::getEbelp, row.getEbelp()));
            }
        }
        return invoice;
    }

    public SupplierInvoice find(String belnr) {
        SupplierInvoice invoice = invoices.selectById(belnr);
        if (invoice == null) throw new BizException("供应商发票不存在: " + belnr);
        invoice.setItems(invoiceItems.selectList(new LambdaQueryWrapper<SupplierInvoiceItem>()
                .eq(SupplierInvoiceItem::getBelnr, belnr)));
        return invoice;
    }

    private BigDecimal grIrValue(String ebeln, String ebelp, boolean gr) {
        GrIr row = grIr.selectOne(new LambdaQueryWrapper<GrIr>()
                .eq(GrIr::getEbeln, ebeln).eq(GrIr::getEbelp, ebelp));
        if (row == null) return BigDecimal.ZERO;
        return gr ? zero(row.getGrQty()) : zero(row.getIrQty());
    }

    private static BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
