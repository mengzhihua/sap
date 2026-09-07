package com.sap.sd.service;
import com.sap.common.NumberRangeService;
import com.sap.fi.service.AccountingDocumentService;
import com.sap.mm.service.StockService;
import com.sap.sd.dto.BillingRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class BillingService {
    private final JdbcTemplate jdbc; private final NumberRangeService numbers; private final AccountingDocumentService accounting;
    public BillingService(JdbcTemplate jdbc,NumberRangeService numbers,AccountingDocumentService accounting){this.jdbc=jdbc;this.numbers=numbers;this.accounting=accounting;}
    @Transactional public Map<String,Object> create(BillingRequest request){
        Map<String,Object> dn=jdbc.queryForMap("SELECT * FROM sap_delivery WHERE vbeln=?",request.getDnVbeln());
        List<Map<String,Object>> lines=jdbc.queryForList("SELECT * FROM sap_delivery_item WHERE vbeln=?",request.getDnVbeln());
        BigDecimal net=BigDecimal.ZERO; for(Map<String,Object> line:lines){
            BigDecimal price=jdbc.queryForObject("SELECT std_price FROM sap_material WHERE matnr=?",BigDecimal.class,line.get("matnr"));
            net=net.add(StockService.decimal(line,"qty").multiply(price));
        }
        BigDecimal tax=net.multiply(new BigDecimal("0.13")).setScale(2,RoundingMode.HALF_UP);
        BigDecimal gross=net.add(tax);
        String vbeln=numbers.next("BILLING");
        String fi=accounting.post("DR","SD",vbeln,Arrays.asList(
                new AccountingDocumentService.FiLine("1122","S",gross,null,null,String.valueOf(dn.get("kunnr")),"应收"),
                new AccountingDocumentService.FiLine("6001","H",net,null,null,null,"收入"),
                new AccountingDocumentService.FiLine("2221","H",tax,null,null,null,"销项税")));
        jdbc.update("INSERT INTO sap_billing_doc(vbeln,fkart,kunnr,net,tax,gross,fi_belnr,status) VALUES(?,?,?,?,?,?,?,?)",
                vbeln,"F2",dn.get("kunnr"),net,tax,gross,fi,"POSTED");
        return jdbc.queryForMap("SELECT * FROM sap_billing_doc WHERE vbeln=?",vbeln);
    }
}
