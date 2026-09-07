package com.sap.basis.dto;

import lombok.Data;

@Data
public class OrgRequest {
    private String code;
    private String name;
    private String bukrs;
    private String currency;
    private String werks;
    private String lgort;
    private String ekorg;
    private String ekgrp;
    private String vkorg;
    private String bmsWarehouseCode;
}
