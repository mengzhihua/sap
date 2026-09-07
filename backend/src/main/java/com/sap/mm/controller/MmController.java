package com.sap.mm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.common.PageResult;
import com.sap.common.R;
import com.sap.mm.dto.*;
import com.sap.mm.entity.*;
import com.sap.mm.mapper.*;
import com.sap.mm.service.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/mm")
@Validated
public class MmController {
    private final PurchaseOrderService pos;
    private final GoodsMovementService goods;
    private final InvoiceVerificationService invoices;
    private final MaterialMapper materials;
    private final VendorMapper vendors;
    private final MaterialDocumentMapper materialDocuments;
    private final MaterialDocumentItemMapper materialDocumentItems;
    private final StockMapper stocks;
    private final SupplierInvoiceMapper supplierInvoices;
    private final SupplierInvoiceItemMapper supplierInvoiceItems;
    private final GrIrMapper grIr;

    public MmController(PurchaseOrderService pos, GoodsMovementService goods, InvoiceVerificationService invoices,
                        MaterialMapper materials, VendorMapper vendors, MaterialDocumentMapper materialDocuments,
                        MaterialDocumentItemMapper materialDocumentItems, StockMapper stocks,
                        SupplierInvoiceMapper supplierInvoices, SupplierInvoiceItemMapper supplierInvoiceItems,
                        GrIrMapper grIr) {
        this.pos = pos; this.goods = goods; this.invoices = invoices; this.materials = materials;
        this.vendors = vendors; this.materialDocuments = materialDocuments;
        this.materialDocumentItems = materialDocumentItems; this.stocks = stocks;
        this.supplierInvoices = supplierInvoices; this.supplierInvoiceItems = supplierInvoiceItems; this.grIr = grIr;
    }

    @GetMapping("/materials")
    public R<PageResult<Material>> materials(@RequestParam(required = false) String q,
                                             @RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "20") long size) {
        LambdaQueryWrapper<Material> query = new LambdaQueryWrapper<>();
        if (q != null && !q.trim().isEmpty()) query.and(w -> w.like(Material::getMatnr, q)
                .or().like(Material::getAliasCode, q).or().like(Material::getMaktx, q));
        return R.ok(page(materials.selectList(query), page, size));
    }

    @GetMapping("/vendors")
    public R<PageResult<Vendor>> vendors(@RequestParam(required = false) String q,
                                         @RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "20") long size) {
        LambdaQueryWrapper<Vendor> query = new LambdaQueryWrapper<>();
        if (q != null && !q.trim().isEmpty()) query.and(w -> w.like(Vendor::getLifnr, q)
                .or().like(Vendor::getAliasCode, q).or().like(Vendor::getName, q));
        return R.ok(page(vendors.selectList(query), page, size));
    }

    @PostMapping("/po")
    public R<PurchaseOrder> po(@Valid @RequestBody PoCreateRequest request) { return R.ok(pos.create(request, "MANUAL")); }

    @GetMapping("/po")
    public R<List<PurchaseOrder>> pos() { return R.ok(pos.list()); }

    @GetMapping("/po/{id}")
    public R<PurchaseOrder> po(@PathVariable String id) { return R.ok(pos.one(id)); }

    @PostMapping("/po/{id}/close")
    public R<PurchaseOrder> close(@PathVariable String id) {
        PurchaseOrder order = pos.one(id); order.setStatus("CLOSED"); pos.updateStatus(order);
        return R.ok(pos.one(id));
    }

    @PostMapping("/migo")
    public R<MaterialDocument> migo(@Valid @RequestBody MigoRequest request) { return R.ok(goods.post(request)); }

    @GetMapping("/material-docs")
    public R<List<MaterialDocument>> docs() {
        List<MaterialDocument> result = materialDocuments.selectList(null);
        result.forEach(doc -> doc.setItems(materialDocumentItems.selectList(new LambdaQueryWrapper<MaterialDocumentItem>()
                .eq(MaterialDocumentItem::getMblnr, doc.getMblnr()))));
        return R.ok(result);
    }

    @GetMapping("/stock")
    public R<List<Stock>> stock(@RequestParam(required = false) String matnr,
                                @RequestParam(required = false) String werks) {
        LambdaQueryWrapper<Stock> query = new LambdaQueryWrapper<>();
        if (matnr != null) query.eq(Stock::getMatnr, matnr);
        if (werks != null) query.eq(Stock::getWerks, werks);
        return R.ok(stocks.selectList(query));
    }

    @PostMapping("/miro")
    public R<SupplierInvoice> miro(@Valid @RequestBody MiroRequest request) { return R.ok(invoices.post(request)); }

    @GetMapping("/supplier-invoices")
    public R<List<SupplierInvoice>> invoices() {
        List<SupplierInvoice> result = supplierInvoices.selectList(null);
        result.forEach(invoice -> invoice.setItems(supplierInvoiceItems.selectList(
                new LambdaQueryWrapper<SupplierInvoiceItem>().eq(SupplierInvoiceItem::getBelnr, invoice.getBelnr()))));
        return R.ok(result);
    }

    @GetMapping("/gr-ir")
    public R<List<GrIr>> grir() { return R.ok(grIr.selectList(null)); }

    private static <T> PageResult<T> page(List<T> all, long page, long size) {
        long from = Math.max(0, (page - 1) * size);
        long to = Math.min(all.size(), from + size);
        List<T> records = from >= all.size() ? java.util.Collections.emptyList() : all.subList((int) from, (int) to);
        return new PageResult<>(all.size(), page, size, records);
    }
}
