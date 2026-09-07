package com.sap.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SrmGoodsReceiptPayload {
    @JsonProperty("GoodsMovementCode") private String goodsMovementCode;
    @JsonProperty("PostingDate") private String postingDate;
    @JsonProperty("to_MaterialDocumentItem") private List<Item> items;
    @Data public static class Item {
        @JsonProperty("PurchaseOrder") private String purchaseOrder;
        @JsonProperty("PurchaseOrderItem") private String purchaseOrderItem;
        @JsonProperty("Material") private String material;
        @JsonProperty("QuantityInBaseUnit") private BigDecimal quantityInBaseUnit;
        @JsonProperty("GoodsMovementType") private String goodsMovementType;
    }
}
