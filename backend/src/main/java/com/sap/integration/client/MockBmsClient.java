package com.sap.integration.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "sap.bms.mode", havingValue = "mock", matchIfMissing = true)
public class MockBmsClient implements BmsClient {
    @Override
    public Map<String, Object> push(String sourcePath, Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();
        result.put("extRef", payload.get("extRef"));
        result.put("docNo", "BMS-" + payload.get("extRef"));
        result.put("billStatus", "BILLED");
        result.put("arAmount", payload.get("qty") == null ? 0 : payload.get("qty"));
        result.put("apAmount", 0);
        result.put("error", null);
        return result;
    }
}
