package com.sap.integration.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data @TableName("sap_integration_log")
public class IntegrationLog extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String direction;
    private String systemName;
    private String actionName;
    private String request;
    private String response;
    private Integer success;
    private String error;
    private Long elapsedMs;
}
