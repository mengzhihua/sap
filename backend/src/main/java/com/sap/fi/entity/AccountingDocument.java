package com.sap.fi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.util.List;

@Data @TableName("sap_acc_document")
public class AccountingDocument extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String belnr;
    private String gjahr;
    private String bukrs;
    private String blart;
    private java.time.LocalDate budat;
    private java.time.LocalDate bldat;
    private String waers;
    private String headerText;
    private String refNo;
    private String source;
    private String reversedBy;
    @TableField("cleared_by")
    private String clearedBy;
    @TableField(exist = false)
    private List<AccountingDocumentItem> items;
}
