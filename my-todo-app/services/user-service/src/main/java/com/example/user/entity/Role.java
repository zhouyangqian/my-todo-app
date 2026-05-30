package com.example.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体类
 * <p>
 * 对应数据库表 sys_role，存储角色基本信息，
 * 包括角色名称、编码、描述、状态等。支持多租户数据隔离和软删除。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class Role extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 角色名称 */
    private String roleName;

    /** 角色编码（租户内唯一） */
    private String roleCode;

    /** 角色描述 */
    private String description;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 是否系统角色：0-否，1-是（系统角色不可删除） */
    private Integer isSystem;
}
