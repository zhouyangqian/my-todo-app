package com.example.permission.service;

import com.example.permission.api.dto.PermissionTreeDTO;
import com.example.permission.api.vo.PermissionCreateVO;
import com.example.permission.api.vo.PermissionUpdateVO;
import com.example.permission.entity.Permission;

import java.util.List;

/**
 * 动态权限服务接口
 */
public interface DynamicPermissionService {

    Permission createPermission(PermissionCreateVO vo, Long tenantId);

    Permission updatePermission(Long permissionId, PermissionUpdateVO vo, Long tenantId);

    void deletePermission(Long permissionId, Long tenantId);

    List<PermissionTreeDTO> getPermissionTree(Long tenantId);

    void invalidateCache(Long tenantId);
}
