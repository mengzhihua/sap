package com.sap.mm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_material")
public class Material extends BaseEntity {
    @TableId private String matnr;
    private String maktx;
    private String meins;
    private String mtart;
    private String matkl;
    private BigDecimal stdPrice;
    private String priceControl;
    private String aliasCode;
}
