package com.sap.pp.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ProductionOrderRequest {
    @NotBlank private String matnr;
    private String werks = "1000";
    @NotNull private BigDecimal targetQty;
}
