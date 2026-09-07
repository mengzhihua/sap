package com.sap.co.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_co_document")
public class CoDocument extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String fiBelnr;
    private String kostl;
    private String costElement;
    private BigDecimal amount;
    private java.time.LocalDate budat;
    @TableField("text") private String documentText;
}
