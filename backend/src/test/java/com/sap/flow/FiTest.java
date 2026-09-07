package com.sap.flow;

import com.sap.TestSupport;
import com.sap.common.BizException;
import com.sap.fi.dto.FiDocumentRequest;
import com.sap.fi.dto.PaymentRequest;
import com.sap.fi.service.AccountingDocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class FiTest extends TestSupport {
    @Autowired
    private AccountingDocumentService service;

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
        Map<String, Object> payment = service.payment(request);
        assertNotNull(payment.get("belnr"));
    }

    @Test
    void reversalCreatesOppositeDocument() {
        FiDocumentRequest request = new FiDocumentRequest();
        FiDocumentRequest.Item debit = new FiDocumentRequest.Item();
        debit.setSaknr("6401"); debit.setShkzg("S"); debit.setAmount(new java.math.BigDecimal("10"));
        FiDocumentRequest.Item credit = new FiDocumentRequest.Item();
        credit.setSaknr("1002"); credit.setShkzg("H"); credit.setAmount(new java.math.BigDecimal("10"));
        request.setItems(Arrays.asList(debit, credit));
        Map<String, Object> original = service.post(request);
        assertNotNull(service.reverse(String.valueOf(original.get("belnr"))).get("belnr"));
    }
}
