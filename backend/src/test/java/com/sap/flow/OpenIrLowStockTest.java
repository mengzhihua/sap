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

        mockMvc.perform(post("/api/open/ir/actions")
                        .header("X-Api-Key", "sap-open-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SAP_CREATE_PR\",\"targetKey\":\"MAT-1000\","
                                + "\"params\":{\"sku\":\"MAT-1000\",\"qty\":16,\"plantCode\":\"1000\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.banfn").isString())
                .andExpect(jsonPath("$.data.status").value("CREATED"));
    }
}
