package com.sap.basis.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data
@TableName("sap_role")
public class Role extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String code;
    private String name;
}
