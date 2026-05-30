package com.example.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.permission.entity.Permission;
import com.example.permission.entity.Role;
import com.example.permission.entity.RoleInheritance;

import java.util.List;
import java.util.Map;

/**
 * 角色继承服务接口
 */
public interface RoleInheritanceService extends IService<RoleInheritance> {

    void setParent(Long childRoleId, Long parentRoleId, Long tenantId);

    void removeParent(Long childRoleId, Long parentRoleId);

    List<Role> getParentRoles(Long roleId);

    List<Permission> getInheritedPermissions(Long roleId);

    List<Permission> getAllEffectivePermissions(Long roleId);

    List<Map<String, Object>> getInheritanceTree(Long roleId);
}
