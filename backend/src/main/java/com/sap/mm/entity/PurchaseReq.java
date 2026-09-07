package com.sap.mm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data @TableName("sap_purchase_req")
public class PurchaseReq extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String banfn;
    private String status;
    private String requester;
    private String bukrs;
    private String werks;
    private BigDecimal totalAmount;
    @TableField(exist = false)
    private List<PurchaseReqItem> items;
}
