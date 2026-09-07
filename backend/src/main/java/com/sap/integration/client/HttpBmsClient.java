package com.sap.integration.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "sap.bms.mode", havingValue = "http")
public class HttpBmsClient implements BmsClient {
    private final RestTemplate rest;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl;
    private final String apiKey;

    public HttpBmsClient(RestTemplateBuilder builder,
                         @Value("${sap.bms.base-url:http://localhost:8080}") String baseUrl,
                         @Value("${sap.bms.api-key:dev-open-key}") String apiKey) {
        this.rest = builder.setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30)).build();
        this.baseUrl = baseUrl.replaceAll("/$", "");
        this.apiKey = apiKey;
    }

    @Override
    public Map<String, Object> push(String sourcePath, Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Api-Key", apiKey);
        ResponseEntity<String> response = rest.postForEntity(baseUrl + sourcePath,
                new HttpEntity<Object>(new Object[]{payload}, headers), String.class);
        try {
            Map<String, Object> body = objectMapper.readValue(response.getBody(),
                    new TypeReference<Map<String, Object>>() { });
            Object data = body.get("data");
            if (data instanceof java.util.List && !((java.util.List<?>) data).isEmpty()) {
                return (Map<String, Object>) ((java.util.List<?>) data).get(0);
            }
            return body;
        } catch (Exception e) {
            throw new IllegalStateException("BMS 响应解析失败: " + e.getMessage(), e);
        }
    }
}
