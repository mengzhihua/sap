package com.sap.mm.service;

import com.sap.common.NumberRangeService;
import com.sap.fi.service.AccountingDocumentService;
import com.sap.mm.dto.MiroRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class InvoiceVerificationService {
    private final JdbcTemplate jdbc;
    private final NumberRangeService numbers;
    private final ReferenceDataService refs;
    private final PurchaseOrderService purchaseOrders;
    private final AccountingDocumentService accounting;

    public InvoiceVerificationService(JdbcTemplate jdbc, NumberRangeService numbers, ReferenceDataService refs,
                                      PurchaseOrderService purchaseOrders, AccountingDocumentService accounting) {
        this.jdbc = jdbc;
        this.numbers = numbers;
        this.refs = refs;
        this.purchaseOrders = purchaseOrders;
        this.accounting = accounting;
    }

    @Transactional
    public Map<String, Object> post(MiroRequest request) {
        String lifnr = refs.vendor(request.getLifnr());
        String ebeln = request.getEbeln();
        if (ebeln == null && !request.getItems().isEmpty()) ebeln = request.getItems().get(0).getEbeln();
        Map<String, Object> po = purchaseOrders.find(ebeln);
        BigDecimal gross = request.getGrossAmount() == null ? BigDecimal.ZERO : request.getGrossAmount();
        BigDecimal net = BigDecimal.ZERO;
        boolean blocked = false;
        for (MiroRequest.MiroItemRequest item : request.getItems()) {
            Map<String, Object> pi = purchaseOrders.item(String.valueOf(po.get("ebeln")),
                    refs.maybeMaterial(item.getEbelp()), item.getEbelp());
            if (pi == null) throw new com.sap.common.BizException("发票采购订单行不存在: " + item.getEbelp());
            BigDecimal qty = item.getQty() == null ? BigDecimal.ZERO : item.getQty();
            BigDecimal price = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
            BigDecimal open = StockService.decimal(pi, "delivered_qty").subtract(StockService.decimal(pi, "invoiced_qty"));
            if (qty.compareTo(open) > 0) blocked = true;
            BigDecimal poPrice = StockService.decimal(pi, "netpr");
            if (poPrice.compareTo(BigDecimal.ZERO) > 0
                    && price.subtract(poPrice).abs().divide(poPrice, 6, RoundingMode.HALF_UP)
                    .compareTo(new BigDecimal("0.05")) > 0) blocked = true;
            net = net.add(qty.multiply(price));
        }
        if (gross.compareTo(BigDecimal.ZERO) <= 0) gross = net.multiply(new BigDecimal("1.13")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = gross.subtract(net).max(BigDecimal.ZERO);
        String belnr = numbers.next("INVOICE");
        String fiNo = null;
        if (!blocked) {
            fiNo = accounting.post("RE", "MM", belnr, Arrays.asList(
                    new AccountingDocumentService.FiLine("2202", "S", net, null, null, null, "GR/IR"),
                    new AccountingDocumentService.FiLine("2211", "S", tax, null, null, null, "进项税"),
                    new AccountingDocumentService.FiLine("2201", "H", gross, null, lifnr, null, "应付账款")));
        }
        jdbc.update("INSERT INTO sap_supplier_invoice(belnr,gjahr,lifnr,ebeln,external_invoice_no,gross_amount,tax_amount,status,fi_belnr,match_result) VALUES(?,?,?,?,?,?,?,?,?,?)",
                belnr, String.valueOf(LocalDate.now().getYear()), lifnr, po.get("ebeln"), request.getExternalInvoiceNo(),
                gross, tax, blocked ? "BLOCKED" : "POSTED", fiNo, blocked ? "BLOCKED" : "PASSED");
        for (MiroRequest.MiroItemRequest item : request.getItems()) {
            jdbc.update("INSERT INTO sap_supplier_invoice_item(belnr,ebelp,qty,price) VALUES(?,?,?,?)",
                    belnr, item.getEbelp(), item.getQty(), item.getPrice());
        }
        return jdbc.queryForMap("SELECT * FROM sap_supplier_invoice WHERE belnr=?", belnr);
    }
}
