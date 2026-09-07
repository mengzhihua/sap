package com.sap.basis.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data
@TableName("sap_op_log")
public class OpLog extends BaseEntity {
    @TableId
    private Long id;
    private String username;
    private String method;
    private String path;
    private String requestBody;
    private String responseBody;
}
