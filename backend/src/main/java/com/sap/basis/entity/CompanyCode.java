package com.sap.basis.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data @TableName("sap_company_code")
public class CompanyCode extends BaseEntity {
    @TableId private String bukrs;
    private String name;
    private String currency;
}
