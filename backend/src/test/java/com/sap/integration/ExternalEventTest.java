package com.sap.integration;

import com.sap.TestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class ExternalEventTest extends TestSupport {
    @Autowired
    private MockMvc mvc;

    @Test
    void deliveryFromOmsIsIdempotent() throws Exception {
        String body = "{\"orderNo\":\"OMS-SO-EXT-1\",\"kunnr\":\"C1000\",\"items\":[{\"sku\":\"M1001\",\"qty\":2}]}";
        String first = mvc.perform(post("/api/open/events/delivery").header("X-Api-Key", "sap-open-key")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("PGI"))
                .andExpect(jsonPath("$.data.soVbeln").value("OMS-SO-EXT-1"))
                .andReturn().getResponse().getContentAsString();
        String vbeln = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(first).path("data").path("vbeln").asText();
        mvc.perform(post("/api/open/events/delivery").header("X-Api-Key", "sap-open-key")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.vbeln").value(vbeln));
    }
}
