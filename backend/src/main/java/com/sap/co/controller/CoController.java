package com.sap.co.controller;
import com.sap.common.R;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/co")
public class CoController {
    private final JdbcTemplate jdbc; public CoController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @GetMapping("/cost-centers") public R<List<Map<String,Object>>> centers(){return R.ok(jdbc.queryForList("SELECT * FROM sap_cost_center"));}
    @GetMapping("/documents") public R<List<Map<String,Object>>> docs(@RequestParam(required=false)String kostl){return R.ok(kostl==null?jdbc.queryForList("SELECT * FROM sap_co_document"):jdbc.queryForList("SELECT * FROM sap_co_document WHERE kostl=?",kostl));}
    @GetMapping("/report") public R<List<Map<String,Object>>> report(){return R.ok(jdbc.queryForList("SELECT kostl,cost_element,SUM(amount) amount FROM sap_co_document GROUP BY kostl,cost_element"));}
}
