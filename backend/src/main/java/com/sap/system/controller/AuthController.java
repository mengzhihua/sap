package com.sap.system.controller;

import com.sap.common.BizException;
import com.sap.common.R;
import com.sap.system.auth.CurrentUser;
import com.sap.system.auth.PasswordHasher;
import com.sap.system.auth.TokenService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JdbcTemplate jdbc;
    private final TokenService tokens;
    private final String adminPassword;

    public AuthController(JdbcTemplate jdbc, TokenService tokens,
                          @Value("${sap.auth.admin-password:admin123}") String adminPassword) {
        this.jdbc = jdbc;
        this.tokens = tokens;
        this.adminPassword = adminPassword;
    }

    @Data
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> user;
        try {
            user = jdbc.queryForMap("SELECT * FROM sap_user WHERE username=?", request.getUsername());
        } catch (Exception e) {
            throw new BizException("用户名或密码错误");
        }
        if (!PasswordHasher.verify(request.getPassword(), String.valueOf(user.get("password")))
                || !Integer.valueOf(1).equals(((Number) user.get("status")).intValue())) {
            throw new BizException("用户名或密码错误");
        }
        jdbc.update("UPDATE sap_user SET last_login_at=CURRENT_TIMESTAMP WHERE id=?", user.get("id"));
        Map<String, Object> safe = new HashMap<>(user);
        safe.remove("password");
        Map<String, Object> result = new HashMap<>();
        result.put("token", tokens.issue(((Number) user.get("id")).longValue(), request.getUsername()));
        result.put("user", safe);
        return R.ok(result);
    }

    @GetMapping("/me")
    public R<Map<String, Object>> me() {
        return R.ok(jdbc.queryForMap("SELECT id,username,real_name,role,status,last_login_at FROM sap_user WHERE username=?",
                CurrentUser.get()));
    }
}
