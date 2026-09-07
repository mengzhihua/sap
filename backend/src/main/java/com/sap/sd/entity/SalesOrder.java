package com.sap.sd.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data @TableName("sap_sales_order")
public class SalesOrder extends BaseEntity {
    @TableId private String vbeln;
    private String auart;
    private String kunnr;
    private String vkorg;
    private String waers;
    private String status;
}
