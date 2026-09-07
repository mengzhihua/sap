package com.sap.mm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_purchase_req_item")
public class PurchaseReqItem extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String banfn;
    private String bnfpo;
    private String matnr;
    private BigDecimal menge;
    private BigDecimal netpr;
    private String werks;
    private String lgort;
}
