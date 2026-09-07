package com.sap.basis.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data
@TableName("sap_user_role")
public class UserRole extends BaseEntity {
    @TableId
    private Long userId;
    private Long roleId;
}
