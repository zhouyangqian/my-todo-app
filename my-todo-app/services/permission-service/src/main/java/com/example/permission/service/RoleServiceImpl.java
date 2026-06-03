package com.example.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.permission.entity.Role;
import com.example.permission.entity.RolePermission;
import com.example.permission.entity.UserRole;
import com.example.permission.mapper.RoleMapper;
import com.example.permission.mapper.RolePermissionMapper;
import com.example.permission.mapper.UserRoleMapper;
import com.example.common.core.util.CodeGenerateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供角色相关的核心业务逻辑：
 * - 角色的分页查询（支持按名称模糊搜索）
 * - 角色的创建、更新（含编码唯一性校验）、删除（逻辑删除）
 * - 给用户分配角色（先删后增策略，事务保证）
 * - 给角色分配权限（先删后增策略，事务保证）
 * - 查询用户拥有的角色列表
 * - 查询角色关联的权限ID列表
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    /** 用户-角色关联数据访问层 */
    private final UserRoleMapper userRoleMapper;

    /** 角色-权限关联数据访问层 */
    private final RolePermissionMapper rolePermissionMapper;

    /**
     * 分页查询角色列表
     * <p>
     * 根据租户ID查询该租户下的角色列表，支持按角色名称模糊搜索，
     * 结果按排序字段升序排列，只查询未删除的角色
     * </p>
     *
     * @param tenantId 租户ID
     * @param page     当前页码
     * @param size     每页条数
     * @param roleName 角色名称（可选，用于模糊搜索）
     * @return 角色分页数据
     */
    @Override
    public Page<Role> getRolePage(Long tenantId, int page, int size, String roleName) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        // 过滤条件：租户ID匹配 + 未删除
        wrapper.eq(Role::getTenantId, tenantId)
               .eq(Role::getDeleted, 0);
        // 如果传入了角色名称，则进行模糊查询
        if (roleName != null && !roleName.isEmpty()) {
            wrapper.like(Role::getRoleName, roleName);
        }
        // 按排序字段升序排列
        wrapper.orderByAsc(Role::getSort);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 根据用户ID查询其拥有的所有角色
     *
     * @param userId 用户ID
     * @return 该用户拥有的角色列表，如果没有则返回空列表
     */
    @Override
    public List<Role> getRolesByUserId(Long userId) {
        // 先查询用户关联的角色ID列表
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        // 根据角色ID列表批量查询角色详情
        return listByIds(roleIds);
    }

    /**
     * 给用户分配角色
     * <p>
     * 采用"先删后增"策略：
     * 1. 先删除该用户原有的所有角色关联记录
     * 2. 再批量插入新的角色关联记录
     * 整个操作在事务中执行，确保数据一致性
     * </p>
     *
     * @param userId     目标用户ID
     * @param tenantId   租户ID
     * @param roleIds    待分配的角色ID列表
     * @param operatorId 当前操作用户ID
     */
    @Override
    @Transactional
    public void assignRolesToUser(Long userId, Long tenantId, List<Long> roleIds, Long operatorId) {
        // 先删除该用户原有的所有角色关联
        userRoleMapper.delete(
            new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
        );

        // 批量插入新的角色关联记录
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setTenantId(tenantId);
            userRole.setCreatedBy(operatorId);
            userRoleMapper.insert(userRole);
        }
        log.info("已为用户 {} 分配角色 {}", userId, roleIds);
    }

    /**
     * 给角色分配权限
     * <p>
     * 采用"先删后增"策略：
     * 1. 先删除该角色原有的所有权限关联记录
     * 2. 再批量插入新的权限关联记录
     * 整个操作在事务中执行，确保数据一致性
     * </p>
     *
     * @param roleId        目标角色ID
     * @param tenantId      租户ID
     * @param permissionIds 待分配的权限ID列表
     * @param operatorId    当前操作用户ID
     */
    @Override
    @Transactional
    public void assignPermissionsToRole(Long roleId, Long tenantId, List<Long> permissionIds, Long operatorId) {
        // 先删除该角色原有的所有权限关联
        rolePermissionMapper.delete(
            new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId)
        );

        // 批量插入新的权限关联记录
        for (Long permissionId : permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermission.setTenantId(tenantId);
            rolePermission.setCreatedBy(operatorId);
            rolePermissionMapper.insert(rolePermission);
        }
        log.info("已为角色 {} 分配权限 {}", roleId, permissionIds);
    }

    /**
     * 查询角色关联的权限ID列表
     *
     * @param roleId 角色ID
     * @return 该角色关联的权限ID列表
     */
    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        // 查询该角色的所有权限关联记录
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
            new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId)
        );
        // 提取权限ID列表
        return rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());
    }

    /**
     * 创建角色
     * <p>
     * 创建前会校验角色编码在同一租户下的唯一性，
     * 如果编码已存在则抛出 IllegalArgumentException
     * </p>
     *
     * @param role 角色实体对象
     * @return 创建成功的角色对象
     * @throws IllegalArgumentException 角色编码已存在时抛出
     */
    @Override
    @Transactional
    public Role createRole(Role role) {
        // 自动生成角色编码（格式：ROLE-拼音首字母-时间戳）
        String generatedCode = CodeGenerateUtil.generate("ROLE",
                role.getRoleName(),
                code -> getOne(new LambdaQueryWrapper<Role>()
                        .eq(Role::getTenantId, role.getTenantId())
                        .eq(Role::getRoleCode, code)
                        .eq(Role::getDeleted, 0)) != null
        );
        role.setRoleCode(generatedCode);
        save(role);
        return role;
    }

    /**
     * 更新角色
     * <p>
     * 更新前会校验角色编码在同一租户下的唯一性（排除自身），
     * 如果编码已被其他角色使用则抛出 IllegalArgumentException
     * </p>
     *
     * @param role 角色实体对象（包含待更新的字段和角色ID）
     * @return 更新后的角色对象
     * @throws IllegalArgumentException 角色编码已被其他角色使用时抛出
     */
    @Override
    @Transactional
    public Role updateRole(Role role) {
        // 校验角色编码在同一租户下是否已被其他角色使用（排除自身）
        Role existing = getOne(
            new LambdaQueryWrapper<Role>()
                .eq(Role::getTenantId, role.getTenantId())
                .eq(Role::getRoleCode, role.getRoleCode())
                .ne(Role::getId, role.getId())
                .eq(Role::getDeleted, 0)
        );
        if (existing != null) {
            throw new IllegalArgumentException("角色编码已存在: " + role.getRoleCode());
        }
        updateById(role);
        return role;
    }

    /**
     * 删除角色（逻辑删除）
     * <p>
     * 将角色的 deleted 字段设置为1（逻辑删除），
     * 同时清理该角色关联的用户-角色关系和角色-权限关系，
     * 整个操作在事务中执行
     * </p>
     *
     * @param roleId 待删除的角色ID
     */
    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = getById(roleId);
        if (role != null) {
            // 逻辑删除角色（设置deleted=1）
            role.setDeleted(1);
            updateById(role);

            // 清理该角色关联的用户-角色关联记录
            userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>()
                    .eq(UserRole::getRoleId, roleId)
            );

            // 清理该角色关联的角色-权限关联记录
            rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>()
                    .eq(RolePermission::getRoleId, roleId)
            );
        }
    }

    // ==================== 角色继承相关 ====================

    /** 最大继承层级 */
    private static final int MAX_INHERITANCE_DEPTH = 5;

    /**
     * 检测角色继承是否会导致循环继承
     *
     * @param roleId   当前角色ID
     * @param parentId 要设置的父角色ID
     * @return true-存在循环，false-无循环
     */
    @Override
    public boolean hasCircularInheritance(Long roleId, Long parentId) {
        if (parentId == null || parentId == 0L) {
            return false;
        }
        Set<Long> visited = new HashSet<>();
        visited.add(roleId);
        Long current = parentId;
        int depth = 0;

        while (current != null && current != 0L) {
            if (visited.contains(current)) {
                return true;
            }
            visited.add(current);
            depth++;
            if (depth > MAX_INHERITANCE_DEPTH) {
                return true;
            }
            Role parent = getById(current);
            current = parent != null ? parent.getParentId() : null;
        }
        return false;
    }

    /**
     * 获取角色的所有继承权限（包含父角色的权限）
     * <p>
     * 递归向上查找父角色，合并所有层级的权限ID。
     * 最多递归5层，防止无限循环。
     * </p>
     *
     * @param roleId 角色ID
     * @return 合并后的权限ID列表（去重）
     */
    @Override
    public List<Long> getInheritedPermissionIds(Long roleId) {
        Set<Long> allPermissions = new LinkedHashSet<>();
        collectInheritedPermissions(roleId, allPermissions, 0);
        return new ArrayList<>(allPermissions);
    }

    private void collectInheritedPermissions(Long roleId, Set<Long> permissions, int depth) {
        if (roleId == null || depth > MAX_INHERITANCE_DEPTH) {
            return;
        }
        // 收集当前角色的权限
        List<Long> rolePerms = getPermissionIdsByRoleId(roleId);
        permissions.addAll(rolePerms);

        // 递归收集父角色权限
        Role role = getById(roleId);
        if (role != null && role.getParentId() != null && role.getParentId() != 0L) {
            collectInheritedPermissions(role.getParentId(), permissions, depth + 1);
        }
    }

    /**
     * 获取角色的继承链（从当前角色到顶级角色）
     *
     * @param roleId 角色ID
     * @return 继承链列表，按层级从子到父排序
     */
    @Override
    public List<Role> getInheritanceChain(Long roleId) {
        List<Role> chain = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Long currentId = roleId;

        while (currentId != null && currentId != 0L && !visited.contains(currentId)) {
            visited.add(currentId);
            Role role = getById(currentId);
            if (role == null) break;
            chain.add(role);
            currentId = role.getParentId();
        }
        return chain;
    }
}
