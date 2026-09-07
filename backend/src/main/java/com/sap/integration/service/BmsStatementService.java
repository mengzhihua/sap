package com.sap.integration.service;
import com.sap.common.BizException;
import com.sap.fi.service.AccountingDocumentService;
import com.sap.mm.service.ReferenceDataService;
import com.sap.integration.dto.BmsStatementRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class BmsStatementService {
    private final JdbcTemplate jdbc; private final ReferenceDataService refs; private final AccountingDocumentService accounting;
    public BmsStatementService(JdbcTemplate jdbc,ReferenceDataService refs,AccountingDocumentService accounting){this.jdbc=jdbc;this.refs=refs;this.accounting=accounting;}
    @Transactional public Map<String,Object> post(BmsStatementRequest request){
        List<Map<String,Object>> existing=jdbc.queryForList("SELECT * FROM sap_bms_statement WHERE statement_no=?",request.getStatementNo());
        if(!existing.isEmpty()) return existing.get(0);
        BigDecimal tax=request.getTaxAmount()==null?BigDecimal.ZERO:request.getTaxAmount();
        String fi;
        if("AR".equalsIgnoreCase(request.getDirection())){
            String customer=refs.customer(request.getPartnerCode());
            fi=accounting.post("DR","BMS",request.getStatementNo(),Arrays.asList(
                    new AccountingDocumentService.FiLine("1122","S",request.getAmount(),null,null,customer,"BMS应收"),
                    new AccountingDocumentService.FiLine("6001","H",request.getAmount().subtract(tax),null,null,null,"BMS收入"),
                    new AccountingDocumentService.FiLine("2221","H",tax,null,null,null,"BMS销项税")));
        }else{
            String vendor=refs.vendor(request.getPartnerCode());
            fi=accounting.post("KR","BMS",request.getStatementNo(),Arrays.asList(
                    new AccountingDocumentService.FiLine("6601","S",request.getAmount().subtract(tax),null,null,null,"BMS费用"),
                    new AccountingDocumentService.FiLine("2211","S",tax,null,null,null,"BMS进项税"),
                    new AccountingDocumentService.FiLine("2201","H",request.getAmount(),null,vendor,null,"BMS应付")));
        }
        jdbc.update("INSERT INTO sap_bms_statement(statement_no,direction,partner_code,amount,tax_amount,biz_date,remark,belnr) VALUES(?,?,?,?,?,?,?,?)",
                request.getStatementNo(),request.getDirection(),request.getPartnerCode(),request.getAmount(),tax,
                request.getBizDate()==null?LocalDate.now().toString():request.getBizDate(),request.getRemark(),fi);
        return jdbc.queryForMap("SELECT * FROM sap_bms_statement WHERE statement_no=?",request.getStatementNo());
    }
    public Map<String,Object> find(String no){List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM sap_bms_statement WHERE statement_no=?",no);if(rows.isEmpty())throw new BizException("对账单不存在: "+no);return rows.get(0);}
}
