package com.sap.system.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AccessPolicy {
    private final JdbcTemplate jdbc;

    public AccessPolicy(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean allowed(String username, String method, String path) {
        if ("GET".equalsIgnoreCase(method)) {
            return true;
        }
        Map<String, Object> user = jdbc.queryForMap(
                "SELECT role FROM sap_user WHERE username=?", username);
        String role = String.valueOf(user.get("role"));
        if ("ADMIN".equalsIgnoreCase(role)) {
            return true;
        }
        String module = moduleFor(path);
        return role.equalsIgnoreCase(module);
    }

    private String moduleFor(String path) {
        if (path.startsWith("/api/mm/")) return "MM";
        if (path.startsWith("/api/sd/")) return "SD";
        if (path.startsWith("/api/fi/")) return "FI";
        if (path.startsWith("/api/co/")) return "CO";
        if (path.startsWith("/api/pp/")) return "PP";
        if (path.startsWith("/api/basis/")) return "BASIS";
        if (path.startsWith("/api/dashboard/")) return "DASHBOARD";
        if (path.startsWith("/api/integration/")) return "INTEGRATION";
        return "";
    }
}
