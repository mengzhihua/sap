package com.sap.basis.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserRequest {
    @NotBlank private String username;
    private String password;
    private String realName;
    private String role;
    private Integer status;
}
