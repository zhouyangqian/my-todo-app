package com.example.permission.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单树节点 DTO
 * <p>
 * 返回给前端的菜单信息，包含子节点列表。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDTO {

    /** 菜单ID */
    private Long id;

    /** 父菜单ID */
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

    /** 子菜单列表 */
    private List<MenuDTO> children;
}
