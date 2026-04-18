package com.example.dict.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统参数配置实体
 */
@Data
@TableName("sys_config")
public class SystemConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 配置编码
     */
    private String configCode;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置类型: 1-系统, 2-业务
     */
    private Integer configType;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 是否系统内置: 0-否, 1-是
     */
    private Integer isSystem;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 软删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
