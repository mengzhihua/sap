package com.sap.mm.controller;

import com.sap.common.R;
import com.sap.mm.dto.*;
import com.sap.mm.service.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/mm")
@Validated
public class MmController {
    private final JdbcTemplate jdbc; private final PurchaseOrderService pos; private final GoodsMovementService goods; private final InvoiceVerificationService invoices;
    public MmController(JdbcTemplate jdbc,PurchaseOrderService pos,GoodsMovementService goods,InvoiceVerificationService invoices){this.jdbc=jdbc;this.pos=pos;this.goods=goods;this.invoices=invoices;}
    @GetMapping("/materials") public R<List<Map<String,Object>>> materials(@RequestParam(required=false)String q){String x=q==null?"%":"%"+q+"%";return R.ok(jdbc.queryForList("SELECT * FROM sap_material WHERE matnr LIKE ? OR alias_code LIKE ? OR maktx LIKE ? ORDER BY matnr",x,x,x));}
    @GetMapping("/vendors") public R<List<Map<String,Object>>> vendors(@RequestParam(required=false)String q){String x=q==null?"%":"%"+q+"%";return R.ok(jdbc.queryForList("SELECT * FROM sap_vendor WHERE lifnr LIKE ? OR alias_code LIKE ? OR name LIKE ? ORDER BY lifnr",x,x,x));}
    @PostMapping("/po") public R<Map<String,Object>> po(@Valid @RequestBody PoCreateRequest request){return R.ok(pos.create(request,"MANUAL"));}
    @GetMapping("/po") public R<List<Map<String,Object>>> pos(){return R.ok(pos.list());}
    @GetMapping("/po/{id}") public R<Map<String,Object>> po(@PathVariable String id){return R.ok(pos.one("SELECT * FROM sap_purchase_order WHERE ebeln=?",id));}
    @PostMapping("/po/{id}/close") public R<Map<String,Object>> close(@PathVariable String id){jdbc.update("UPDATE sap_purchase_order SET status='CLOSED' WHERE ebeln=?",id);return po(id);}
    @PostMapping("/migo") public R<Map<String,Object>> migo(@Valid @RequestBody MigoRequest request){return R.ok(goods.post(request));}
    @GetMapping("/material-docs") public R<List<Map<String,Object>>> docs(){return R.ok(jdbc.queryForList("SELECT * FROM sap_material_document"));}
    @GetMapping("/stock") public R<List<Map<String,Object>>> stock(@RequestParam(required=false)String matnr,@RequestParam(required=false)String werks){if(matnr!=null&&werks!=null)return R.ok(jdbc.queryForList("SELECT * FROM sap_stock WHERE matnr=? AND werks=?",matnr,werks));if(matnr!=null)return R.ok(jdbc.queryForList("SELECT * FROM sap_stock WHERE matnr=?",matnr));return R.ok(jdbc.queryForList("SELECT * FROM sap_stock"));}
    @PostMapping("/miro") public R<Map<String,Object>> miro(@Valid @RequestBody MiroRequest request){return R.ok(invoices.post(request));}
    @GetMapping("/supplier-invoices") public R<List<Map<String,Object>>> invoices(){return R.ok(jdbc.queryForList("SELECT * FROM sap_supplier_invoice"));}
    @GetMapping("/gr-ir") public R<List<Map<String,Object>>> grir(){return R.ok(jdbc.queryForList("SELECT * FROM sap_gr_ir"));}
}
