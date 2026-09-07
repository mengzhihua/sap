package com.sap.system.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Slf4j
@Component
public class TokenService {
    private final byte[] key;
    private final long ttlMillis;

    public TokenService(@Value("${sap.auth.secret:}") String secret,
                        @Value("${sap.auth.token-ttl:12h}") Duration ttl) {
        if (secret == null || secret.trim().isEmpty()) {
            byte[] b = new byte[32];
            new SecureRandom().nextBytes(b);
            key = b;
        } else {
            key = secret.getBytes(StandardCharsets.UTF_8);
        }
        ttlMillis = ttl.toMillis();
    }

    public String issue(Long userId, String username) {
        String payload = userId + ":" + username + ":" + (System.currentTimeMillis() + ttlMillis);
        String body = b64(payload.getBytes(StandardCharsets.UTF_8));
        return body + "." + b64(sign(body));
    }

    public Principal parse(String token) {
        try {
            if (token == null) return null;
            int dot = token.lastIndexOf('.');
            if (dot <= 0) return null;
            String body = token.substring(0, dot);
            byte[] signature = Base64.getUrlDecoder().decode(token.substring(dot + 1));
            if (!MessageDigest.isEqual(signature, sign(body))) return null;
            String[] parts = new String(Base64.getUrlDecoder().decode(body), StandardCharsets.UTF_8).split(":", 3);
            if (parts.length != 3 || Long.parseLong(parts[2]) < System.currentTimeMillis()) return null;
            return new Principal(Long.valueOf(parts[0]), parts[1]);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private byte[] sign(String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String b64(byte[] b) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    public static class Principal {
        private final Long userId;
        private final String username;

        public Principal(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
    }
}
