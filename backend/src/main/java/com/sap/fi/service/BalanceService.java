package com.sap.fi.service;

import com.sap.fi.mapper.AccountingDocumentMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class BalanceService {
    private final AccountingDocumentMapper documents;

    public BalanceService(AccountingDocumentMapper documents) {
        this.documents = documents;
    }

    public Map<String, Object> summary(String gjahr) {
        return documents.balanceSummary(gjahr);
    }
}
