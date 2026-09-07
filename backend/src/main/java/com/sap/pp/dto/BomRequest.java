package com.sap.pp.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BomRequest {
    @NotBlank private String matnr;
    private String werks = "1000";
    private BigDecimal baseQty = BigDecimal.ONE;
    @NotEmpty @Valid private List<Item> items;
    @Data public static class Item {
        private String component;
        private String matnr;
        private BigDecimal qty;
    }
}
