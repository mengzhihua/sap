package com.sap.fi.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sap.common.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("sap_fixed_asset")
public class FixedAsset extends BaseEntity {
    @TableId private String anln1;
    private String name;
    private String assetClass;
    private String bukrs;
    private String kostl;
    private LocalDate capitalizationDate;
    private Integer usefulLifeMonths;
    private BigDecimal acquisitionValue;
    private BigDecimal accumulatedDepreciation;
    private BigDecimal bookValue;
    private BigDecimal salvageValue;
    private String status;
}
