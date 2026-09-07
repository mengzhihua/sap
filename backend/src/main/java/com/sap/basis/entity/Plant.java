package com.sap.basis.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data @TableName("sap_plant")
public class Plant extends BaseEntity {
    @TableId private String werks;
    private String name;
    private String bukrs;
    private String bmsWarehouseCode;
}
