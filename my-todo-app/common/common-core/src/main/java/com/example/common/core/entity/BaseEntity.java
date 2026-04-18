package com.example.common.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 * <p>
 * 所有数据库实体的公共父类，包含主键、租户ID、软删除标记、
 * 创建/更新时间等通用字段。MyBatis-Plus 会自动填充标记了 fill 的字段。
 * </p>
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID，用于多租户数据隔离，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 软删除标记：0-未删除，1-已删除，插入时自动填充 */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /** 创建人用户ID，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新人用户ID，更新时自动填充 */
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
