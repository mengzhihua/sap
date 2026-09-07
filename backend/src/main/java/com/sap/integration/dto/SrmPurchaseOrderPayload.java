package com.sap.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SrmPurchaseOrderPayload {
    @JsonProperty("PurchaseOrderType") private String purchaseOrderType;
    @JsonProperty("Supplier") private String supplier;
    @JsonProperty("PurchasingOrganization") private String purchasingOrganization;
    @JsonProperty("PurchasingGroup") private String purchasingGroup;
    @JsonProperty("CompanyCode") private String companyCode;
    @JsonProperty("DocumentCurrency") private String documentCurrency;
    @JsonProperty("to_PurchaseOrderItem") private List<Item> items;
    @Data public static class Item {
        @JsonProperty("Material") private String material;
        @JsonProperty("OrderQuantity") private BigDecimal orderQuantity;
        @JsonProperty("NetPriceAmount") private BigDecimal netPriceAmount;
        @JsonProperty("Plant") private String plant;
        @JsonProperty("ScheduleLineDeliveryDate") private String scheduleLineDeliveryDate;
    }
}
