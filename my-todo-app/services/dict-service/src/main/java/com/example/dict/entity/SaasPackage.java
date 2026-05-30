package com.example.dict.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SaaS套餐实体
 */
@Data
@TableName("saas_package")
public class SaasPackage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 套餐名称 */
    private String packageName;

    /** 套餐编码 */
    private String packageCode;

    /** 描述 */
    private String description;

    /** 价格 */
    private BigDecimal price;

    /** 计费周期 */
    private String billingCycle;

    /** 最大用户数 */
    private Integer maxUsers;

    /** 最大存储(MB) */
    private Integer maxStorageMb;

    /** 功能列表(JSON) */
    private String features;

    /** 状态 */
    private Integer status;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
