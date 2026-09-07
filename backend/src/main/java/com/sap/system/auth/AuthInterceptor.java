package com.sap.system.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.common.R;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final TokenService tokenService;
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(TokenService tokenService, JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws IOException {
        String path = req.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())
                || "/api/auth/login".equals(path)
                || "/actuator/health".equals(path)
                || path.startsWith("/API_")
                || path.startsWith("/api/srm/")
                || path.startsWith("/api/open/")) {
            return true;
        }
        String auth = req.getHeader("Authorization");
        String token = auth != null && auth.regionMatches(true, 0, "Bearer ", 0, 7)
                ? auth.substring(7).trim() : null;
        TokenService.Principal principal = tokenService.parse(token);
        if (principal == null) {
            return reject(res, 401, "未登录或登录已过期");
        }
        Map<String, Object> user = jdbc.queryForMap(
                "SELECT id,username,role,status FROM sap_user WHERE id=?", principal.getUserId());
        if (user.isEmpty() || !Integer.valueOf(1).equals(((Number) user.get("status")).intValue())) {
            return reject(res, 401, "账号不存在或已停用");
        }
        CurrentUser.set(String.valueOf(user.get("username")));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        CurrentUser.clear();
    }

    private boolean reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(R.fail(status, message)));
        return false;
    }
}
