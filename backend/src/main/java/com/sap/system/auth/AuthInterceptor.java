package com.sap.system.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.basis.entity.OpLog;
import com.sap.basis.service.OpLogService;
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
    private final AccessPolicy accessPolicy;
    private final OpLogService opLogService;

    public AuthInterceptor(TokenService tokenService, JdbcTemplate jdbc, ObjectMapper objectMapper,
                           AccessPolicy accessPolicy, OpLogService opLogService) {
        this.tokenService = tokenService;
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
        this.accessPolicy = accessPolicy;
        this.opLogService = opLogService;
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
        String username = String.valueOf(user.get("username"));
        if (!accessPolicy.allowed(username, req.getMethod(), path)) {
            return reject(res, 403, "无权访问该模块");
        }
        CurrentUser.set(username);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (isMutation(request.getMethod()) && !request.getRequestURI().startsWith("/api/auth/")) {
            OpLog log = new OpLog();
            log.setUsername(CurrentUser.get());
            log.setMethod(request.getMethod());
            log.setPath(request.getRequestURI());
            log.setResponseBody(String.valueOf(response.getStatus()));
            opLogService.save(log);
        }
        CurrentUser.clear();
    }

    private boolean isMutation(String method) {
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }

    private boolean reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(R.fail(status, message)));
        return false;
    }
}
