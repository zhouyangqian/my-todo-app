package com.example.permission.service;

/**
 * 租户权限初始化服务
 * <p>
 * 在新租户注册时，从模板租户（tenantId=1）复制默认角色和权限到新租户，
 * 并将管理员用户绑定到管理员角色。
 * </p>
 */
public interface TenantPermissionInitService {

    /**
     * 初始化新租户的默认角色和权限
     *
     * @param tenantId     新租户ID
     * @param adminUserId  管理员用户ID
     * @param adminUsername 管理员用户名
     */
    void initTenantPermissions(Long tenantId, Long adminUserId, String adminUsername);
}
