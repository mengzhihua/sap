package com.sap.fi.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotBlank private String type;
    @NotBlank private String partner;
    @NotNull private BigDecimal amount;
}
