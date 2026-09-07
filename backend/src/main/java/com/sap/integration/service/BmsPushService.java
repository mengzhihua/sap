package com.sap.integration.service;

import com.sap.integration.client.BmsClient;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class BmsPushService {
    private final BmsClient client;
    private final IntegrationLogService logs;
    public BmsPushService(BmsClient client, IntegrationLogService logs) {
        this.client = client; this.logs = logs;
    }
    public Map<String, Object> push(String path, Map<String, Object> payload) {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> result = client.push(path, payload);
            logs.write("OUT", "BMS", "BMS_PUSH_DELIVERY", payload, result, true, null,
                    System.currentTimeMillis() - start);
            return result;
        } catch (RuntimeException e) {
            logs.write("OUT", "BMS", "BMS_PUSH_DELIVERY", payload, null, false, e.getMessage(),
                    System.currentTimeMillis() - start);
            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            return result;
        }
    }
    public Map<String, Object> pushInbound(String ebeln, BigDecimal qty) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("extRef", ebeln); payload.put("bizType", "INBOUND");
        payload.put("customerCode", "CUST-001"); payload.put("supplierCode", "SUP01");
        payload.put("warehouseCode", "WH-1000"); payload.put("orders", 1);
        payload.put("lines", 1); payload.put("qty", qty);
        return push("/api/open/wms/docs", payload);
    }
}
