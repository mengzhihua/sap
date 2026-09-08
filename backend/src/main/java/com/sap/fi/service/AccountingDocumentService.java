package com.sap.fi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.co.entity.CoDocument;
import com.sap.co.mapper.CoDocumentMapper;
import com.sap.common.BizException;
import com.sap.common.NumberRangeService;
import com.sap.fi.dto.FiDocumentRequest;
import com.sap.fi.dto.PaymentRequest;
import com.sap.fi.entity.AccountingDocument;
import com.sap.fi.entity.AccountingDocumentItem;
import com.sap.fi.entity.Payment;
import com.sap.fi.mapper.AccountingDocumentItemMapper;
import com.sap.fi.mapper.AccountingDocumentMapper;
import com.sap.fi.mapper.PaymentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountingDocumentService {
    private final AccountingDocumentMapper documents;
    private final AccountingDocumentItemMapper items;
    private final PaymentMapper payments;
    private final CoDocumentMapper coDocuments;
    private final NumberRangeService numbers;
    private final PostingPeriodService postingPeriods;

    public AccountingDocumentService(AccountingDocumentMapper documents,
                                     AccountingDocumentItemMapper items,
                                     PaymentMapper payments,
                                     CoDocumentMapper coDocuments,
                                     NumberRangeService numbers, PostingPeriodService postingPeriods) {
        this.documents = documents;
        this.items = items;
        this.payments = payments;
        this.coDocuments = coDocuments;
        this.numbers = numbers;
        this.postingPeriods = postingPeriods;
    }

    @Transactional
    public AccountingDocument post(FiDocumentRequest request) {
        List<FiLine> lines = new ArrayList<>();
        for (FiDocumentRequest.Item item : request.getItems()) {
            lines.add(new FiLine(item.getSaknr(), item.getShkzg(), item.getAmount(), item.getKostl(),
                    item.getLifnr(), item.getKunnr(), item.getText()));
        }
        LocalDate postingDate = request.getBudat() == null ? LocalDate.now() : request.getBudat();
        LocalDate documentDate = request.getBldat() == null ? postingDate : request.getBldat();
        return document(request.getBlart(), request.getSource(), request.getRefNo(), lines,
                request.getBukrs(), request.getWaers(), postingDate, documentDate, request.getHeaderText());
    }

    @Transactional
    public AccountingDocument payment(PaymentRequest request) {
        String type = request.getType();
        boolean ap = "AP".equalsIgnoreCase(type);
        String partner = request.getPartner();
        List<String> clearDocs = request.getClearDocs() == null
                ? new ArrayList<>() : request.getClearDocs();
        validateClearDocs(clearDocs, ap, partner);

        List<FiLine> lines;
        if (ap) {
            lines = Arrays.asList(
                    new FiLine("2201", "S", request.getAmount(), null, partner, null, "付款"),
                    new FiLine("1002", "H", request.getAmount(), null, null, null, "银行付款"));
        } else {
            lines = Arrays.asList(
                    new FiLine("1002", "S", request.getAmount(), null, null, null, "银行收款"),
                    new FiLine("1122", "H", request.getAmount(), null, null, partner, "收款"));
        }
        AccountingDocument document = document(ap ? "KZ" : "DZ", "FI", partner, lines);
        Payment payment = new Payment();
        payment.setType(type);
        payment.setPartner(partner);
        payment.setAmount(request.getAmount());
        payment.setBelnr(document.getBelnr());
        payment.setClearedDocs(clearDocs.isEmpty() ? null : String.join(",", clearDocs));
        payments.insert(payment);
        for (String clearDoc : clearDocs) {
            AccountingDocument openItem = documents.selectOne(new LambdaQueryWrapper<AccountingDocument>()
                    .eq(AccountingDocument::getBelnr, clearDoc));
            openItem.setClearedBy(document.getBelnr());
            documents.updateById(openItem);
        }
        return document;
    }

    @Transactional
    public AccountingDocument reverse(String belnr) {
        AccountingDocument original = documents.selectOne(new LambdaQueryWrapper<AccountingDocument>()
                .eq(AccountingDocument::getBelnr, belnr));
        if (original == null) throw new BizException("会计凭证不存在: " + belnr);
        if ("AB".equalsIgnoreCase(original.getBlart()) || original.getReversedBy() != null) {
            throw new BizException("会计凭证已冲销: " + belnr);
        }
        List<AccountingDocumentItem> originalItems = items.selectList(new LambdaQueryWrapper<AccountingDocumentItem>()
                .eq(AccountingDocumentItem::getBelnr, belnr));
        List<FiLine> lines = originalItems.stream()
                .map(item -> new FiLine(item.getSaknr(), "S".equals(item.getShkzg()) ? "H" : "S",
                        item.getAmount(), item.getKostl(), item.getLifnr(), item.getKunnr(), "冲销"))
                .collect(Collectors.toList());
        AccountingDocument reversal = document("AB", "FI", belnr, lines);
        original.setReversedBy(reversal.getBelnr());
        documents.updateById(original);
        return reversal;
    }

    @Transactional
    public String post(String blart, String source, String refNo, List<FiLine> lines) {
        return document(blart, source, refNo, lines).getBelnr();
    }

    public AccountingDocument find(String belnr) {
        AccountingDocument document = documents.selectOne(new LambdaQueryWrapper<AccountingDocument>()
                .eq(AccountingDocument::getBelnr, belnr));
        if (document == null) throw new BizException("会计凭证不存在: " + belnr);
        return load(document);
    }

    private AccountingDocument document(String blart, String source, String refNo, List<FiLine> lines) {
        return document(blart, source, refNo, lines, "1000", "CNY", LocalDate.now(), LocalDate.now(), source);
    }

    private AccountingDocument document(String blart, String source, String refNo, List<FiLine> lines,
                                          String bukrs, String waers, LocalDate postingDate,
                                          LocalDate documentDate, String headerText) {
        bukrs = bukrs == null || bukrs.trim().isEmpty() ? "1000" : bukrs;
        waers = waers == null || waers.trim().isEmpty() ? "CNY" : waers;
        postingPeriods.assertOpen(bukrs, postingDate);
        if (lines.size() < 2) throw new BizException("会计凭证至少需要两行");
        BigDecimal debit = BigDecimal.ZERO;
        BigDecimal credit = BigDecimal.ZERO;
        for (FiLine line : lines) {
            if (line.amount.compareTo(BigDecimal.ZERO) <= 0) throw new BizException("凭证金额必须大于0");
            if ("S".equalsIgnoreCase(line.shkzg)) debit = debit.add(line.amount);
            else if ("H".equalsIgnoreCase(line.shkzg)) credit = credit.add(line.amount);
            else throw new BizException("借贷标识必须为 S/H");
        }
        if (debit.subtract(credit).abs().compareTo(new BigDecimal("0.005")) > 0) {
            throw new BizException("借贷不平衡: 借方=" + debit + ",贷方=" + credit);
        }
        String belnr = numbers.next("FI");
        AccountingDocument document = new AccountingDocument();
        document.setBelnr(belnr);
        document.setGjahr(String.valueOf(postingDate.getYear()));
        document.setBukrs(bukrs);
        document.setBlart(blart);
        document.setBudat(postingDate);
        document.setBldat(documentDate);
        document.setWaers(waers);
        document.setHeaderText(headerText == null || headerText.trim().isEmpty() ? source : headerText);
        document.setRefNo(refNo);
        document.setSource(source);
        documents.insert(document);
        int lineNo = 1;
        for (FiLine line : lines) {
            AccountingDocumentItem item = new AccountingDocumentItem();
            item.setBelnr(belnr);
            item.setBuzei(String.valueOf(lineNo++));
            item.setBschl("S".equalsIgnoreCase(line.shkzg) ? "40" : "50");
            item.setShkzg(line.shkzg);
            item.setSaknr(line.saknr);
            item.setLifnr(line.lifnr);
            item.setKunnr(line.kunnr);
            item.setKostl(line.kostl);
            item.setAmount(line.amount);
            item.setItemText(line.text);
            items.insert(item);
            if (line.kostl != null && !line.kostl.trim().isEmpty()) {
                CoDocument co = new CoDocument();
                co.setFiBelnr(belnr);
                co.setKostl(line.kostl);
                co.setCostElement(line.saknr);
                co.setAmount(line.amount);
                co.setBudat(postingDate);
                co.setDocumentText(line.text);
                coDocuments.insert(co);
            }
        }
        return load(document);
    }

    private AccountingDocument load(AccountingDocument document) {
        document.setItems(items.selectList(new LambdaQueryWrapper<AccountingDocumentItem>()
                .eq(AccountingDocumentItem::getBelnr, document.getBelnr())));
        return document;
    }

    private void validateClearDocs(List<String> clearDocs, boolean ap, String partner) {
        for (String belnr : clearDocs) {
            AccountingDocument document = documents.selectOne(new LambdaQueryWrapper<AccountingDocument>()
                    .eq(AccountingDocument::getBelnr, belnr));
            if (document == null || "AB".equalsIgnoreCase(document.getBlart())
                    || document.getReversedBy() != null || document.getClearedBy() != null) {
                throw new BizException("待清账凭证不可用: " + belnr);
            }
            String account = ap ? "2201" : "1122";
            AccountingDocumentItem item = items.selectOne(new LambdaQueryWrapper<AccountingDocumentItem>()
                    .eq(AccountingDocumentItem::getBelnr, belnr)
                    .eq(AccountingDocumentItem::getSaknr, account)
                    .eq(ap ? AccountingDocumentItem::getLifnr : AccountingDocumentItem::getKunnr, partner));
            if (item == null) throw new BizException("待清账凭证不属于业务伙伴: " + belnr);
        }
    }

    public static final class FiLine {
        public final String saknr;
        public final String shkzg;
        public final BigDecimal amount;
        public final String kostl;
        public final String lifnr;
        public final String kunnr;
        public final String text;

        public FiLine(String saknr, String shkzg, BigDecimal amount, String kostl,
                      String lifnr, String kunnr, String text) {
            this.saknr = saknr;
            this.shkzg = shkzg;
            this.amount = amount == null ? BigDecimal.ZERO : amount;
            this.kostl = kostl;
            this.lifnr = lifnr;
            this.kunnr = kunnr;
            this.text = text;
        }
    }
}
