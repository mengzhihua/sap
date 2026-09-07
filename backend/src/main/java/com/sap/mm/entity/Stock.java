package com.sap.mm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_stock")
public class Stock extends BaseEntity {
    @TableId
    private String matnr;
    private String werks;
    private String lgort;
    private BigDecimal unrestrictedQty;
    @TableField("value") private BigDecimal stockValue;
}
