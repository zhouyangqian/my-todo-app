package com.example.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色-权限关联实体类
 * <p>
 * 对应数据库表 sys_role_permission，用于存储角色与权限的关联关系。
 * 一个角色可以关联多个权限（多对多关系的中间表）。
 * 支持多租户隔离，通过 tenantId 区分不同租户的关联数据。
 * </p>
 */
@Data
@TableName("sys_role_permission")
public class RolePermission implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /** 关联记录主键ID，自增长 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID，用于多租户数据隔离，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 角色ID，关联 sys_role 表的主键 */
    private Long roleId;

    /** 权限ID，关联 sys_permission 表的主键 */
    private Long permissionId;

    /** 创建人ID，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
