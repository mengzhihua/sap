package com.sap.mm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data @TableName("sap_vendor")
public class Vendor extends BaseEntity {
    @TableId private String lifnr;
    private String name;
    private String aliasCode;
    private String country;
    private String paymentTerm;
    private String reconAccount;
}
