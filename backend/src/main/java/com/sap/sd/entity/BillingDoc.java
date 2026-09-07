package com.sap.sd.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data @TableName("sap_billing_doc")
public class BillingDoc extends BaseEntity {
    @TableId private String vbeln;
    private String fkart;
    private String kunnr;
    private BigDecimal net;
    private BigDecimal tax;
    private BigDecimal gross;
    private String fiBelnr;
    private String status;
    @TableField(exist = false)
    private List<BillingDocItem> items;
}
