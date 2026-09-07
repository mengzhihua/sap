package com.sap.pp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_production_order_component")
public class ProductionOrderComponent extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String aufnr;
    private String matnr;
    private BigDecimal reqQty;
    private BigDecimal issuedQty;
}
