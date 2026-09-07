package com.sap.basis.controller;
import com.sap.common.R;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/basis")
public class BasisController {
    private final JdbcTemplate jdbc; public BasisController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @GetMapping("/users") public R<List<Map<String,Object>>> users(){return R.ok(jdbc.queryForList("SELECT id,username,real_name,role,status,last_login_at FROM sap_user"));}
    @GetMapping("/roles") public R<List<Map<String,Object>>> roles(){return R.ok(Arrays.asList(map("code","ADMIN","name","管理员"),map("code","MM","name","物料管理"),map("code","SD","name","销售管理"),map("code","FI","name","财务管理"),map("code","CO","name","成本管理"),map("code","PP","name","生产管理")));}
    @GetMapping("/org/{kind}") public R<List<Map<String,Object>>> org(@PathVariable String kind){return R.ok(jdbc.queryForList("SELECT * FROM "+table(kind)));}
    @GetMapping("/tcodes") public R<List<Map<String,Object>>> tcodes(@RequestParam(required=false)String q){if(q==null)return R.ok(jdbc.queryForList("SELECT * FROM sap_tcode"));String x="%"+q+"%";return R.ok(jdbc.queryForList("SELECT * FROM sap_tcode WHERE tcode LIKE ? OR name LIKE ?",x,x));}
    @GetMapping("/op-logs") public R<List<Map<String,Object>>> logs(){return R.ok(jdbc.queryForList("SELECT * FROM sap_op_log ORDER BY id DESC"));}
    private String table(String kind){if("company-codes".equals(kind))return "sap_company_code";if("plants".equals(kind))return "sap_plant";if("storage-locations".equals(kind))return "sap_storage_location";if("purchasing-orgs".equals(kind))return "sap_purchasing_org";if("purchasing-groups".equals(kind))return "sap_purchasing_group";if("sales-orgs".equals(kind))return "sap_sales_org";throw new IllegalArgumentException("未知组织类型: "+kind);}
    private static Map<String,Object> map(String a,Object b,String c,Object d){Map<String,Object> m=new HashMap<>();m.put(a,b);m.put(c,d);return m;}
}
