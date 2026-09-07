package com.sap.mm.service;

import com.sap.common.BizException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class StockService {
    private final JdbcTemplate jdbc;
    public StockService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public void change(String matnr, String werks, String lgort, BigDecimal qty, BigDecimal value) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM sap_stock WHERE matnr=? AND werks=? AND lgort=?", matnr, werks, lgort);
        if (rows.isEmpty()) {
            if (qty.compareTo(BigDecimal.ZERO) < 0) throw new BizException("库存不足: " + matnr);
            jdbc.update("INSERT INTO sap_stock(matnr,werks,lgort,unrestricted_qty,value) VALUES(?,?,?,?,?)",
                    matnr, werks, lgort, qty, value);
            return;
        }
        BigDecimal current = decimal(rows.get(0), "unrestricted_qty");
        if (current.add(qty).compareTo(BigDecimal.ZERO) < 0) throw new BizException("库存不足: " + matnr);
        jdbc.update("UPDATE sap_stock SET unrestricted_qty=unrestricted_qty+?,value=value+? WHERE matnr=? AND werks=? AND lgort=?",
                qty, value, matnr, werks, lgort);
    }
    public static BigDecimal decimal(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
    }
}
