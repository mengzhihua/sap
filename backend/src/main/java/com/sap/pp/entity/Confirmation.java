package com.sap.pp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_confirmation")
public class Confirmation extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String aufnr;
    private BigDecimal qty;
    private java.time.LocalDate budat;
}
