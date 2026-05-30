package com.example.permission.api.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建权限请求VO
 * <p>
 * 用于动态创建权限节点时接收前端传递的参数
 * </p>
 */
@Data
public class PermissionCreateVO {

    /** 权限编码（如 system:user:create），全局唯一 */
    @NotBlank(message = "权限编码不能为空")
    private String code;

    /** 权限名称（如 "创建用户"），用于界面展示 */
    @NotBlank(message = "权限名称不能为空")
    private String name;

    /** 权限描述 */
    private String description;

    /** 父权限ID，不传或为null表示顶级权限 */
    private Long parentId;

    /** 层级，不传则自动根据父节点计算 */
    private Integer level;

    /** 权限类型：1-菜单，2-按钮，3-API，默认2 */
    private Integer permissionType;
}
