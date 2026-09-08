package com.sap.fi.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sap.common.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("sap_asset_transaction")
public class AssetTransaction extends BaseEntity {
    @TableId private Long id;
    private String anln1;
    private String transactionType;
    private String fiscalPeriod;
    private LocalDate postingDate;
    private BigDecimal amount;
    private String belnr;
    private String text;
}
