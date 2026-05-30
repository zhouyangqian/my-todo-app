package com.example.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.permission.entity.Permission;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 */
public interface PermissionService extends IService<Permission> {

    Set<String> getUserPermissions(Long userId, Long tenantId);

    Set<String> getUserRoles(Long userId, Long tenantId);

    void clearUserPermissionCache(Long userId, Long tenantId);

    List<Permission> getPermissionTree(Long tenantId);

    boolean hasPermission(Long userId, Long tenantId, String permissionCode);

    boolean hasAnyPermission(Long userId, Long tenantId, String... permissionCodes);

    boolean hasAllPermissions(Long userId, Long tenantId, String... permissionCodes);
}
