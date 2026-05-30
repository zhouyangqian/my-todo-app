package com.example.permission.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.permission.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService extends IService<Role> {

    Page<Role> getRolePage(Long tenantId, int page, int size, String roleName);

    List<Role> getRolesByUserId(Long userId);

    void assignRolesToUser(Long userId, Long tenantId, List<Long> roleIds, Long operatorId);

    void assignPermissionsToRole(Long roleId, Long tenantId, List<Long> permissionIds, Long operatorId);

    List<Long> getPermissionIdsByRoleId(Long roleId);

    Role createRole(Role role);

    Role updateRole(Role role);

    void deleteRole(Long roleId);

    boolean hasCircularInheritance(Long roleId, Long parentId);

    List<Long> getInheritedPermissionIds(Long roleId);

    List<Role> getInheritanceChain(Long roleId);
}
