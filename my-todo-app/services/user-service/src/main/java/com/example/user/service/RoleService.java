package com.example.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.user.entity.Role;

import java.util.List;

/**
 * 角色管理业务逻辑服务接口
 */
public interface RoleService extends IService<Role> {

    Role createRole(Role role);

    Role updateRole(Role role);

    void deleteRole(Long roleId);

    Page<Role> getRolePage(Long tenantId, int page, int size, String roleName);

    void assignRolesToUser(Long userId, List<Long> roleIds, Long tenantId);

    void removeRolesFromUser(Long userId, List<Long> roleIds, Long tenantId);

    List<Role> getRolesByUserId(Long userId);
}
