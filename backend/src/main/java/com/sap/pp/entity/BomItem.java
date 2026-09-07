package com.sap.pp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_bom_item")
public class BomItem extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String matnr;
    private String werks;
    private String component;
    private BigDecimal qty;
}
