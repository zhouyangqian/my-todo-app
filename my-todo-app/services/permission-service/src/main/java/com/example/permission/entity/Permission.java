package com.example.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限实体类
 * <p>
 * 对应数据库表 sys_permission，用于存储系统中的权限资源信息。
 * 权限分为三种类型：菜单权限、按钮权限、API接口权限。
 * 支持树形结构的权限层级，通过 parentId 实现父子关系。
 * 支持多租户隔离，通过 tenantId 区分不同租户的权限数据。
 * </p>
 */
@Data
@TableName("sys_permission")
public class Permission implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /** 权限主键ID，自增长 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID，用于多租户数据隔离，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 父权限ID，用于构建权限树形结构，顶级权限的parentId为0 */
    private Long parentId;

    /** 权限编码，全局唯一标识权限（如 "user:create"、"order:delete"） */
    private String permissionCode;

    /** 权限名称，用于界面展示（如 "创建用户"、"删除订单"） */
    private String permissionName;

    /** 权限类型：1-菜单权限，2-按钮权限，3-API接口权限 */
    private Integer permissionType;

    /** 资源路径（菜单对应的URL或API接口的请求路径） */
    private String resourcePath;

    /** HTTP请求方法（仅API权限使用，如 GET、POST、PUT、DELETE） */
    private String httpMethod;

    /** 菜单图标（前端展示用的图标类名或图标路径） */
    private String icon;

    /** 菜单路径（前端路由路径，如 /system/user） */
    private String menuPath;

    /** 组件路径（前端Vue组件路径，如 system/UserList） */
    private String component;

    /** 排序序号，值越小越靠前 */
    private Integer sort;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 是否在菜单中可见：0-隐藏，1-显示 */
    private Integer visible;

    /** 逻辑删除标志：0-未删除，1-已删除，使用MyBatis-Plus逻辑删除自动处理 */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /** 创建人ID，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 最后更新人ID，更新时自动填充 */
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /** 最后更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
