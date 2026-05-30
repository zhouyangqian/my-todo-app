package com.example.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.permission.entity.Permission;
import com.example.permission.entity.Role;
import com.example.permission.entity.RoleInheritance;
import com.example.permission.entity.RolePermission;
import com.example.permission.mapper.PermissionMapper;
import com.example.permission.mapper.RoleInheritanceMapper;
import com.example.permission.mapper.RoleMapper;
import com.example.permission.mapper.RolePermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色继承服务实现类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供角色继承关系的业务逻辑：
 * - 设置/移除角色继承关系（含循环检测和深度检测）
 * - 查询父角色列表
 * - 递归获取继承的权限
 * - 获取所有有效权限（自有 + 继承）
 * - 获取继承树结构
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleInheritanceServiceImpl extends ServiceImpl<RoleInheritanceMapper, RoleInheritance> implements RoleInheritanceService {

    /** 最大继承深度 */
    private static final int MAX_INHERITANCE_DEPTH = 5;

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    /**
     * 设置父角色继承关系
     * <p>
     * 包含以下校验：
     * 1. 不能继承自己
     * 2. 循环检测：从parentRoleId开始向上遍历父链，如果发现childRoleId则抛异常
     * 3. 深度检测：遍历父链最多5级，超过抛异常
     * </p>
     *
     * @param childRoleId  子角色ID
     * @param parentRoleId 父角色ID
     * @param tenantId     租户ID
     */
    @Override
    @Transactional
    public void setParent(Long childRoleId, Long parentRoleId, Long tenantId) {
        // 不能继承自己
        if (childRoleId.equals(parentRoleId)) {
            throw new IllegalArgumentException("不能继承自己");
        }

        // 校验角色是否存在
        Role childRole = roleMapper.selectById(childRoleId);
        if (childRole == null) {
            throw new IllegalArgumentException("子角色不存在: " + childRoleId);
        }
        Role parentRole = roleMapper.selectById(parentRoleId);
        if (parentRole == null) {
            throw new IllegalArgumentException("父角色不存在: " + parentRoleId);
        }

        // 检查是否已存在继承关系
        long exists = count(
            new LambdaQueryWrapper<RoleInheritance>()
                .eq(RoleInheritance::getChildRoleId, childRoleId)
                .eq(RoleInheritance::getParentRoleId, parentRoleId)
        );
        if (exists > 0) {
            throw new IllegalArgumentException("该继承关系已存在");
        }

        // 循环检测：从parentRoleId开始向上遍历，检查是否会回到childRoleId
        checkCircularInheritance(childRoleId, parentRoleId);

        // 深度检测：计算继承后的深度是否超过限制
        checkInheritanceDepth(childRoleId, parentRoleId);

        // 创建继承关系
        RoleInheritance inheritance = new RoleInheritance();
        inheritance.setChildRoleId(childRoleId);
        inheritance.setParentRoleId(parentRoleId);
        inheritance.setTenantId(tenantId);
        save(inheritance);
        log.info("已设置角色继承: childRoleId={}, parentRoleId={}", childRoleId, parentRoleId);
    }

    /**
     * 移除继承关系
     *
     * @param childRoleId  子角色ID
     * @param parentRoleId 父角色ID
     */
    @Override
    @Transactional
    public void removeParent(Long childRoleId, Long parentRoleId) {
        remove(
            new LambdaQueryWrapper<RoleInheritance>()
                .eq(RoleInheritance::getChildRoleId, childRoleId)
                .eq(RoleInheritance::getParentRoleId, parentRoleId)
        );
        log.info("已移除角色继承: childRoleId={}, parentRoleId={}", childRoleId, parentRoleId);
    }

    /**
     * 获取指定角色的所有直接父角色
     *
     * @param roleId 角色ID
     * @return 父角色列表
     */
    @Override
    public List<Role> getParentRoles(Long roleId) {
        List<RoleInheritance> inheritances = list(
            new LambdaQueryWrapper<RoleInheritance>()
                .eq(RoleInheritance::getChildRoleId, roleId)
        );
        if (inheritances.isEmpty()) {
            return List.of();
        }
        List<Long> parentIds = inheritances.stream()
                .map(RoleInheritance::getParentRoleId)
                .collect(Collectors.toList());
        return roleMapper.selectBatchIds(parentIds);
    }

    /**
     * 获取继承的权限（递归，最多5级）
     * <p>
     * 从指定角色的父角色开始，递归向上收集所有继承的权限。
     * 返回的权限不包含角色自身的权限，仅包含从父角色链继承的权限。
     * </p>
     *
     * @param roleId 角色ID
     * @return 继承的权限列表（去重）
     */
    @Override
    public List<Permission> getInheritedPermissions(Long roleId) {
        Set<Long> collectedPermissionIds = new LinkedHashSet<>();
        Set<Long> visitedRoleIds = new HashSet<>();
        visitedRoleIds.add(roleId); // 避免回溯到自身
        collectParentPermissions(roleId, collectedPermissionIds, visitedRoleIds, 0);

        if (collectedPermissionIds.isEmpty()) {
            return List.of();
        }
        return queryPermissionsByIds(collectedPermissionIds);
    }

    /**
     * 获取所有有效权限（自身权限 + 继承权限）
     *
     * @param roleId 角色ID
     * @return 所有有效权限列表（去重）
     */
    @Override
    public List<Permission> getAllEffectivePermissions(Long roleId) {
        Set<Long> allPermissionIds = new LinkedHashSet<>();

        // 收集自身权限
        List<Long> ownPermissionIds = getPermissionIdsByRoleId(roleId);
        allPermissionIds.addAll(ownPermissionIds);

        // 收集继承的权限
        Set<Long> visitedRoleIds = new HashSet<>();
        visitedRoleIds.add(roleId);
        collectParentPermissions(roleId, allPermissionIds, visitedRoleIds, 0);

        if (allPermissionIds.isEmpty()) {
            return List.of();
        }
        return queryPermissionsByIds(allPermissionIds);
    }

    /**
     * 获取继承树
     * <p>
     * 返回以指定角色为根节点的继承树结构，
     * 包含所有父角色（递归向上）
     * </p>
     *
     * @param roleId 角色ID
     * @return 继承树（角色列表，含父角色信息）
     */
    @Override
    public List<Map<String, Object>> getInheritanceTree(Long roleId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        buildInheritanceTree(roleId, tree, visited, 0);
        return tree;
    }

    // ==================== 私有方法 ====================

    /**
     * 循环检测：从parentRoleId开始向上遍历父链
     */
    private void checkCircularInheritance(Long childRoleId, Long parentRoleId) {
        Set<Long> visited = new HashSet<>();
        visited.add(childRoleId);
        Long currentId = parentRoleId;

        while (currentId != null) {
            if (visited.contains(currentId)) {
                throw new IllegalArgumentException("存在循环继承，无法设置该继承关系");
            }
            visited.add(currentId);

            // 查找currentId的所有父角色
            List<RoleInheritance> parentRelations = list(
                new LambdaQueryWrapper<RoleInheritance>()
                    .eq(RoleInheritance::getChildRoleId, currentId)
            );
            if (parentRelations.isEmpty()) {
                break;
            }
            // 取第一个父角色继续向上遍历（简化为单继承检测）
            // 多父角色情况下也检测所有路径
            for (RoleInheritance ri : parentRelations) {
                checkCircularInheritance(childRoleId, ri.getParentRoleId());
            }
            break;
        }
    }

    /**
     * 深度检测：计算继承深度是否超过限制
     */
    private void checkInheritanceDepth(Long childRoleId, Long parentRoleId) {
        int depth = calculateMaxDepth(parentRoleId, new HashSet<>(), 0);
        // 加上即将新增的这一层
        if (depth + 1 > MAX_INHERITANCE_DEPTH) {
            throw new IllegalArgumentException("继承深度超过最大限制(" + MAX_INHERITANCE_DEPTH + "级)，无法设置该继承关系");
        }
    }

    /**
     * 计算从指定角色向上的最大继承深度
     */
    private int calculateMaxDepth(Long roleId, Set<Long> visited, int currentDepth) {
        if (visited.contains(roleId) || currentDepth > MAX_INHERITANCE_DEPTH) {
            return currentDepth;
        }
        visited.add(roleId);

        List<RoleInheritance> parentRelations = list(
            new LambdaQueryWrapper<RoleInheritance>()
                .eq(RoleInheritance::getChildRoleId, roleId)
        );
        if (parentRelations.isEmpty()) {
            return currentDepth;
        }

        int maxDepth = currentDepth;
        for (RoleInheritance ri : parentRelations) {
            int depth = calculateMaxDepth(ri.getParentRoleId(), new HashSet<>(visited), currentDepth + 1);
            maxDepth = Math.max(maxDepth, depth);
        }
        return maxDepth;
    }

    /**
     * 递归收集父角色的权限ID
     */
    private void collectParentPermissions(Long roleId, Set<Long> permissionIds, Set<Long> visitedRoleIds, int depth) {
        if (depth > MAX_INHERITANCE_DEPTH) {
            return;
        }

        // 查找直接父角色
        List<RoleInheritance> parentRelations = list(
            new LambdaQueryWrapper<RoleInheritance>()
                .eq(RoleInheritance::getChildRoleId, roleId)
        );

        for (RoleInheritance ri : parentRelations) {
            Long parentId = ri.getParentRoleId();
            if (visitedRoleIds.contains(parentId)) {
                continue;
            }
            visitedRoleIds.add(parentId);

            // 收集父角色的权限
            List<Long> parentPermIds = getPermissionIdsByRoleId(parentId);
            permissionIds.addAll(parentPermIds);

            // 递归向上收集
            collectParentPermissions(parentId, permissionIds, visitedRoleIds, depth + 1);
        }
    }

    /**
     * 查询角色关联的权限ID列表
     */
    private List<Long> getPermissionIdsByRoleId(Long roleId) {
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
            new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId)
        );
        return rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());
    }

    /**
     * 根据权限ID列表查询权限详情
     */
    private List<Permission> queryPermissionsByIds(Set<Long> permissionIds) {
        if (permissionIds.isEmpty()) {
            return List.of();
        }
        return permissionMapper.selectBatchIds(permissionIds);
    }

    /**
     * 递归构建继承树
     */
    private void buildInheritanceTree(Long roleId, List<Map<String, Object>> tree, Set<Long> visited, int depth) {
        if (depth > MAX_INHERITANCE_DEPTH || visited.contains(roleId)) {
            return;
        }
        visited.add(roleId);

        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            return;
        }

        List<RoleInheritance> parentRelations = list(
            new LambdaQueryWrapper<RoleInheritance>()
                .eq(RoleInheritance::getChildRoleId, roleId)
        );

        List<Map<String, Object>> parents = new ArrayList<>();
        for (RoleInheritance ri : parentRelations) {
            Map<String, Object> parentNode = new LinkedHashMap<>();
            Role parentRole = roleMapper.selectById(ri.getParentRoleId());
            if (parentRole != null) {
                parentNode.put("roleId", parentRole.getId());
                parentNode.put("roleName", parentRole.getRoleName());
                parentNode.put("roleCode", parentRole.getRoleCode());

                // 递归构建父角色的父节点
                List<Map<String, Object>> grandParents = new ArrayList<>();
                buildInheritanceTreeToList(ri.getParentRoleId(), grandParents, new HashSet<>(visited), depth + 1);
                if (!grandParents.isEmpty()) {
                    parentNode.put("parents", grandParents);
                }
                parents.add(parentNode);
            }
        }

        // 如果是第一层，包装根节点
        if (depth == 0) {
            Map<String, Object> rootNode = new LinkedHashMap<>();
            rootNode.put("roleId", role.getId());
            rootNode.put("roleName", role.getRoleName());
            rootNode.put("roleCode", role.getRoleCode());
            if (!parents.isEmpty()) {
                rootNode.put("parents", parents);
            }
            tree.add(rootNode);
        } else {
            // 非第一层直接添加到tree中
            for (Map<String, Object> p : parents) {
                tree.add(p);
            }
        }
    }

    /**
     * 递归构建继承树（返回列表形式）
     */
    private void buildInheritanceTreeToList(Long roleId, List<Map<String, Object>> tree, Set<Long> visited, int depth) {
        if (depth > MAX_INHERITANCE_DEPTH || visited.contains(roleId)) {
            return;
        }
        visited.add(roleId);

        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            return;
        }

        List<RoleInheritance> parentRelations = list(
            new LambdaQueryWrapper<RoleInheritance>()
                .eq(RoleInheritance::getChildRoleId, roleId)
        );

        List<Map<String, Object>> parents = new ArrayList<>();
        for (RoleInheritance ri : parentRelations) {
            Map<String, Object> parentNode = new LinkedHashMap<>();
            Role parentRole = roleMapper.selectById(ri.getParentRoleId());
            if (parentRole != null) {
                parentNode.put("roleId", parentRole.getId());
                parentNode.put("roleName", parentRole.getRoleName());
                parentNode.put("roleCode", parentRole.getRoleCode());

                List<Map<String, Object>> grandParents = new ArrayList<>();
                buildInheritanceTreeToList(ri.getParentRoleId(), grandParents, new HashSet<>(visited), depth + 1);
                if (!grandParents.isEmpty()) {
                    parentNode.put("parents", grandParents);
                }
                parents.add(parentNode);
            }
        }

        Map<String, Object> node = new LinkedHashMap<>();
        node.put("roleId", role.getId());
        node.put("roleName", role.getRoleName());
        node.put("roleCode", role.getRoleCode());
        if (!parents.isEmpty()) {
            node.put("parents", parents);
        }
        tree.add(node);
    }
}
