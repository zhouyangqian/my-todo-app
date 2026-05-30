package com.example.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色继承关系实体类
 * <p>
 * 对应数据库表 sys_role_inheritance，用于定义角色之间的继承关系。
 * 子角色自动获得父角色的所有权限，支持多级继承（最多5级）。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_role_inheritance")
public class RoleInheritance implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID，自增长 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID，用于多租户数据隔离 */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 子角色ID */
    private Long childRoleId;

    /** 父角色ID */
    private Long parentRoleId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
