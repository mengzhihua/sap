package com.sap.flow;

import com.sap.TestSupport;
import com.sap.common.BizException;
import com.sap.fi.dto.FiDocumentRequest;
import com.sap.fi.dto.PaymentRequest;
import com.sap.fi.service.AccountingDocumentService;
import com.sap.fi.entity.PostingPeriod;
import com.sap.fi.mapper.PostingPeriodMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class FiTest extends TestSupport {
    @Autowired
    private AccountingDocumentService service;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PostingPeriodMapper postingPeriods;

    @Test
    void fb50RejectsUnbalancedDocument() {
        FiDocumentRequest body = new FiDocumentRequest();
        FiDocumentRequest.Item item = new FiDocumentRequest.Item();
        item.setSaknr("6401"); item.setShkzg("S"); item.setAmount(new java.math.BigDecimal("10"));
        body.setItems(Collections.singletonList(item));
        assertThrows(BizException.class, () -> service.post(body));
    }

    @Test
    void paymentCreatesBalancedDocument() {
        PaymentRequest request = new PaymentRequest();
        request.setType("AP"); request.setPartner("100010"); request.setAmount(new java.math.BigDecimal("10"));
        com.sap.fi.entity.AccountingDocument payment = service.payment(request);
        assertNotNull(payment.getBelnr());
    }

    @Test
    void paymentClearsPartnerOpenItem() {
        FiDocumentRequest source = new FiDocumentRequest();
        FiDocumentRequest.Item debit = new FiDocumentRequest.Item();
        debit.setSaknr("2201"); debit.setShkzg("S"); debit.setAmount(new java.math.BigDecimal("10")); debit.setLifnr("100010");
        FiDocumentRequest.Item credit = new FiDocumentRequest.Item();
        credit.setSaknr("6601"); credit.setShkzg("H"); credit.setAmount(new java.math.BigDecimal("10"));
        source.setItems(Arrays.asList(debit, credit));
        com.sap.fi.entity.AccountingDocument open = service.post(source);
        PaymentRequest payment = new PaymentRequest();
        payment.setType("AP"); payment.setPartner("100010"); payment.setAmount(new java.math.BigDecimal("10"));
        payment.setClearDocs(Collections.singletonList(open.getBelnr()));
        com.sap.fi.entity.AccountingDocument saved = service.payment(payment);
        assertEquals("KZ", saved.getBlart());
        assertEquals(saved.getBelnr(), jdbc.queryForObject(
                "SELECT cleared_by FROM sap_acc_document WHERE belnr=?", String.class, open.getBelnr()));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM sap_acc_document d JOIN sap_acc_document_item i ON d.belnr=i.belnr " +
                "WHERE d.belnr=? AND d.cleared_by IS NULL AND d.reversed_by IS NULL AND d.blart <> 'AB' AND i.saknr='2201'",
                Integer.class, open.getBelnr()));
    }

    @Test
    void reversalCreatesOppositeDocument() {
        FiDocumentRequest request = new FiDocumentRequest();
        FiDocumentRequest.Item debit = new FiDocumentRequest.Item();
        debit.setSaknr("6401"); debit.setShkzg("S"); debit.setAmount(new java.math.BigDecimal("10"));
        FiDocumentRequest.Item credit = new FiDocumentRequest.Item();
        credit.setSaknr("1002"); credit.setShkzg("H"); credit.setAmount(new java.math.BigDecimal("10"));
        request.setItems(Arrays.asList(debit, credit));
        com.sap.fi.entity.AccountingDocument original = service.post(request);
        assertNotNull(service.reverse(original.getBelnr()).getBelnr());
        assertThrows(BizException.class, () -> service.reverse(original.getBelnr()));
    }

    @Test
    void arPaymentUsesDz() {
        PaymentRequest request = new PaymentRequest();
        request.setType("AR"); request.setPartner("200010"); request.setAmount(new java.math.BigDecimal("10"));
        assertEquals("DZ", service.payment(request).getBlart());
    }

    @Test
    void ob52RejectsPostingOutsideOpenRange() {
        PostingPeriod period = new PostingPeriod();
        period.setBukrs("TEST"); period.setFiscalYear(2025);
        period.setFromPeriod(1); period.setToPeriod(3); period.setOpen(true);
        postingPeriods.insert(period);
        FiDocumentRequest request = new FiDocumentRequest();
        request.setBukrs("TEST"); request.setBudat(java.time.LocalDate.of(2025, 4, 1));
        FiDocumentRequest.Item debit = new FiDocumentRequest.Item();
        debit.setSaknr("6401"); debit.setShkzg("S"); debit.setAmount(new java.math.BigDecimal("10"));
        FiDocumentRequest.Item credit = new FiDocumentRequest.Item();
        credit.setSaknr("1002"); credit.setShkzg("H"); credit.setAmount(new java.math.BigDecimal("10"));
        request.setItems(Arrays.asList(debit, credit));
        BizException error = assertThrows(BizException.class, () -> service.post(request));
        assertTrue(error.getMessage().contains("期间已关闭"));
    }
}
