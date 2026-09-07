package com.sap.mm.service;

import com.sap.common.BizException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReferenceDataService {
    private final JdbcTemplate jdbc;
    public ReferenceDataService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public String vendor(String value) {
        if (value == null || value.trim().isEmpty()) throw new BizException("供应商不能为空");
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT lifnr FROM sap_vendor WHERE lifnr=? OR alias_code=?", value, value);
        if (rows.isEmpty()) throw new BizException("供应商不存在: " + value);
        return String.valueOf(rows.get(0).get("lifnr"));
    }
    public String material(String value) {
        if (value == null || value.trim().isEmpty()) throw new BizException("物料不能为空");
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT matnr FROM sap_material WHERE matnr=? OR alias_code=?", value, value);
        if (rows.isEmpty()) throw new BizException("物料不存在: " + value);
        return String.valueOf(rows.get(0).get("matnr"));
    }
    public String maybeMaterial(String value) {
        if (value == null) return null;
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT matnr FROM sap_material WHERE matnr=? OR alias_code=?", value, value);
        return rows.isEmpty() ? value : String.valueOf(rows.get(0).get("matnr"));
    }
    public String plant(String value) {
        if (value == null || value.trim().isEmpty()) return "1000";
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT werks FROM sap_plant WHERE werks=?", value);
        if (rows.isEmpty() && "P001".equals(value)) return "1000";
        if (rows.isEmpty()) throw new BizException("工厂不存在: " + value);
        return String.valueOf(rows.get(0).get("werks"));
    }
    public String customer(String value) {
        if (value == null || value.trim().isEmpty()) throw new BizException("客户不能为空");
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT kunnr FROM sap_customer WHERE kunnr=? OR alias_code=?", value, value);
        if (rows.isEmpty()) throw new BizException("客户不存在: " + value);
        return String.valueOf(rows.get(0).get("kunnr"));
    }
    public String alias(String table, String key, String value) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT alias_code FROM " + table + " WHERE " + key + "=?", value);
        return rows.isEmpty() ? value : String.valueOf(rows.get(0).get("alias_code"));
    }
    public String stockAccount(String matnr) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT mtart FROM sap_material WHERE matnr=?", matnr);
        return !rows.isEmpty() && "ROH".equals(rows.get(0).get("mtart")) ? "1411" : "1405";
    }
    public String warehouse(String werks) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT bms_warehouse_code FROM sap_plant WHERE werks=?", werks);
        return rows.isEmpty() ? werks : String.valueOf(rows.get(0).get("bms_warehouse_code"));
    }
}
