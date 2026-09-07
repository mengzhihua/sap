package com.sap.fi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.common.PageResult;
import com.sap.common.R;
import com.sap.fi.dto.*;
import com.sap.fi.entity.*;
import com.sap.fi.mapper.*;
import com.sap.fi.service.AccountingDocumentService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fi")
public class FiController {
    private final JdbcTemplate jdbc;
    private final AccountingDocumentService accounting;
    private final GlAccountMapper accounts;
    private final AccountingDocumentMapper documents;
    private final AccountingDocumentItemMapper items;
    private final AccountDeterminationMapper determination;
    private final PaymentMapper payments;

    public FiController(JdbcTemplate jdbc, AccountingDocumentService accounting, GlAccountMapper accounts,
                        AccountingDocumentMapper documents, AccountingDocumentItemMapper items,
                        AccountDeterminationMapper determination, PaymentMapper payments) {
        this.jdbc = jdbc; this.accounting = accounting; this.accounts = accounts;
        this.documents = documents; this.items = items; this.determination = determination;
        this.payments = payments;
    }

    @GetMapping("/gl-accounts")
    public R<PageResult<GlAccount>> accounts(@RequestParam(required = false) String q,
                                             @RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "20") long size) {
        LambdaQueryWrapper<GlAccount> query = new LambdaQueryWrapper<>();
        if (q != null && !q.trim().isEmpty()) {
            query.like(GlAccount::getSaknr, q).or().like(GlAccount::getTxt, q);
        }
        List<GlAccount> all = accounts.selectList(query);
        return R.ok(page(all, page, size));
    }

    @PostMapping("/gl-accounts")
    public R<GlAccount> createAccount(@RequestBody GlAccount account) {
        accounts.insert(account);
        return R.ok(account);
    }

    @PutMapping("/gl-accounts/{saknr}")
    public R<GlAccount> updateAccount(@PathVariable String saknr, @RequestBody GlAccount input) {
        input.setSaknr(saknr);
        accounts.updateById(input);
        return R.ok(accounts.selectById(saknr));
    }

    @PostMapping("/documents")
    public R<AccountingDocument> post(@Valid @RequestBody FiDocumentRequest request) {
        return R.ok(accounting.post(request));
    }

    @GetMapping("/documents")
    public R<PageResult<AccountingDocument>> docs(@RequestParam(required = false) String blart,
                                                  @RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "20") long size) {
        LambdaQueryWrapper<AccountingDocument> query = new LambdaQueryWrapper<>();
        if (blart != null) query.eq(AccountingDocument::getBlart, blart);
        List<AccountingDocument> all = documents.selectList(query);
        all.forEach(this::loadItems);
        return R.ok(page(all, page, size));
    }

    @GetMapping("/documents/{id}")
    public R<AccountingDocument> doc(@PathVariable String id) {
        try { return R.ok(accounting.find(id)); }
        catch (RuntimeException ignored) {
            AccountingDocument document = documents.selectById(id);
            if (document == null) throw ignored;
            return R.ok(loadItems(document));
        }
    }

    @PostMapping("/documents/{id}/reverse")
    public R<AccountingDocument> reverse(@PathVariable String id) { return R.ok(accounting.reverse(id)); }

    @PostMapping("/payments")
    public R<AccountingDocument> payment(@Valid @RequestBody PaymentRequest request) {
        return R.ok(accounting.payment(request));
    }

    @GetMapping("/balances")
    public R<List<Map<String, Object>>> balances() {
        return R.ok(jdbc.queryForList("SELECT saknr,SUM(CASE WHEN shkzg='S' THEN amount ELSE -amount END) balance FROM sap_acc_document_item GROUP BY saknr"));
    }

    @GetMapping("/ap/open-items")
    public R<List<AccountingDocumentItem>> ap(@RequestParam(required = false) String lifnr) {
        return R.ok(openItems("2201", lifnr, true));
    }

    @GetMapping("/ar/open-items")
    public R<List<AccountingDocumentItem>> ar(@RequestParam(required = false) String kunnr) {
        return R.ok(openItems("1122", kunnr, false));
    }

    @GetMapping("/account-determination")
    public R<List<AccountDetermination>> determination() { return R.ok(determination.selectList(null)); }

    @PutMapping("/account-determination")
    public R<List<AccountDetermination>> updateDetermination(
            @RequestBody List<AccountDeterminationRequest> request) {
        List<AccountDetermination> result = new ArrayList<>();
        for (AccountDeterminationRequest input : request) {
            AccountDetermination value = new AccountDetermination();
            value.setAccountKey(input.getKey());
            value.setSaknr(input.getSaknr());
            if (determination.selectById(input.getKey()) == null) determination.insert(value);
            else determination.updateById(value);
            result.add(value);
        }
        return R.ok(result);
    }

    @GetMapping("/payments")
    public R<List<Payment>> payments() {
        return R.ok(payments.selectList(null));
    }

    private List<AccountingDocumentItem> openItems(String account, String partner, boolean ap) {
        List<AccountingDocument> all = documents.selectList(new LambdaQueryWrapper<AccountingDocument>()
                .isNull(AccountingDocument::getClearedBy)
                .isNull(AccountingDocument::getReversedBy)
                .ne(AccountingDocument::getBlart, "AB"));
        Set<String> belnrs = all.stream().map(AccountingDocument::getBelnr).collect(Collectors.toSet());
        LambdaQueryWrapper<AccountingDocumentItem> query = new LambdaQueryWrapper<AccountingDocumentItem>()
                .eq(AccountingDocumentItem::getSaknr, account).in(AccountingDocumentItem::getBelnr, belnrs);
        if (partner != null) query.eq(ap ? AccountingDocumentItem::getLifnr : AccountingDocumentItem::getKunnr, partner);
        return items.selectList(query);
    }

    private AccountingDocument loadItems(AccountingDocument document) {
        document.setItems(items.selectList(new LambdaQueryWrapper<AccountingDocumentItem>()
                .eq(AccountingDocumentItem::getBelnr, document.getBelnr())));
        return document;
    }

    private static <T> PageResult<T> page(List<T> all, long page, long size) {
        long from = Math.max(0, (page - 1) * size);
        long to = Math.min(all.size(), from + size);
        List<T> records = from >= all.size() ? java.util.Collections.emptyList() : all.subList((int) from, (int) to);
        return new PageResult<>(all.size(), page, size, records);
    }
}
