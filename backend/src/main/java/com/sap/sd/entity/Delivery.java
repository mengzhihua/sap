package com.sap.sd.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.util.List;

@Data @TableName("sap_delivery")
public class Delivery extends BaseEntity {
    @TableId private String vbeln;
    private String soVbeln;
    private String kunnr;
    private String werks;
    private String status;
    private String materialDoc;
    private Integer bmsSynced;
    private String bmsDocNo;
    @TableField(exist = false)
    private List<DeliveryItem> items;
}
