package com.sap.integration.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.common.BizException;
import com.sap.fi.service.AccountingDocumentService;
import com.sap.integration.dto.BmsStatementRequest;
import com.sap.integration.entity.BmsStatement;
import com.sap.integration.mapper.BmsStatementMapper;
import com.sap.mm.service.ReferenceDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

@Service
public class BmsStatementService {
    private final BmsStatementMapper statements;
    private final ReferenceDataService refs;
    private final AccountingDocumentService accounting;

    public BmsStatementService(BmsStatementMapper statements, ReferenceDataService refs,
                               AccountingDocumentService accounting) {
        this.statements = statements; this.refs = refs; this.accounting = accounting;
    }

    @Transactional
    public BmsStatement post(BmsStatementRequest request) {
        BmsStatement existing = findOrNull(request.getStatementNo());
        if (existing != null) return existing;
        BigDecimal tax = request.getTaxAmount() == null ? BigDecimal.ZERO : request.getTaxAmount();
        String fi;
        if ("AR".equalsIgnoreCase(request.getDirection())) {
            String customer = refs.customer(request.getPartnerCode());
            List<AccountingDocumentService.FiLine> lines = new ArrayList<>();
            lines.add(new AccountingDocumentService.FiLine("1122", "S", request.getAmount(), null, null, customer, "BMS应收"));
            lines.add(new AccountingDocumentService.FiLine("6001", "H", request.getAmount().subtract(tax), null, null, null, "BMS收入"));
            if (tax.signum() > 0) lines.add(new AccountingDocumentService.FiLine("2221", "H", tax, null, null, null, "BMS销项税"));
            fi = accounting.post("DR", "BMS", request.getStatementNo(), lines);
        } else {
            String vendor = refs.vendor(request.getPartnerCode());
            List<AccountingDocumentService.FiLine> lines = new ArrayList<>();
            lines.add(new AccountingDocumentService.FiLine("6601", "S", request.getAmount().subtract(tax), null, null, null, "BMS费用"));
            if (tax.signum() > 0) lines.add(new AccountingDocumentService.FiLine("2211", "S", tax, null, null, null, "BMS进项税"));
            lines.add(new AccountingDocumentService.FiLine("2201", "H", request.getAmount(), null, vendor, null, "BMS应付"));
            fi = accounting.post("KR", "BMS", request.getStatementNo(), lines);
        }
        BmsStatement statement = new BmsStatement();
        statement.setStatementNo(request.getStatementNo()); statement.setDirection(request.getDirection());
        statement.setPartnerCode(request.getPartnerCode()); statement.setAmount(request.getAmount());
        statement.setTaxAmount(tax); statement.setBizDate(request.getBizDate() == null
                ? LocalDate.now() : LocalDate.parse(request.getBizDate()));
        statement.setRemark(request.getRemark()); statement.setBelnr(fi); statements.insert(statement);
        return statement;
    }

    public BmsStatement find(String no) {
        BmsStatement statement = findOrNull(no);
        if (statement == null) throw new BizException("对账单不存在: " + no);
        return statement;
    }

    private BmsStatement findOrNull(String no) {
        return statements.selectOne(new LambdaQueryWrapper<BmsStatement>().eq(BmsStatement::getStatementNo, no));
    }
}
