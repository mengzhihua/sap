package com.sap.sd.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class BillingRequest {
    @NotBlank private String dnVbeln;
}
