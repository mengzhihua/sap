package com.sap.mm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;
import java.util.List;

@Data @TableName("sap_material_document")
public class MaterialDocument extends BaseEntity {
    @TableId private String mblnr;
    private String mjahr;
    private java.time.LocalDate budat;
    private String bwart;
    private String refType;
    private String refNo;
    private String fiBelnr;
    @TableField(exist = false)
    private List<MaterialDocumentItem> items;
}
