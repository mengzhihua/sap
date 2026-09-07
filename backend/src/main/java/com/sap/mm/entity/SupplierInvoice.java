package com.sap.mm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_supplier_invoice")
public class SupplierInvoice extends BaseEntity {
    @TableId private String belnr;
    private String gjahr;
    private String lifnr;
    private String ebeln;
    private String externalInvoiceNo;
    private BigDecimal grossAmount;
    private BigDecimal taxAmount;
    private String status;
    private String fiBelnr;
    private String matchResult;
}
