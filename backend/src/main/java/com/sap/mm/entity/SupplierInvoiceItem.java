package com.sap.mm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_supplier_invoice_item")
public class SupplierInvoiceItem extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String belnr;
    private String ebelp;
    private BigDecimal qty;
    private BigDecimal price;
}
