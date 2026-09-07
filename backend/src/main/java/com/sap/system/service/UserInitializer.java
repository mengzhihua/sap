package com.sap.system.service;

import com.sap.system.auth.PasswordHasher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final String adminPassword;

    public UserInitializer(JdbcTemplate jdbc,
                           @Value("${sap.auth.admin-password:admin123}") String adminPassword) {
        this.jdbc = jdbc;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        create("admin", adminPassword, "系统管理员", "ADMIN");
        create("mm_user", "admin123", "物料用户", "MM");
        create("fi_user", "admin123", "财务用户", "FI");
        create("sd_user", "admin123", "销售用户", "SD");
        create("pp_user", "admin123", "生产用户", "PP");
    }

    private void create(String username, String password, String name, String role) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM sap_user WHERE username=?", Integer.class, username);
        if (count != null && count > 0) return;
        jdbc.update("INSERT INTO sap_user(username,password,real_name,role,status) VALUES(?,?,?,?,1)",
                username, PasswordHasher.hash(password), name, role);
    }
}
