package com.sap.sd.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_delivery_item")
public class DeliveryItem extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String vbeln;
    private String posnr;
    private String matnr;
    private BigDecimal qty;
    private BigDecimal pickedQty;
    private BigDecimal pgiQty;
}
