package com.sap.sd.service;

import com.sap.common.NumberRangeService;
import com.sap.fi.service.AccountingDocumentService;
import com.sap.integration.service.BmsPushService;
import com.sap.mm.service.ReferenceDataService;
import com.sap.mm.service.StockService;
import com.sap.sd.dto.DnCreateRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class DeliveryService {
    private final JdbcTemplate jdbc; private final NumberRangeService numbers;
    private final ReferenceDataService refs; private final StockService stock;
    private final AccountingDocumentService accounting; private final BmsPushService bms;
    public DeliveryService(JdbcTemplate jdbc,NumberRangeService numbers,ReferenceDataService refs,StockService stock,
                           AccountingDocumentService accounting,BmsPushService bms){
        this.jdbc=jdbc;this.numbers=numbers;this.refs=refs;this.stock=stock;this.accounting=accounting;this.bms=bms;
    }
    @Transactional
    public Map<String,Object> create(DnCreateRequest request){
        Map<String,Object> order=jdbc.queryForMap("SELECT * FROM sap_sales_order WHERE vbeln=?",request.getSoVbeln());
        String dn=numbers.next("DN");
        jdbc.update("INSERT INTO sap_delivery(vbeln,so_vbeln,kunnr,werks,status,bms_synced) VALUES(?,?,?,?,?,0)",
                dn,request.getSoVbeln(),order.get("kunnr"),value(request.getWerks(),"1000"),"OPEN");
        List<Map<String,Object>> lines=jdbc.queryForList("SELECT * FROM sap_sales_order_item WHERE vbeln=?",request.getSoVbeln());
        int i=0; for(Map<String,Object> line:lines){
            BigDecimal qty=StockService.decimal(line,"kwmeng").subtract(StockService.decimal(line,"delivered_qty"));
            if(request.getItems()!=null&&!request.getItems().isEmpty()&&i<request.getItems().size()&&request.getItems().get(i).getQty()!=null)
                qty=request.getItems().get(i).getQty();
            jdbc.update("INSERT INTO sap_delivery_item(vbeln,posnr,matnr,qty,picked_qty,pgi_qty) VALUES(?,?,?, ?,0,0)",
                    dn,line.get("posnr"),line.get("matnr"),qty); i++;
        }
        return jdbc.queryForMap("SELECT * FROM sap_delivery WHERE vbeln=?",dn);
    }
    @Transactional public Map<String,Object> pick(String id){
        jdbc.update("UPDATE sap_delivery SET status='PICKED' WHERE vbeln=?",id);
        jdbc.update("UPDATE sap_delivery_item SET picked_qty=qty WHERE vbeln=?",id);
        return jdbc.queryForMap("SELECT * FROM sap_delivery WHERE vbeln=?",id);
    }
    @Transactional public Map<String,Object> pgi(String id){
        Map<String,Object> dn=jdbc.queryForMap("SELECT * FROM sap_delivery WHERE vbeln=?",id);
        List<Map<String,Object>> lines=jdbc.queryForList("SELECT * FROM sap_delivery_item WHERE vbeln=?",id);
        BigDecimal total=BigDecimal.ZERO;
        for(Map<String,Object> line:lines){
            BigDecimal qty=StockService.decimal(line,"qty");
            BigDecimal price=jdbc.queryForObject("SELECT std_price FROM sap_material WHERE matnr=?",BigDecimal.class,line.get("matnr"));
            stock.change(String.valueOf(line.get("matnr")),String.valueOf(dn.get("werks")),"0002",qty.negate(),qty.multiply(price).negate());
            total=total.add(qty.multiply(price));
        }
        String fi=accounting.post("WE","SD",id,Arrays.asList(
                new AccountingDocumentService.FiLine("6402","S",total,null,null,null,"PGI成本"),
                new AccountingDocumentService.FiLine(refs.stockAccount(String.valueOf(lines.get(0).get("matnr"))),"H",total,null,null,null,"PGI库存")));
        String mblnr=numbers.next("MATERIAL");
        jdbc.update("INSERT INTO sap_material_document(mblnr,mjahr,budat,bwart,ref_type,ref_no,fi_belnr) VALUES(?,?,CURRENT_DATE,'601','SO',?,?)",
                mblnr,String.valueOf(LocalDate.now().getYear()),id,fi);
        jdbc.update("UPDATE sap_delivery SET status='PGI',material_doc=? WHERE vbeln=?",mblnr,id);
        jdbc.update("UPDATE sap_delivery_item SET pgi_qty=qty WHERE vbeln=?",id);
        Map<String,Object> payload=new HashMap<>(); payload.put("extRef",id);payload.put("bizType","OUTBOUND");
        payload.put("customerCode",refs.alias("sap_customer","kunnr",String.valueOf(dn.get("kunnr"))));
        payload.put("warehouseCode","WH-"+dn.get("werks"));payload.put("bizDate",LocalDate.now().toString());
        payload.put("orders",1);payload.put("lines",lines.size());payload.put("qty",total);
        Map<String,Object> pushed=bms.push("/api/open/oms/docs",payload);
        jdbc.update("UPDATE sap_delivery SET bms_synced=?,bms_doc_no=? WHERE vbeln=?",1,pushed.get("docNo"),id);
        return jdbc.queryForMap("SELECT * FROM sap_delivery WHERE vbeln=?",id);
    }
    private static String value(String value,String fallback){return value==null||value.trim().isEmpty()?fallback:value;}
}
