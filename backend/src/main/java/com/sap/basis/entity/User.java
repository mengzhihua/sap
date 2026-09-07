package com.sap.basis.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data
@TableName("sap_user")
public class User extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String username;
    private String password;
    private String realName;
    private String role;
    private Integer status;
    private java.time.LocalDateTime lastLoginAt;
}
