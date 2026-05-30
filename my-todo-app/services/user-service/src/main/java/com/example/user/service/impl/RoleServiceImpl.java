package com.example.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.user.entity.Role;
import com.example.user.entity.UserRole;
import com.example.user.mapper.RoleMapper;
import com.example.user.mapper.UserRoleMapper;
import com.example.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理业务逻辑服务实现类
 * <p>
 * 提供角色的增删改查、用户角色分配/移除等功能。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final UserRoleMapper userRoleMapper;

    /**
     * 创建角色（检查roleCode在租户内唯一性）
     *
     * @param role 角色信息
     * @return 创建后的角色
     */
    @Transactional
    @Override
    public Role createRole(Role role) {
        // 检查同一租户下角色编码是否已存在
        Role existing = getOne(
            new LambdaQueryWrapper<Role>()
                .eq(Role::getTenantId, role.getTenantId())
                .eq(Role::getRoleCode, role.getRoleCode())
                .eq(Role::getDeleted, 0)
        );
        if (existing != null) {
            throw new IllegalArgumentException("角色编码已存在: " + role.getRoleCode());
        }
        role.setStatus(1);
        role.setIsSystem(0);
        save(role);
        log.info("创建角色: {}", role.getRoleCode());
        return role;
    }

    /**
     * 更新角色信息
     *
     * @param role 角色信息（必须包含id）
     * @return 更新后的角色
     */
    @Transactional
    @Override
    public Role updateRole(Role role) {
        Role existing = getById(role.getId());
        if (existing == null) {
            throw new IllegalArgumentException("角色不存在: " + role.getId());
        }
        // 系统角色不允许修改编码
        if (existing.getIsSystem() != null && existing.getIsSystem() == 1 && role.getRoleCode() != null) {
            role.setRoleCode(existing.getRoleCode());
        }
        updateById(role);
        log.info("更新角色: id={}", role.getId());
        return role;
    }

    /**
     * 软删除角色
     *
     * @param roleId 角色ID
     */
    @Transactional
    @Override
    public void deleteRole(Long roleId) {
        Role role = getById(roleId);
        if (role != null) {
            if (role.getIsSystem() != null && role.getIsSystem() == 1) {
                throw new IllegalArgumentException("系统角色不允许删除");
            }
            role.setDeleted(1);
            updateById(role);
            log.info("删除角色: {}", role.getRoleCode());
        }
    }

    /**
     * 分页查询角色列表
     *
     * @param tenantId 租户ID
     * @param page     页码
     * @param size     每页大小
     * @param roleName 角色名称（模糊查询）
     * @return 分页结果
     */
    @Override
    public Page<Role> getRolePage(Long tenantId, int page, int size, String roleName) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getTenantId, tenantId)
               .eq(Role::getDeleted, 0);
        if (roleName != null && !roleName.isEmpty()) {
            wrapper.like(Role::getRoleName, roleName);
        }
        wrapper.orderByDesc(Role::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 为用户分配角色
     *
     * @param userId   用户ID
     * @param roleIds  角色ID列表
     * @param tenantId 租户ID
     */
    @Transactional
    @Override
    public void assignRolesToUser(Long userId, List<Long> roleIds, Long tenantId) {
        for (Long roleId : roleIds) {
            // 检查是否已分配
            UserRole existing = userRoleMapper.selectOne(
                new LambdaQueryWrapper<UserRole>()
                    .eq(UserRole::getUserId, userId)
                    .eq(UserRole::getRoleId, roleId)
                    .eq(UserRole::getTenantId, tenantId)
            );
            if (existing == null) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setTenantId(tenantId);
                userRole.setCreatedAt(LocalDateTime.now());
                userRoleMapper.insert(userRole);
            }
        }
        log.info("为用户分配角色: userId={}, roleIds={}", userId, roleIds);
    }

    /**
     * 移除用户的角色
     *
     * @param userId   用户ID
     * @param roleIds  角色ID列表
     * @param tenantId 租户ID
     */
    @Transactional
    @Override
    public void removeRolesFromUser(Long userId, List<Long> roleIds, Long tenantId) {
        userRoleMapper.delete(
            new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .in(UserRole::getRoleId, roleIds)
                .eq(UserRole::getTenantId, tenantId)
        );
        log.info("移除用户角色: userId={}, roleIds={}", userId, roleIds);
    }

    /**
     * 查询用户拥有的角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @Override
    public List<Role> getRolesByUserId(Long userId) {
        // 先查询用户角色关联
        List<UserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
        );
        if (userRoles.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = userRoles.stream()
            .map(UserRole::getRoleId)
            .collect(Collectors.toList());
        // 查询角色详情
        return list(
            new LambdaQueryWrapper<Role>()
                .in(Role::getId, roleIds)
                .eq(Role::getDeleted, 0)
        );
    }
}
