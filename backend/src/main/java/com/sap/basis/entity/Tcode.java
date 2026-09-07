package com.sap.basis.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data @TableName("sap_tcode")
public class Tcode extends BaseEntity {
    @TableId private String tcode;
    private String module;
    private String name;
    private String route;
}
