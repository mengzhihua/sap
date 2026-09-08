package com.sap.fi.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sap_posting_period")
public class PostingPeriod {
    @TableId private Long id;
    private String bukrs;
    private Integer fiscalYear;
    private Integer fromPeriod;
    private Integer toPeriod;
    private Boolean open;
}
