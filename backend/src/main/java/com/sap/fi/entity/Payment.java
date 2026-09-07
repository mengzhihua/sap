package com.sap.fi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_payment")
public class Payment extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String type;
    private String partner;
    private BigDecimal amount;
    private String belnr;
    private String clearedDocs;
}
