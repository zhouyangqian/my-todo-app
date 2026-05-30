package com.example.permission.api.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建/更新菜单请求 VO
 */
@Data
public class MenuCreateVO {

    /** 父菜单ID */
    private Long parentId;

    /** 菜单名称 */
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    /** 菜单类型：1=目录 2=菜单 3=按钮 */
    @NotNull(message = "菜单类型不能为空")
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
}
