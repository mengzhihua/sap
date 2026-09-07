package com.sap.pp.controller;
import com.sap.common.R;
import com.sap.pp.dto.*;
import com.sap.pp.service.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;

@RestController @RequestMapping("/api/pp") @Validated
public class PpController {
    private final JdbcTemplate jdbc;private final BomService boms;private final ProductionOrderService orders;
    public PpController(JdbcTemplate jdbc,BomService boms,ProductionOrderService orders){this.jdbc=jdbc;this.boms=boms;this.orders=orders;}
    @GetMapping("/bom") public R<List<Map<String,Object>>> bom(){return R.ok(jdbc.queryForList("SELECT * FROM sap_bom"));}
    @PostMapping("/bom") public R<Map<String,Object>> bom(@Valid @RequestBody BomRequest request){return R.ok(boms.create(request));}
    @GetMapping("/orders") public R<List<Map<String,Object>>> orders(){return R.ok(jdbc.queryForList("SELECT * FROM sap_production_order"));}
    @PostMapping("/orders") public R<Map<String,Object>> order(@Valid @RequestBody ProductionOrderRequest request){return R.ok(orders.create(request));}
    @PostMapping("/orders/{id}/release") public R<Map<String,Object>> release(@PathVariable String id){return R.ok(orders.release(id));}
    @PostMapping("/orders/{id}/issue") public R<Map<String,Object>> issue(@PathVariable String id,@RequestParam(required=false,defaultValue="CC1000")String kostl){return R.ok(orders.issue(id,kostl));}
    @PostMapping("/orders/{id}/confirm") public R<Map<String,Object>> confirm(@PathVariable String id,@RequestParam(required=false,defaultValue="1")BigDecimal qty){return R.ok(orders.confirm(id,qty));}
    @PostMapping("/orders/{id}/receipt") public R<Map<String,Object>> receipt(@PathVariable String id,@RequestParam(required=false)BigDecimal qty){Map<String,Object> order=jdbc.queryForMap("SELECT * FROM sap_production_order WHERE aufnr=?",id);return R.ok(orders.receipt(id,qty==null?(BigDecimal)order.get("target_qty"):qty));}
    @PostMapping("/orders/{id}/teco") public R<Map<String,Object>> teco(@PathVariable String id){return R.ok(orders.teco(id));}
}
