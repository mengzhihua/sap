package com.sap.fi.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AccountDeterminationRequest {
    @NotBlank private String key;
    @NotBlank private String saknr;
}
