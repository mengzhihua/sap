package com.sap.mm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_gr_ir")
public class GrIr extends BaseEntity {
    @TableId private String ebeln;
    @TableField("ebelp") private String ebelp;
    private BigDecimal grQty;
    private BigDecimal irQty;
    private BigDecimal grAmount;
    private BigDecimal irAmount;
}
