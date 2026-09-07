package com.sap.sd.controller;
import com.sap.common.R;
import com.sap.sd.dto.*;
import com.sap.sd.service.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.*;

@RestController @RequestMapping("/api/sd") @Validated
public class SdController {
    private final JdbcTemplate jdbc;private final SalesOrderService sales;private final DeliveryService deliveries;private final BillingService billing;
    public SdController(JdbcTemplate jdbc,SalesOrderService sales,DeliveryService deliveries,BillingService billing){this.jdbc=jdbc;this.sales=sales;this.deliveries=deliveries;this.billing=billing;}
    @GetMapping("/customers") public R<List<Map<String,Object>>> customers(){return R.ok(jdbc.queryForList("SELECT * FROM sap_customer"));}
    @PostMapping("/so") public R<Map<String,Object>> so(@Valid @RequestBody SoCreateRequest request){return R.ok(sales.create(request));}
    @GetMapping("/so") public R<List<Map<String,Object>>> sos(){return R.ok(sales.list());}
    @PostMapping("/dn") public R<Map<String,Object>> dn(@Valid @RequestBody DnCreateRequest request){return R.ok(deliveries.create(request));}
    @PostMapping("/dn/{id}/pick") public R<Map<String,Object>> pick(@PathVariable String id){return R.ok(deliveries.pick(id));}
    @PostMapping("/dn/{id}/pgi") public R<Map<String,Object>> pgi(@PathVariable String id){return R.ok(deliveries.pgi(id));}
    @PostMapping("/billing") public R<Map<String,Object>> bill(@Valid @RequestBody BillingRequest request){return R.ok(billing.create(request));}
    @GetMapping("/billing") public R<List<Map<String,Object>>> bills(){return R.ok(jdbc.queryForList("SELECT * FROM sap_billing_doc"));}
}
