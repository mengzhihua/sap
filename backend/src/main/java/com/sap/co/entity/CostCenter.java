package com.sap.co.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data @TableName("sap_cost_center")
public class CostCenter extends BaseEntity {
    @TableId private String kostl;
    private String name;
    private String bukrs;
    private String responsible;
}
