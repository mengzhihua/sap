package com.sap.integration.client;

import java.util.Map;

public interface BmsClient {
    Map<String, Object> push(String sourcePath, Map<String, Object> payload);
}
