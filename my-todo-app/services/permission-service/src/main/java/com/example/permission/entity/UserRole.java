package com.example.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户-角色关联实体类
 * <p>
 * 对应数据库表 sys_user_role，用于存储用户与角色的关联关系。
 * 一个用户可以拥有多个角色（多对多关系的中间表）。
 * 支持多租户隔离，通过 tenantId 区分不同租户的关联数据。
 * </p>
 */
@Data
@TableName("sys_user_role")
public class UserRole implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /** 关联记录主键ID，自增长 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID，用于多租户数据隔离，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 用户ID，关联用户服务中的用户主键 */
    private Long userId;

    /** 角色ID，关联 sys_role 表的主键 */
    private Long roleId;

    /** 创建人ID，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
