package com.sap.system.service;

import com.sap.basis.entity.User;
import com.sap.basis.mapper.UserMapper;
import com.sap.system.auth.PasswordHasher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class UserInitializer implements ApplicationRunner {
    private final UserMapper users;
    private final String adminPassword;

    public UserInitializer(UserMapper users,
                           @Value("${sap.auth.admin-password:admin123}") String adminPassword) {
        this.users = users;
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
        if (users.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)) != null) return;
        User user = new User();
        user.setUsername(username); user.setPassword(PasswordHasher.hash(password));
        user.setRealName(name); user.setRole(role); user.setStatus(1);
        users.insert(user);
    }
}
