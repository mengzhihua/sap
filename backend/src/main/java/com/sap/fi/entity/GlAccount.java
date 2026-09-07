package com.sap.fi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data @TableName("sap_gl_account")
public class GlAccount extends BaseEntity {
    @TableId private String saknr;
    private String txt;
    private String type;
    private String reconType;
}
