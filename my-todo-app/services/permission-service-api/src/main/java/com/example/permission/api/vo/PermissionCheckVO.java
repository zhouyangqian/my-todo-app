package com.example.permission.api.vo;

import lombok.Data;

/**
 * 权限校验请求DTO
 * <p>
 * 用于校验用户是否拥有指定权限时接收前端传递的参数，
 * 包含待校验的用户ID和权限编码
 * </p>
 */
@Data
public class PermissionCheckVO {

    /** 待校验的用户ID */
    private Long userId;

    /** 待校验的权限编码（如 "user:create"） */
    private String permissionCode;
}
