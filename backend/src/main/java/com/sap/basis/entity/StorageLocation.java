package com.sap.basis.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sap.common.BaseEntity;
import lombok.Data;

@Data @TableName("sap_storage_location")
public class StorageLocation extends BaseEntity {
    @TableId
    private String werks;
    private String lgort;
    private String name;
}
