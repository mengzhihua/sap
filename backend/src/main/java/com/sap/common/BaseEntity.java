package com.sap.common;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseEntity {
    @TableField(exist = false)
    private Long id;
    @TableField(exist = false)
    private LocalDateTime createdAt;
    @TableField(exist = false)
    private LocalDateTime updatedAt;
    @TableField(exist = false)
    private String createdBy;
}
