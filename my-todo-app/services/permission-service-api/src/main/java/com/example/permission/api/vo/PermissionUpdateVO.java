package com.example.permission.api.vo;

import lombok.Data;

/**
 * 更新权限请求VO
 * <p>
 * 用于动态更新权限节点时接收前端传递的参数，仅允许修改名称和描述
 * </p>
 */
@Data
public class PermissionUpdateVO {

    /** 权限名称 */
    private String name;

    /** 权限描述 */
    private String description;
}
