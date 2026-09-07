package com.sap.basis.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data @TableName("sap_sales_org")
public class SalesOrg extends BaseEntity {
    @TableId private String vkorg;
    private String name;
    private String bukrs;
}
