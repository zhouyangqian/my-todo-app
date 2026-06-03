package com.example.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.permission.entity.Permission;
import com.example.permission.entity.Role;
import com.example.permission.entity.RolePermission;
import com.example.permission.entity.UserRole;
import com.example.permission.mapper.PermissionMapper;
import com.example.permission.mapper.RoleMapper;
import com.example.permission.mapper.RolePermissionMapper;
import com.example.permission.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 租户权限初始化服务实现
 * <p>
 * 新租户注册时，从模板租户（tenantId=1）复制默认角色和权限结构：
 * 1. 复制所有权限记录到新租户，并建立新旧ID映射
 * 2. 复制默认角色（ADMIN、USER）到新租户
 * 3. 将复制后的权限分配给复制后的管理员角色
 * 4. 将管理员用户绑定到管理员角色
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantPermissionInitServiceImpl implements TenantPermissionInitService {

    /** 模板租户ID，默认权限和角色从此租户复制 */
    private static final Long TEMPLATE_TENANT_ID = 1L;

    private final PermissionMapper permissionMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    @Transactional
    public void initTenantPermissions(Long tenantId, Long adminUserId, String adminUsername) {
        log.info("开始初始化租户权限: tenantId={}, adminUserId={}", tenantId, adminUserId);

        // 1. 复制权限
        Map<Long, Long> permissionIdMap = copyPermissions(tenantId);

        // 2. 复制角色（ADMIN 和 USER）
        Map<String, Long> roleIdMap = copyRoles(tenantId);

        // 3. 给新租户的管理员角色分配权限
        assignPermissionsToAdminRole(tenantId, roleIdMap, permissionIdMap);

        // 4. 将管理员用户绑定到管理员角色
        bindAdminRoleToUser(tenantId, adminUserId, roleIdMap);

        log.info("租户权限初始化完成: tenantId={}, 复制权限{}个, 复制角色{}个",
                tenantId, permissionIdMap.size(), roleIdMap.size());
    }

    /**
     * 从模板租户复制所有权限到新租户
     *
     * @return 旧权限ID → 新权限ID 映射
     */
    private Map<Long, Long> copyPermissions(Long newTenantId) {
        // 查询模板租户的所有未删除权限
        List<Permission> templatePermissions = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getTenantId, TEMPLATE_TENANT_ID)
                        .eq(Permission::getDeleted, 0)
        );

        if (templatePermissions.isEmpty()) {
            log.warn("模板租户 tenantId={} 没有可用权限，跳过权限复制", TEMPLATE_TENANT_ID);
            return Collections.emptyMap();
        }

        // 先复制顶级权限（parentId=0），再复制子权限
        // 为保证父子关系正确，按 parentId 排序后逐条复制
        Map<Long, Long> idMap = new HashMap<>();

        // 先处理 parentId=0 的顶级权限
        for (Permission p : templatePermissions) {
            if (p.getParentId() == null || p.getParentId() == 0) {
                Long oldId = p.getId();
                Permission copy = copyPermission(p, newTenantId, idMap);
                permissionMapper.insert(copy);
                idMap.put(oldId, copy.getId());
            }
        }

        // 再处理有父级的权限
        for (Permission p : templatePermissions) {
            if (p.getParentId() != null && p.getParentId() > 0) {
                Permission copy = copyPermission(p, newTenantId, idMap);
                permissionMapper.insert(copy);
                idMap.put(p.getId(), copy.getId());
            }
        }

        log.info("已复制 {} 个权限从租户 {} 到租户 {}", idMap.size(), TEMPLATE_TENANT_ID, newTenantId);
        return idMap;
    }

    /**
     * 复制单个权限，替换 tenantId 和 parentId
     */
    private Permission copyPermission(Permission source, Long newTenantId, Map<Long, Long> idMap) {
        Permission copy = new Permission();
        copy.setTenantId(newTenantId);
        copy.setPermissionCode(source.getPermissionCode());
        copy.setPermissionName(source.getPermissionName());
        copy.setPermissionType(source.getPermissionType());
        copy.setResourcePath(source.getResourcePath());
        copy.setHttpMethod(source.getHttpMethod());
        copy.setIcon(source.getIcon());
        copy.setMenuPath(source.getMenuPath());
        copy.setComponent(source.getComponent());
        copy.setSort(source.getSort());
        copy.setStatus(source.getStatus());
        copy.setVisible(source.getVisible());

        // parentId 映射到新ID
        if (source.getParentId() != null && source.getParentId() > 0) {
            Long newParentId = idMap.get(source.getParentId());
            copy.setParentId(newParentId != null ? newParentId : 0L);
        } else {
            copy.setParentId(0L);
        }

        return copy;
    }

    /**
     * 从模板租户复制 ADMIN 和 USER 角色到新租户
     *
     * @return roleCode → 新角色ID 映射
     */
    private Map<String, Long> copyRoles(Long newTenantId) {
        // 查询模板租户的默认角色
        List<Role> templateRoles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getTenantId, TEMPLATE_TENANT_ID)
                        .in(Role::getRoleCode, List.of("ADMIN", "USER"))
                        .eq(Role::getDeleted, 0)
        );

        Map<String, Long> roleIdMap = new HashMap<>();

        for (Role templateRole : templateRoles) {
            Role newRole = new Role();
            newRole.setTenantId(newTenantId);
            newRole.setRoleCode(templateRole.getRoleCode());
            newRole.setRoleName(rewriteRoleName(templateRole.getRoleName()));
            newRole.setDescription(templateRole.getDescription());
            newRole.setParentId(templateRole.getParentId());
            newRole.setSort(templateRole.getSort());
            newRole.setStatus(templateRole.getStatus());
            newRole.setDataScope(templateRole.getDataScope());
            roleMapper.insert(newRole);

            roleIdMap.put(templateRole.getRoleCode(), newRole.getId());
            log.info("已复制角色: roleCode={}, newRoleId={}", templateRole.getRoleCode(), newRole.getId());
        }

        return roleIdMap;
    }

    /**
     * 重写角色名称（去除"系统"等前缀，使其更适合租户场景）
     */
    private String rewriteRoleName(String originalName) {
        if (originalName == null) return null;
        return originalName.replace("系统", "").replace("超级", "").trim();
    }

    /**
     * 查询模板租户中 ADMIN 角色拥有的权限ID列表，映射到新租户的权限ID，分配给新管理员角色
     */
    private void assignPermissionsToAdminRole(Long newTenantId, Map<String, Long> roleIdMap,
                                               Map<Long, Long> permissionIdMap) {
        Long newAdminRoleId = roleIdMap.get("ADMIN");
        if (newAdminRoleId == null) {
            log.warn("未找到 ADMIN 角色，跳过权限分配");
            return;
        }

        // 查询模板租户中 ADMIN 角色拥有的权限
        Long templateAdminRoleId = getTemplateRoleId("ADMIN");
        if (templateAdminRoleId == null) {
            log.warn("模板租户中未找到 ADMIN 角色，为新管理员角色分配所有可用权限");
            // 降级：分配所有复制过来的权限
            for (Long newPermissionId : permissionIdMap.values()) {
                RolePermission rp = new RolePermission();
                rp.setTenantId(newTenantId);
                rp.setRoleId(newAdminRoleId);
                rp.setPermissionId(newPermissionId);
                rolePermissionMapper.insert(rp);
            }
            return;
        }

        // 查询模板 ADMIN 角色的权限关联
        List<RolePermission> templateRolePermissions = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, templateAdminRoleId)
                        .eq(RolePermission::getTenantId, TEMPLATE_TENANT_ID)
        );

        int count = 0;
        for (RolePermission rp : templateRolePermissions) {
            Long newPermissionId = permissionIdMap.get(rp.getPermissionId());
            if (newPermissionId != null) {
                RolePermission newRp = new RolePermission();
                newRp.setTenantId(newTenantId);
                newRp.setRoleId(newAdminRoleId);
                newRp.setPermissionId(newPermissionId);
                rolePermissionMapper.insert(newRp);
                count++;
            }
        }

        log.info("已为管理员角色分配 {} 个权限", count);
    }

    /**
     * 将管理员用户绑定到 ADMIN 角色
     */
    private void bindAdminRoleToUser(Long newTenantId, Long adminUserId, Map<String, Long> roleIdMap) {
        Long adminRoleId = roleIdMap.get("ADMIN");
        if (adminRoleId == null) {
            log.warn("未找到 ADMIN 角色，跳过用户角色绑定");
            return;
        }

        UserRole userRole = new UserRole();
        userRole.setUserId(adminUserId);
        userRole.setRoleId(adminRoleId);
        userRole.setTenantId(newTenantId);
        userRoleMapper.insert(userRole);

        log.info("已将管理员用户 {} 绑定到 ADMIN 角色 {}", adminUserId, adminRoleId);
    }

    /**
     * 获取模板租户中指定 roleCode 的角色ID
     */
    private Long getTemplateRoleId(String roleCode) {
        Role role = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getTenantId, TEMPLATE_TENANT_ID)
                        .eq(Role::getRoleCode, roleCode)
                        .eq(Role::getDeleted, 0)
        );
        return role != null ? role.getId() : null;
    }
}
