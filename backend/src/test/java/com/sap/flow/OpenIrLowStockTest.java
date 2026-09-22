package com.sap.flow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.TestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class OpenIrLowStockTest extends TestSupport {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void lowStockAliasAndCreatePrByAlias() throws Exception {
        String body = mockMvc.perform(get("/api/open/ir/snapshots")
                        .header("X-Api-Key", "sap-open-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        boolean foundLow = false;
        for (JsonNode row : objectMapper.readTree(body).get("data").get("snapshots")) {
            if ("STOCK".equals(row.path("dataType").asText())
                    && "LOW".equals(row.path("status").asText())
                    && "MAT-1000".equals(row.path("sku").asText())) {
                foundLow = true;
                org.junit.jupiter.api.Assertions.assertEquals("M1099/1000/0001",
                        row.path("bizKey").asText());
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(foundLow, "种子物料 M1099/MAT-1000 应为 LOW");
        boolean foundPr = false;
        for (JsonNode row : objectMapper.readTree(body).get("data").get("snapshots")) {
            if ("PR".equals(row.path("dataType").asText())
                    && "IR1000001".equals(row.path("bizKey").asText())
                    && "CREATED".equals(row.path("status").asText())) {
                foundPr = true;
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(foundPr, "应包含待释放采购申请 IR1000001");
        boolean foundMo = false;
        for (JsonNode row : objectMapper.readTree(body).get("data").get("snapshots")) {
            if ("MO".equals(row.path("dataType").asText())
                    && "IR10000100".equals(row.path("bizKey").asText())
                    && "CREATED".equals(row.path("status").asText())) {
                foundMo = true;
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(foundMo, "应包含待释放生产订单 IR10000100");
        boolean foundAp = false;
        boolean foundAr = false;
        for (JsonNode row : objectMapper.readTree(body).get("data").get("snapshots")) {
            if ("AP_OPEN".equals(row.path("dataType").asText())
                    && "IRFI0001/1".equals(row.path("bizKey").asText())) {
                foundAp = true;
                org.junit.jupiter.api.Assertions.assertEquals("OPEN", row.path("status").asText());
            }
            if ("AR_OPEN".equals(row.path("dataType").asText())
                    && "IRFI0002/1".equals(row.path("bizKey").asText())) {
                foundAr = true;
                org.junit.jupiter.api.Assertions.assertEquals("OPEN", row.path("status").asText());
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(foundAp, "应包含应付未清 IRFI0001");
        org.junit.jupiter.api.Assertions.assertTrue(foundAr, "应包含应收未清 IRFI0002");

        mockMvc.perform(post("/api/open/ir/actions")
                        .header("X-Api-Key", "sap-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SAP_RELEASE_MO\",\"targetKey\":\"IR10000100\","
                                + "\"idempotencyKey\":\"SAP-MO-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("REL"));
        mockMvc.perform(post("/api/open/ir/actions")
                        .header("X-Api-Key", "sap-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SAP_RELEASE_MO\",\"targetKey\":\"IR10000100\","
                                + "\"idempotencyKey\":\"SAP-MO-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("REL"));

        String created = mockMvc.perform(post("/api/open/ir/actions")
                        .header("X-Api-Key", "sap-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SAP_CREATE_PR\",\"targetKey\":\"MAT-1000\","
                                + "\"idempotencyKey\":\"SAP-PR-1\","
                                + "\"params\":{\"sku\":\"MAT-1000\",\"qty\":16,\"plantCode\":\"1000\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.banfn").isString())
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andReturn().getResponse().getContentAsString();
        String replay = mockMvc.perform(post("/api/open/ir/actions")
                        .header("X-Api-Key", "sap-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SAP_CREATE_PR\",\"targetKey\":\"MAT-1000\","
                                + "\"idempotencyKey\":\"SAP-PR-1\","
                                + "\"params\":{\"sku\":\"MAT-1000\",\"qty\":16,\"plantCode\":\"1000\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertEquals(
                objectMapper.readTree(created).get("data").get("banfn").asText(),
                objectMapper.readTree(replay).get("data").get("banfn").asText());

        mockMvc.perform(post("/api/open/ir/release-mo")
                        .header("X-Api-Key", "sap-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aufnr\":\"IR10000100\",\"idempotencyKey\":\"SAP-MO-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("REL"));
        String dedicatedPr = mockMvc.perform(post("/api/open/ir/create-pr")
                        .header("X-Api-Key", "sap-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"MAT-1000\",\"idempotencyKey\":\"SAP-PR-1\","
                                + "\"params\":{\"sku\":\"MAT-1000\",\"qty\":16,\"plantCode\":\"1000\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertEquals(
                objectMapper.readTree(created).get("data").get("banfn").asText(),
                objectMapper.readTree(dedicatedPr).get("data").get("banfn").asText());
    }
}
