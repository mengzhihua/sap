package com.sap.basis.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data @TableName("sap_purchasing_group")
public class PurchasingGroup extends BaseEntity {
    @TableId private String ekgrp;
    private String name;
}
