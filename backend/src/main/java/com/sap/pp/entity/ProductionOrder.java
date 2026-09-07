package com.sap.pp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data @TableName("sap_production_order")
public class ProductionOrder extends BaseEntity {
    @TableId private String aufnr;
    private String matnr;
    private String werks;
    private BigDecimal targetQty;
    private BigDecimal deliveredQty;
    private String status;
    private BigDecimal plannedCost;
    private BigDecimal actualCost;
    @TableField(exist = false)
    private List<ProductionOrderComponent> components;
}
