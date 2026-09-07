package com.sap.mm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_purchase_order")
public class PurchaseOrder extends BaseEntity {
    @TableId private String ebeln;
    private String bsart;
    private String lifnr;
    private String ekorg;
    private String ekgrp;
    private String bukrs;
    private String waers;
    private String status;
    private String externalRef;
    private String source;
    private BigDecimal totalAmount;
}
