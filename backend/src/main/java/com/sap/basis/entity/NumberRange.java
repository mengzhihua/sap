package com.sap.basis.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data @TableName("sap_number_range")
public class NumberRange extends BaseEntity {
    @TableId private String objectName;
    private String prefix;
    private Integer currentNo;
}
