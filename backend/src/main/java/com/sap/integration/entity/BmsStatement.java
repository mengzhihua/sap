package com.sap.integration.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_bms_statement")
public class BmsStatement extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String statementNo;
    private String direction;
    private String partnerCode;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private java.time.LocalDate bizDate;
    private String remark;
    private String belnr;
}
