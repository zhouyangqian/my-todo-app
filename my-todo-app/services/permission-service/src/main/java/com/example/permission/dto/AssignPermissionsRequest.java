package com.example.permission.dto;

import lombok.Data;

import java.util.List;

/**
 * 分配权限请求DTO
 * <p>
 * 用于给角色分配权限时接收前端传递的参数，
 * 包含待分配的权限ID列表
 * </p>
 */
@Data
public class AssignPermissionsRequest {

    /** 待分配的权限ID列表 */
    private List<Long> permissionIds;
}
