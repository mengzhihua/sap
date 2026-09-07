package com.sap.mm.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PoCreateRequest {
    private String bsart = "NB";
    @NotBlank private String supplierCode;
    private String lifnr;
    private String ekorg = "1000";
    private String ekgrp = "001";
    private String bukrs = "1000";
    private String waers = "CNY";
    private String externalRef;
    @NotEmpty @Valid private List<PoItemRequest> items;
    @Data public static class PoItemRequest {
        @NotBlank private String matnr;
        @NotBlank private String werks;
        private String lgort = "0001";
        private BigDecimal qty;
        private BigDecimal price;
        private BigDecimal menge;
        private BigDecimal netpr;
        private String deliveryDate;
    }
}
