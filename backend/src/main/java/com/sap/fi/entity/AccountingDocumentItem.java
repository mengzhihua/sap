package com.sap.fi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_acc_document_item")
public class AccountingDocumentItem extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String belnr;
    private String buzei;
    private String bschl;
    private String shkzg;
    private String saknr;
    private String lifnr;
    private String kunnr;
    private String kostl;
    private BigDecimal amount;
    @TableField("text") private String itemText;
}
