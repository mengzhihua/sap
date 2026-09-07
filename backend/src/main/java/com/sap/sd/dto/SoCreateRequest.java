package com.sap.sd.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SoCreateRequest {
    @NotBlank private String customerCode;
    private String kunnr;
    private String vkorg = "1000";
    private String waers = "CNY";
    @NotEmpty @Valid private List<Item> items;
    @Data public static class Item {
        private String matnr;
        private String werks = "1000";
        private BigDecimal qty;
        private BigDecimal price;
    }
}
