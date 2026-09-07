package com.sap.pp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_bom")
public class Bom extends BaseEntity {
    @TableId private String matnr;
    private String werks;
    private BigDecimal baseQty;
}
