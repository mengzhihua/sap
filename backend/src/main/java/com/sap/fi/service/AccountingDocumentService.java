package com.sap.fi.service;

import com.sap.common.BizException;
import com.sap.common.NumberRangeService;
import com.sap.fi.dto.FiDocumentRequest;
import com.sap.fi.dto.PaymentRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class AccountingDocumentService {
    private final JdbcTemplate jdbc;
    private final NumberRangeService numbers;

    public AccountingDocumentService(JdbcTemplate jdbc, NumberRangeService numbers) {
        this.jdbc = jdbc;
        this.numbers = numbers;
    }

    @Transactional
    public Map<String, Object> post(FiDocumentRequest request) {
        List<FiLine> lines = new ArrayList<>();
        for (FiDocumentRequest.Item item : request.getItems()) {
            lines.add(new FiLine(item.getSaknr(), item.getShkzg(), item.getAmount(), item.getKostl(),
                    item.getLifnr(), item.getKunnr(), item.getText()));
        }
        return document(request.getBlart(), request.getSource(), null, lines);
    }

    @Transactional
    public Map<String, Object> payment(PaymentRequest request) {
        String partner = request.getPartner();
        String type = request.getType();
        List<FiLine> lines;
        if ("AP".equalsIgnoreCase(type)) {
            lines = Arrays.asList(new FiLine("2201", "S", request.getAmount(), null, partner, null, "付款"),
                    new FiLine("1002", "H", request.getAmount(), null, null, null, "银行付款"));
        } else {
            lines = Arrays.asList(new FiLine("1002", "S", request.getAmount(), null, null, null, "银行收款"),
                    new FiLine("1122", "H", request.getAmount(), null, null, partner, "收款"));
        }
        Map<String, Object> doc = document("KZ", "FI", partner, lines);
        jdbc.update("INSERT INTO sap_payment(type,partner,amount,belnr,cleared_docs) VALUES(?,?,?,?,?)",
                type, partner, request.getAmount(), doc.get("belnr"), null);
        return doc;
    }

    @Transactional
    public Map<String, Object> reverse(String belnr) {
        Map<String, Object> original = jdbc.queryForMap("SELECT * FROM sap_acc_document WHERE belnr=?", belnr);
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM sap_acc_document_item WHERE belnr=?", belnr);
        List<FiLine> lines = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String shkzg = "S".equals(row.get("shkzg")) ? "H" : "S";
            lines.add(new FiLine(String.valueOf(row.get("saknr")), shkzg,
                    new BigDecimal(String.valueOf(row.get("amount"))),
                    value(row, "kostl"), value(row, "lifnr"), value(row, "kunnr"), "冲销"));
        }
        Map<String, Object> reversal = document("AB", "FI", belnr, lines);
        jdbc.update("UPDATE sap_acc_document SET reversed_by=? WHERE belnr=?", reversal.get("belnr"), belnr);
        return reversal;
    }

    @Transactional
    public String post(String blart, String source, String refNo, List<FiLine> lines) {
        return String.valueOf(document(blart, source, refNo, lines).get("belnr"));
    }

    private Map<String, Object> document(String blart, String source, String refNo, List<FiLine> lines) {
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
        jdbc.update("INSERT INTO sap_acc_document(belnr,gjahr,bukrs,blart,budat,bldat,waers,header_text,ref_no,source) VALUES(?,?,?, ?,CURRENT_DATE,CURRENT_DATE,'CNY',?,?,?)",
                belnr, String.valueOf(LocalDate.now().getYear()), "1000", blart, source, refNo, source);
        int i = 1;
        for (FiLine line : lines) {
            jdbc.update("INSERT INTO sap_acc_document_item(belnr,buzei,bschl,shkzg,saknr,lifnr,kunnr,kostl,amount,text) VALUES(?,?,?,?,?,?,?,?,?,?)",
                    belnr, String.valueOf(i++), "S".equalsIgnoreCase(line.shkzg) ? "40" : "50",
                    line.shkzg, line.saknr, line.lifnr, line.kunnr, line.kostl, line.amount, line.text);
            if (line.kostl != null && !line.kostl.trim().isEmpty()) {
                jdbc.update("INSERT INTO sap_co_document(fi_belnr,kostl,cost_element,amount,budat,text) VALUES(?,?,?, ?,CURRENT_DATE,?)",
                        belnr, line.kostl, line.saknr, line.amount, line.text);
            }
        }
        return jdbc.queryForMap("SELECT * FROM sap_acc_document WHERE belnr=?", belnr);
    }

    private static String value(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : String.valueOf(value);
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
