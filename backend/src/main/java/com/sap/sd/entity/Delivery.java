package com.sap.sd.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

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
}
