package com.example.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.permission.entity.Permission;
import com.example.permission.mapper.PermissionMapper;
import com.example.permission.mapper.RolePermissionMapper;
import com.example.permission.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 权限服务类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供权限相关的核心业务逻辑：
 * - 基于Redis缓存的用户权限查询（减少数据库访问压力）
 * - 用户角色查询
 * - 权限树构建（支持多层级树形结构）
 * - 权限校验（单个权限、任意权限、全部权限）
 * - 权限缓存清理
 * </p>
 * <p>
 * 缓存策略：
 * - 权限缓存key格式：permission:user:{tenantId}:{userId}
 * - 角色缓存key格式：role:user:{tenantId}:{userId}
 * - 缓存有效期：2小时
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService extends ServiceImpl<PermissionMapper, Permission> {

    /** 用户-角色关联数据访问层 */
    private final UserRoleMapper userRoleMapper;

    /** 角色-权限关联数据访问层 */
    private final RolePermissionMapper rolePermissionMapper;

    /** Redis模板，用于权限和角色的缓存操作 */
    private final RedisTemplate<String, Object> redisTemplate;

    /** 权限缓存key前缀，完整格式：permission:user:{tenantId}:{userId} */
    private static final String PERMISSION_CACHE_KEY = "permission:user:";

    /** 角色缓存key前缀，完整格式：role:user:{tenantId}:{userId} */
    private static final String ROLE_CACHE_KEY = "role:user:";

    /** 缓存过期时间（小时） */
    private static final long CACHE_EXPIRE_HOURS = 2;

    /**
     * 获取用户的所有权限编码集合
     * <p>
     * 先从Redis缓存中获取，如果缓存未命中则查询数据库：
     * 1. 根据用户ID查询其拥有的所有角色ID
     * 2. 根据角色ID列表查询这些角色关联的所有权限编码
     * 3. 将结果写入Redis缓存，有效期2小时
     * </p>
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 用户拥有的权限编码集合（如 "user:create"、"order:delete" 等）
     */
    @SuppressWarnings("unchecked")
    public Set<String> getUserPermissions(Long userId, Long tenantId) {
        String cacheKey = PERMISSION_CACHE_KEY + tenantId + ":" + userId;

        // 尝试从Redis缓存中获取权限数据
        Set<String> cachedPermissions = (Set<String>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedPermissions != null) {
            return cachedPermissions;
        }

        // 根据用户ID查询其关联的所有角色ID
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return Collections.emptySet();
        }

        // 根据角色ID列表批量查询对应的权限编码
        List<String> permissionCodes = rolePermissionMapper.selectPermissionCodesByRoleIds(roleIds);
        Set<String> permissions = new HashSet<>(permissionCodes);

        // 将查询结果写入Redis缓存
        redisTemplate.opsForValue().set(cacheKey, permissions, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return permissions;
    }

    /**
     * 获取用户的所有角色编码集合
     * <p>
     * 先从Redis缓存中获取，如果缓存未命中则查询数据库：
     * 1. 根据用户ID查询其拥有的所有角色编码
     * 2. 将结果写入Redis缓存，有效期2小时
     * </p>
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 用户拥有的角色编码集合（如 "ADMIN"、"EDITOR" 等）
     */
    @SuppressWarnings("unchecked")
    public Set<String> getUserRoles(Long userId, Long tenantId) {
        String cacheKey = ROLE_CACHE_KEY + tenantId + ":" + userId;

        // 尝试从Redis缓存中获取角色数据
        Set<String> cachedRoles = (Set<String>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedRoles != null) {
            return cachedRoles;
        }

        // 从数据库查询用户关联的角色编码
        List<String> roleCodes = userRoleMapper.selectRoleCodesByUserId(userId);
        Set<String> roles = new HashSet<>(roleCodes);

        // 将查询结果写入Redis缓存
        redisTemplate.opsForValue().set(cacheKey, roles, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return roles;
    }

    /**
     * 清除用户权限缓存
     * <p>
     * 当用户的角色或权限发生变更时调用，删除该用户的权限缓存和角色缓存，
     * 确保下次查询时从数据库获取最新数据
     * </p>
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     */
    public void clearUserPermissionCache(Long userId, Long tenantId) {
        String permissionKey = PERMISSION_CACHE_KEY + tenantId + ":" + userId;
        String roleKey = ROLE_CACHE_KEY + tenantId + ":" + userId;
        // 删除权限缓存和角色缓存
        redisTemplate.delete(permissionKey);
        redisTemplate.delete(roleKey);
        log.info("已清除用户权限缓存，用户ID: {}", userId);
    }

    /**
     * 获取权限树
     * <p>
     * 查询指定租户下的所有有效权限（未删除的），按排序字段升序排列，
     * 然后构建成树形结构返回（顶级节点的parentId为0）
     * </p>
     *
     * @param tenantId 租户ID
     * @return 权限树列表（顶级权限节点）
     */
    public List<Permission> getPermissionTree(Long tenantId) {
        // 查询该租户下所有有效的权限记录，按排序字段升序排列
        List<Permission> allPermissions = list(
            new LambdaQueryWrapper<Permission>()
                .eq(Permission::getTenantId, tenantId)
                .eq(Permission::getDeleted, 0)
                .orderByAsc(Permission::getSort)
        );

        // 以parentId=0为根节点构建树形结构
        return buildTree(allPermissions, 0L);
    }

    /**
     * 递归构建权限树
     * <p>
     * 从权限列表中筛选出指定父ID的权限节点，
     * 并递归为每个节点设置子节点列表
     * </p>
     *
     * @param permissions 所有权限列表
     * @param parentId    父权限ID
     * @return 当前层级的权限节点列表
     */
    private List<Permission> buildTree(List<Permission> permissions, Long parentId) {
        List<Permission> tree = new ArrayList<>();

        for (Permission permission : permissions) {
            // 找到当前父节点的子节点
            if (Objects.equals(permission.getParentId(), parentId)) {
                // 递归查找子节点
                List<Permission> children = buildTree(permissions, permission.getId());
                permission.setChildren(children);
                tree.add(permission);
            }
        }

        return tree;
    }

    /**
     * 校验用户是否拥有指定权限
     * <p>
     * 如果用户拥有通配符权限（"*"），则视为拥有所有权限，直接返回true
     * </p>
     *
     * @param userId         用户ID
     * @param tenantId       租户ID
     * @param permissionCode 待校验的权限编码
     * @return 是否拥有该权限
     */
    public boolean hasPermission(Long userId, Long tenantId, String permissionCode) {
        Set<String> permissions = getUserPermissions(userId, tenantId);
        // 判断是否拥有指定权限或通配符权限
        return permissions.contains(permissionCode) || permissions.contains("*");
    }

    /**
     * 校验用户是否拥有指定权限中的任意一个
     * <p>
     * 如果用户拥有通配符权限（"*"），直接返回true。
     * 否则检查用户是否拥有传入权限列表中的任意一个
     * </p>
     *
     * @param userId           用户ID
     * @param tenantId         租户ID
     * @param permissionCodes  待校验的权限编码列表
     * @return 是否拥有任意一个权限
     */
    public boolean hasAnyPermission(Long userId, Long tenantId, String... permissionCodes) {
        Set<String> permissions = getUserPermissions(userId, tenantId);
        // 通配符权限拥有所有权限
        if (permissions.contains("*")) {
            return true;
        }
        // 检查是否拥有任意一个指定权限
        for (String code : permissionCodes) {
            if (permissions.contains(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验用户是否拥有所有指定权限
     * <p>
     * 如果用户拥有通配符权限（"*"），直接返回true。
     * 否则检查用户是否拥有传入权限列表中的全部权限
     * </p>
     *
     * @param userId           用户ID
     * @param tenantId         租户ID
     * @param permissionCodes  待校验的权限编码列表
     * @return 是否拥有全部权限
     */
    public boolean hasAllPermissions(Long userId, Long tenantId, String... permissionCodes) {
        Set<String> permissions = getUserPermissions(userId, tenantId);
        // 通配符权限拥有所有权限
        if (permissions.contains("*")) {
            return true;
        }
        // 检查是否拥有所有指定权限
        for (String code : permissionCodes) {
            if (!permissions.contains(code)) {
                return false;
            }
        }
        return true;
    }
}
