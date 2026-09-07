package com.sap.mm.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseReqRequest {
    private String requester;
    private String bukrs = "1000";
    private String werks = "1000";
    @NotEmpty @Valid private List<Item> items;

    @Data
    public static class Item {
        private String matnr;
        private BigDecimal menge;
        private BigDecimal netpr;
        private String werks = "1000";
        private String lgort = "0001";
    }
}
