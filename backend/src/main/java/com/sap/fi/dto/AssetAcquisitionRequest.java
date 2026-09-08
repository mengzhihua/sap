package com.sap.fi.dto;

import lombok.Data;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AssetAcquisitionRequest {
    @NotNull @DecimalMin(value = "0.01") private BigDecimal amount;
    private LocalDate postingDate;
    private String offsetAccount;
    private String text;
}
