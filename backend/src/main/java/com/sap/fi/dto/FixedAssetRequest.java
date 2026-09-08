package com.sap.fi.dto;

import lombok.Data;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class FixedAssetRequest {
    @NotBlank private String name;
    @NotBlank private String assetClass;
    @NotBlank private String bukrs;
    private String kostl;
    @NotNull @Min(1) private Integer usefulLifeMonths;
    @NotNull @DecimalMin("0.00") private BigDecimal salvageValue;
}
