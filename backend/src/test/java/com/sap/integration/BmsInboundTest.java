package com.sap.integration;

import com.sap.TestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class BmsInboundTest extends TestSupport {
    @Autowired
    private MockMvc mvc;

    @Test
    void bmsStatementRequiresKeyAndIsIdempotent() throws Exception {
        String body = "{\"statementNo\":\"ST-TEST-1\",\"direction\":\"AR\",\"partnerCode\":\"CUST-001\",\"amount\":113,\"taxAmount\":13,\"bizDate\":\"2026-01-01\"}";
        mvc.perform(post("/api/open/bms/statements").header("X-Api-Key", "bad-key")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/open/bms/statements").header("X-Api-Key", "sap-open-key")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        mvc.perform(post("/api/open/bms/statements").header("X-Api-Key", "sap-open-key")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.statementNo").value("ST-TEST-1"));
    }
}
