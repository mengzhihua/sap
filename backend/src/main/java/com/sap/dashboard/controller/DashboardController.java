package com.sap.dashboard.controller;

import com.sap.common.R;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final JdbcTemplate jdbc;

    public DashboardController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/summary")
    public R<Map<String, Object>> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("poCount", count("SELECT COUNT(*) FROM sap_purchase_order"));
        result.put("poOpenCount", count("SELECT COUNT(*) FROM sap_purchase_order WHERE status IN ('OPEN','PARTIAL')"));
        result.put("stockValue", value("SELECT COALESCE(SUM(value),0) FROM sap_stock"));
        result.put("monthRevenue", value("SELECT COALESCE(SUM(amount),0) FROM sap_acc_document_item " +
                "WHERE saknr='6001' AND YEAR(CURRENT_DATE)=YEAR(CURRENT_DATE)"));
        result.put("monthCost", value("SELECT COALESCE(SUM(amount),0) FROM sap_acc_document_item " +
                "WHERE saknr IN ('6401','6402','5001')"));
        result.put("apOpen", count("SELECT COUNT(*) FROM sap_acc_document_item i JOIN sap_acc_document d " +
                "ON d.belnr=i.belnr WHERE i.saknr='2201' AND d.cleared_by IS NULL AND d.reversed_by IS NULL AND d.blart<>'AB'"));
        result.put("arOpen", count("SELECT COUNT(*) FROM sap_acc_document_item i JOIN sap_acc_document d " +
                "ON d.belnr=i.belnr WHERE i.saknr='1122' AND d.cleared_by IS NULL AND d.reversed_by IS NULL AND d.blart<>'AB'"));
        Map<String, Object> statuses = new LinkedHashMap<>();
        for (String status : new String[]{"CRTD", "REL", "CNF", "TECO"}) {
            statuses.put(status, count("SELECT COUNT(*) FROM sap_production_order WHERE status=?", status));
        }
        result.put("prodOrderStatus", statuses);
        Number totalLogs = jdbc.queryForObject("SELECT COUNT(*) FROM sap_integration_log", Integer.class);
        Number successLogs = jdbc.queryForObject("SELECT COALESCE(SUM(success),0) FROM sap_integration_log", Integer.class);
        result.put("integrationSuccessRate", totalLogs == null || totalLogs.intValue() == 0 ? 1
                : successLogs.doubleValue() / totalLogs.doubleValue());
        result.put("recentDocuments", jdbc.queryForList("SELECT * FROM sap_acc_document ORDER BY id DESC LIMIT 10"));
        return R.ok(result);
    }

    private Number count(String sql, Object... args) {
        return jdbc.queryForObject(sql, Integer.class, args);
    }

    private Object value(String sql, Object... args) {
        return jdbc.queryForObject(sql, Object.class, args);
    }
}
