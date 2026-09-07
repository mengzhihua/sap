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
public class SrmInboundTest extends TestSupport {
    @Autowired
    private MockMvc mvc;

    @Test
    void srmRejectsMissingBasicAuth() throws Exception {
        mvc.perform(post("/API_PURCHASEORDER_PROCESS_SRV/A_PurchaseOrder")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("SAP_AUTH_ERROR"));
    }

    @Test
    void srmBasicAuthAliasAndFakePoFallbackWork() throws Exception {
        String basic = "Basic " + java.util.Base64.getEncoder().encodeToString("srm:srm123".getBytes("UTF-8"));
        String po = "{\"PurchaseOrderType\":\"NB\",\"Supplier\":\"SUP01\",\"PurchasingOrganization\":\"1000\",\"PurchasingGroup\":\"001\",\"CompanyCode\":\"1000\",\"DocumentCurrency\":\"CNY\",\"to_PurchaseOrderItem\":[{\"Material\":\"SKU001\",\"OrderQuantity\":2,\"NetPriceAmount\":45,\"Plant\":\"P001\"}]}";
        mvc.perform(post("/API_PURCHASEORDER_PROCESS_SRV/A_PurchaseOrder").header("Authorization", basic)
                        .contentType(MediaType.APPLICATION_JSON).content(po))
                .andExpect(status().isOk()).andExpect(jsonPath("$.d.PurchaseOrder").value(org.hamcrest.Matchers.startsWith("45")));
        String gr = "{\"GoodsMovementCode\":\"01\",\"PostingDate\":\"2026-01-01\",\"to_MaterialDocumentItem\":[{\"PurchaseOrder\":\"PO20260001\",\"Material\":\"SKU001\",\"QuantityInBaseUnit\":1,\"GoodsMovementType\":\"101\"}]}";
        mvc.perform(post("/API_MATERIAL_DOCUMENT_SRV/A_MaterialDocumentHeader").header("Authorization", basic)
                        .contentType(MediaType.APPLICATION_JSON).content(gr))
                .andExpect(status().isOk()).andExpect(jsonPath("$.d.MaterialDocument").value(org.hamcrest.Matchers.startsWith("50")));
        String invoice = "{\"SupplierInvoice\":\"EXT-1\",\"InvoicingParty\":\"SUP01\",\"InvoiceGrossAmount\":50.85,\"to_SuplrInvcItemPurOrdRef\":[{\"PurchaseOrder\":\"PO20260002\",\"PurchaseOrderItem\":\"10\",\"QuantityInPurchaseOrderUnit\":1,\"PurchaseOrderItemPrice\":45}]}";
        mvc.perform(post("/API_SUPPLIERINVOICE_PROCESS_SRV/A_SupplierInvoice").header("Authorization", basic)
                        .contentType(MediaType.APPLICATION_JSON).content(invoice))
                .andExpect(status().isOk()).andExpect(jsonPath("$.d.SupplierInvoice").value(org.hamcrest.Matchers.startsWith("51")));
        mvc.perform(post("/API_SUPPLIER_EVALUATION_SRV/A_SupplierEvaluation").header("Authorization", basic)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"Supplier\":\"SUP01\",\"EvaluationPeriod\":\"2026-Q1\",\"Score\":92,\"Grade\":\"A\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.d.EvaluationId").value(org.hamcrest.Matchers.startsWith("EV")));
    }
}
