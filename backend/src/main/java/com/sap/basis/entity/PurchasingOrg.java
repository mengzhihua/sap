package com.sap.basis.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data @TableName("sap_purchasing_org")
public class PurchasingOrg extends BaseEntity {
    @TableId private String ekorg;
    private String name;
}
