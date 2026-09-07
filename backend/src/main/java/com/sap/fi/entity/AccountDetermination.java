package com.sap.fi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data @TableName("sap_account_determination")
public class AccountDetermination extends BaseEntity {
    @TableId private String accountKey;
    private String saknr;
}
