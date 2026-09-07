package com.sap.integration.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_vendor_evaluation")
public class VendorEvaluation extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String lifnr;
    private String period;
    private BigDecimal score;
    private String grade;
}
