package com.example.permission.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 继承权限DTO
 * <p>
 * 用于返回角色的继承权限信息，区分"继承"和"自有"权限
 * </p>
 */
@Data
public class InheritedPermissionDTO {

    /** 角色ID */
    private Long roleId;

    /** 角色名称 */
    private String roleName;

    /** 权限列表 */
    private List<PermissionItem> permissions;

    /** 来源：own-自有权限，inherited-继承权限 */
    private String source;

    /**
     * 权限项
     */
    @Data
    public static class PermissionItem {
        /** 权限ID */
        private Long permissionId;
        /** 权限编码 */
        private String permissionCode;
        /** 权限名称 */
        private String permissionName;
    }
}
