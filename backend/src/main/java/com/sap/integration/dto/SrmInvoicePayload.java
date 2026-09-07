package com.sap.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SrmInvoicePayload {
    @JsonProperty("SupplierInvoice") private String supplierInvoice;
    @JsonProperty("InvoicingParty") private String invoicingParty;
    @JsonProperty("Supplier") private String supplier;
    @JsonProperty("PurchaseOrder") private String purchaseOrder;
    @JsonProperty("DocumentCurrency") private String documentCurrency;
    @JsonProperty("InvoiceGrossAmount") private BigDecimal invoiceGrossAmount;
    @JsonProperty("to_SuplrInvcItemPurOrdRef") private List<Item> items;
    @Data public static class Item {
        @JsonProperty("PurchaseOrder") private String purchaseOrder;
        @JsonProperty("PurchaseOrderItem") private String purchaseOrderItem;
        @JsonProperty("QuantityInPurchaseOrderUnit") private BigDecimal quantityInPurchaseOrderUnit;
        @JsonProperty("PurchaseOrderItemPrice") private BigDecimal purchaseOrderItemPrice;
    }
}
