package com.sap.dashboard.controller;
import com.sap.common.R;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/dashboard")
public class DashboardController {
    private final JdbcTemplate jdbc; public DashboardController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @GetMapping("/summary") public R<Map<String,Object>> summary(){
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("purchaseOrders",jdbc.queryForObject("SELECT COUNT(*) FROM sap_purchase_order",Integer.class));
        result.put("pendingReceipts",jdbc.queryForObject("SELECT COUNT(*) FROM sap_purchase_order WHERE status IN ('OPEN','PARTIAL')",Integer.class));
        result.put("stockValue",jdbc.queryForObject("SELECT COALESCE(SUM(value),0) FROM sap_stock",Object.class));
        result.put("revenue",jdbc.queryForObject("SELECT COALESCE(SUM(amount),0) FROM sap_acc_document_item WHERE saknr='6001'",Object.class));
        result.put("cost",jdbc.queryForObject("SELECT COALESCE(SUM(amount),0) FROM sap_acc_document_item WHERE saknr IN ('6401','6402','5001')",Object.class));
        result.put("openAp",jdbc.queryForObject("SELECT COUNT(*) FROM sap_acc_document_item WHERE saknr='2201'",Integer.class));
        result.put("openAr",jdbc.queryForObject("SELECT COUNT(*) FROM sap_acc_document_item WHERE saknr='1122'",Integer.class));
        return R.ok(result);
    }
}
