package com.sap.integration.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class IntegrationLogService {
    private final JdbcTemplate jdbc;

    public IntegrationLogService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void write(String direction, String system, String action, Object request,
                      Object response, boolean success, String error, long elapsed) {
        jdbc.update("INSERT INTO sap_integration_log(direction,system_name,action_name,request,response,success,error,elapsed_ms,created_at)"
                        + " VALUES(?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                direction, system, action, String.valueOf(request), String.valueOf(response),
                success ? 1 : 0, error, elapsed);
    }

    public java.util.List<Map<String, Object>> list(String system, String direction) {
        if (system != null && direction != null) {
            return jdbc.queryForList("SELECT * FROM sap_integration_log WHERE system_name=? AND direction=? ORDER BY id DESC",
                    system, direction);
        }
        if (system != null) {
            return jdbc.queryForList("SELECT * FROM sap_integration_log WHERE system_name=? ORDER BY id DESC", system);
        }
        return jdbc.queryForList("SELECT * FROM sap_integration_log ORDER BY id DESC");
    }
}
