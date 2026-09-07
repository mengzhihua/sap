package com.sap.pp.service;
import com.sap.common.NumberRangeService;
import com.sap.fi.service.AccountingDocumentService;
import com.sap.mm.service.ReferenceDataService;
import com.sap.mm.service.StockService;
import com.sap.pp.dto.ProductionOrderRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class ProductionOrderService {
    private final JdbcTemplate jdbc; private final NumberRangeService numbers; private final ReferenceDataService refs;
    private final StockService stock; private final AccountingDocumentService accounting;
    public ProductionOrderService(JdbcTemplate jdbc,NumberRangeService numbers,ReferenceDataService refs,StockService stock,AccountingDocumentService accounting){
        this.jdbc=jdbc;this.numbers=numbers;this.refs=refs;this.stock=stock;this.accounting=accounting;
    }
    @Transactional public Map<String,Object> create(ProductionOrderRequest request){
        String aufnr=numbers.next("PRODORD"), mat=refs.material(request.getMatnr()), werks=refs.plant(request.getWerks());
        BigDecimal target=request.getTargetQty(), planned=BigDecimal.ZERO;
        jdbc.update("INSERT INTO sap_production_order(aufnr,matnr,werks,target_qty,delivered_qty,status,planned_cost,actual_cost) VALUES(?,?,?,?,0,?,?,0)",
                aufnr,mat,werks,target,"CRTD",BigDecimal.ZERO);
        for(Map<String,Object> item:jdbc.queryForList("SELECT * FROM sap_bom_item WHERE matnr=? AND werks=?",mat,werks)){
            BigDecimal req=StockService.decimal(item,"qty").multiply(target);
            BigDecimal price=jdbc.queryForObject("SELECT std_price FROM sap_material WHERE matnr=?",BigDecimal.class,item.get("component"));
            planned=planned.add(req.multiply(price));
            jdbc.update("INSERT INTO sap_production_order_component(aufnr,matnr,req_qty,issued_qty) VALUES(?,?,?,0)",aufnr,item.get("component"),req);
        }
        jdbc.update("UPDATE sap_production_order SET planned_cost=? WHERE aufnr=?",planned,aufnr);
        return one(aufnr);
    }
    @Transactional public Map<String,Object> release(String id){jdbc.update("UPDATE sap_production_order SET status='REL' WHERE aufnr=?",id);return one(id);}
    @Transactional public Map<String,Object> issue(String id,String kostl){
        Map<String,Object> order=one(id); BigDecimal total=BigDecimal.ZERO;
        for(Map<String,Object> item:jdbc.queryForList("SELECT * FROM sap_production_order_component WHERE aufnr=?",id)){
            BigDecimal qty=StockService.decimal(item,"req_qty").subtract(StockService.decimal(item,"issued_qty"));
            BigDecimal price=jdbc.queryForObject("SELECT std_price FROM sap_material WHERE matnr=?",BigDecimal.class,item.get("matnr"));
            stock.change(String.valueOf(item.get("matnr")),String.valueOf(order.get("werks")),"0001",qty.negate(),qty.multiply(price).negate());
            accounting.post("WE","MM",id,Arrays.asList(new AccountingDocumentService.FiLine("5001","S",qty.multiply(price),kostl,null,null,"发料"),
                    new AccountingDocumentService.FiLine("1411","H",qty.multiply(price),null,null,null,"库存减少")));
            total=total.add(qty.multiply(price));
            jdbc.update("UPDATE sap_production_order_component SET issued_qty=req_qty WHERE id=?",item.get("id"));
        }
        jdbc.update("UPDATE sap_production_order SET actual_cost=actual_cost+? WHERE aufnr=?",total,id); return one(id);
    }
    @Transactional public Map<String,Object> confirm(String id,BigDecimal qty){
        jdbc.update("INSERT INTO sap_confirmation(aufnr,qty,budat) VALUES(?,?,CURRENT_DATE)",id,qty);
        jdbc.update("UPDATE sap_production_order SET status='CNF' WHERE aufnr=?",id);
        return jdbc.queryForMap("SELECT * FROM sap_confirmation WHERE id=IDENTITY()");
    }
    @Transactional public Map<String,Object> receipt(String id,BigDecimal qty){
        Map<String,Object> order=one(id); String mat=String.valueOf(order.get("matnr"));
        BigDecimal price=jdbc.queryForObject("SELECT std_price FROM sap_material WHERE matnr=?",BigDecimal.class,mat), amount=qty.multiply(price);
        stock.change(mat,String.valueOf(order.get("werks")),"0002",qty,amount);
        String fi=accounting.post("WE","PP",id,Arrays.asList(new AccountingDocumentService.FiLine("1405","S",amount,null,null,null,"完工入库"),
                new AccountingDocumentService.FiLine("5001","H",amount,null,null,null,"生产成本")));
        String mblnr=numbers.next("MATERIAL");
        jdbc.update("INSERT INTO sap_material_document(mblnr,mjahr,budat,bwart,ref_type,ref_no,fi_belnr) VALUES(?,?,CURRENT_DATE,'101','PRODORD',?,?)",
                mblnr,String.valueOf(LocalDate.now().getYear()),id,fi);
        jdbc.update("UPDATE sap_production_order SET delivered_qty=delivered_qty+?,status='CNF' WHERE aufnr=?",qty,id);
        return jdbc.queryForMap("SELECT * FROM sap_material_document WHERE mblnr=?",mblnr);
    }
    @Transactional public Map<String,Object> teco(String id){jdbc.update("UPDATE sap_production_order SET status='TECO' WHERE aufnr=?",id);return one(id);}
    private Map<String,Object> one(String id){return jdbc.queryForMap("SELECT * FROM sap_production_order WHERE aufnr=?",id);}
}
