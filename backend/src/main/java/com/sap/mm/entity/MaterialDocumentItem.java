package com.sap.mm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data @TableName("sap_material_document_item")
public class MaterialDocumentItem extends BaseEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String mblnr;
    private String zeile;
    private String matnr;
    private String werks;
    private String lgort;
    private BigDecimal menge;
    private BigDecimal amount;
    private String bwart;
    private String ebeln;
    private String ebelp;
    private String kostl;
    private String aufnr;
    private String toLgort;
}
