package com.sap.sd.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data @TableName("sap_customer")
public class Customer extends BaseEntity {
    @TableId private String kunnr;
    private String name;
    private String aliasCode;
    private String reconAccount;
}
