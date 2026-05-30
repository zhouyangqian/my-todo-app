package com.example.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单实体类
 * <p>
 * 对应 sys_menu 表，支持树形结构的菜单层级。
 * menuType: 1=目录, 2=菜单, 3=按钮
 * </p>
 */
@Data
@TableName("sys_menu")
public class SysMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 父菜单ID，null表示根节点 */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 菜单类型：1=目录 2=菜单 3=按钮 */
    private Integer menuType;

    /** 路由路径 */
    private String path;

    /** 前端组件路径 */
    private String component;

    /** 权限编码 */
    private String permissionCode;

    /** 图标 */
    private String icon;

    /** 排序序号 */
    private Integer sortOrder;

    /** 是否可见：0=隐藏 1=显示 */
    private Integer visible;

    /** 状态：0=禁用 1=启用 */
    private Integer status;

    /** 逻辑删除：0=未删除 1=已删除 */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /** 更新人 */
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;

    /** 子菜单列表（非数据库字段） */
    @TableField(exist = false)
    private List<SysMenu> children;
}
