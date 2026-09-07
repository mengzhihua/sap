package com.sap.sd.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_sales_order_item")
public class SalesOrderItem extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String vbeln;
    private String posnr;
    private String matnr;
    private String werks;
    private BigDecimal kwmeng;
    private BigDecimal netpr;
    private BigDecimal deliveredQty;
    private BigDecimal billedQty;
}
