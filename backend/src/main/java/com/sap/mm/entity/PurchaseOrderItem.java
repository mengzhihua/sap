package com.sap.mm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @TableName("sap_purchase_order_item")
public class PurchaseOrderItem extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String ebeln;
    private String ebelp;
    private String matnr;
    private String werks;
    private String lgort;
    private BigDecimal menge;
    private BigDecimal netpr;
    private BigDecimal deliveredQty;
    private BigDecimal invoicedQty;
    private LocalDate deliveryDate;
}
