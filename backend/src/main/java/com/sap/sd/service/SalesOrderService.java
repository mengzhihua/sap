package com.sap.sd.service;

import com.sap.common.NumberRangeService;
import com.sap.mm.service.ReferenceDataService;
import com.sap.sd.dto.SoCreateRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class SalesOrderService {
    private final JdbcTemplate jdbc;
    private final NumberRangeService numbers;
    private final ReferenceDataService refs;
    public SalesOrderService(JdbcTemplate jdbc, NumberRangeService numbers, ReferenceDataService refs) {
        this.jdbc=jdbc; this.numbers=numbers; this.refs=refs;
    }
    @Transactional
    public Map<String,Object> create(SoCreateRequest request) {
        String vbeln=numbers.next("SO");
        String kunnr=refs.customer(request.getKunnr()==null?request.getCustomerCode():request.getKunnr());
        jdbc.update("INSERT INTO sap_sales_order(vbeln,auart,kunnr,vkorg,waers,status) VALUES(?,?,?,?,?,?)",
                vbeln,"OR",kunnr,value(request.getVkorg(),"1000"),value(request.getWaers(),"CNY"),"OPEN");
        int pos=10;
        for(SoCreateRequest.Item item:request.getItems()){
            String mat=refs.material(item.getMatnr());
            BigDecimal qty=item.getQty()==null?BigDecimal.ZERO:item.getQty();
            BigDecimal price=item.getPrice();
            if(price==null) price=jdbc.queryForObject("SELECT std_price FROM sap_material WHERE matnr=?",BigDecimal.class,mat);
            jdbc.update("INSERT INTO sap_sales_order_item(vbeln,posnr,matnr,werks,kwmeng,netpr,delivered_qty,billed_qty) VALUES(?,?,?,?,?,?,0,0)",
                    vbeln,String.valueOf(pos),mat,value(item.getWerks(),"1000"),qty,price);
            pos+=10;
        }
        return jdbc.queryForMap("SELECT * FROM sap_sales_order WHERE vbeln=?",vbeln);
    }
    public List<Map<String,Object>> list(){return jdbc.queryForList("SELECT * FROM sap_sales_order");}
    private static String value(String value,String fallback){return value==null||value.trim().isEmpty()?fallback:value;}
}
