package com.example.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.exception.BusinessException;
import com.example.permission.api.dto.PermissionTreeDTO;
import com.example.permission.api.vo.PermissionCreateVO;
import com.example.permission.api.vo.PermissionUpdateVO;
import com.example.permission.entity.Permission;
import com.example.permission.entity.RolePermission;
import com.example.permission.mapper.PermissionMapper;
import com.example.permission.mapper.RolePermissionMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 动态权限服务实现类
 * <p>
 * 提供权限的动态创建、更新、删除和权限树查询功能。
 * 支持Redis缓存权限树数据，减少数据库访问压力。
 * </p>
 * <p>
 * 缓存策略：
 * - 权限树缓存key格式：permission:tree:{tenantId}，TTL 30分钟
 * - 权限编码缓存key格式：permission:code:{tenantId}，TTL 30分钟
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicPermissionServiceImpl implements DynamicPermissionService {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /** 权限树缓存前缀 */
    private static final String PERMISSION_CACHE_PREFIX = "permission:tree:";
    /** 权限编码缓存前缀 */
    private static final String PERMISSION_CODE_PREFIX = "permission:code:";
    /** 缓存过期时间（分钟） */
    private static final long CACHE_TTL_MINUTES = 30;

    /**
     * 创建权限
     * <p>
     * 校验权限编码唯一性，根据父节点自动计算层级，保存后清除缓存
     * </p>
     *
     * @param vo       创建权限请求参数
     * @param tenantId 租户ID
     * @return 创建成功的权限实体
     */
    @Override
    @Transactional
    public Permission createPermission(PermissionCreateVO vo, Long tenantId) {
        // 检查code唯一性
        Long count = permissionMapper.selectCount(
                new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getPermissionCode, vo.getCode())
                        .eq(Permission::getTenantId, tenantId)
                        .eq(Permission::getDeleted, 0)
        );
        if (count > 0) {
            throw new BusinessException("权限编码已存在: " + vo.getCode());
        }

        Permission permission = new Permission();
        permission.setPermissionCode(vo.getCode());
        permission.setPermissionName(vo.getName());
        permission.setTenantId(tenantId);
        permission.setStatus(1);
        permission.setSort(0);
        permission.setPermissionType(vo.getPermissionType() != null ? vo.getPermissionType() : 2);
        permission.setVisible(1);

        // 将描述存入 resourcePath 字段（数据库无独立 description 列）
        if (vo.getDescription() != null && !vo.getDescription().isEmpty()) {
            permission.setResourcePath(vo.getDescription());
        }

        // 设置层级：如果指定了父节点，则层级=父节点层级+1；否则为0
        if (vo.getParentId() != null && vo.getParentId() > 0) {
            Permission parent = permissionMapper.selectById(vo.getParentId());
            if (parent == null) {
                throw new BusinessException("父权限不存在");
            }
            permission.setParentId(vo.getParentId());
            permission.setSort(vo.getLevel() != null ? vo.getLevel() : parent.getSort() + 1);
        } else {
            permission.setParentId(0L);
            permission.setSort(vo.getLevel() != null ? vo.getLevel() : 0);
        }

        permissionMapper.insert(permission);
        log.info("创建权限成功，编码: {}, 名称: {}", vo.getCode(), vo.getName());

        // 清除缓存
        invalidateCache(tenantId);
        return permission;
    }

    /**
     * 更新权限
     * <p>
     * 仅更新权限名称和描述，更新后清除缓存
     * </p>
     *
     * @param permissionId 权限ID
     * @param vo           更新权限请求参数
     * @param tenantId     租户ID
     * @return 更新后的权限实体
     */
    @Override
    @Transactional
    public Permission updatePermission(Long permissionId, PermissionUpdateVO vo, Long tenantId) {
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }

        if (vo.getName() != null) {
            permission.setPermissionName(vo.getName());
        }
        // 将描述信息存入resourcePath字段（复用已有字段）
        if (vo.getDescription() != null) {
            permission.setResourcePath(vo.getDescription());
        }
        permissionMapper.updateById(permission);
        log.info("更新权限成功，ID: {}", permissionId);

        // 清除缓存
        invalidateCache(tenantId);
        return permission;
    }

    /**
     * 删除权限
     * <p>
     * 删除前检查是否有子权限和角色关联，存在则拒绝删除。
     * 使用MyBatis-Plus逻辑删除，删除后清除缓存。
     * </p>
     *
     * @param permissionId 权限ID
     * @param tenantId     租户ID
     */
    @Override
    @Transactional
    public void deletePermission(Long permissionId, Long tenantId) {
        // 检查是否有子权限
        Long childCount = permissionMapper.selectCount(
                new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getParentId, permissionId)
                        .eq(Permission::getDeleted, 0)
        );
        if (childCount > 0) {
            throw new BusinessException("该权限下存在子权限，不能删除");
        }

        // 检查是否有角色关联
        Long roleCount = rolePermissionMapper.selectCount(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getPermissionId, permissionId)
        );
        if (roleCount > 0) {
            throw new BusinessException("该权限已分配给角色，不能删除");
        }

        // 逻辑删除
        permissionMapper.deleteById(permissionId);
        log.info("删除权限成功，ID: {}", permissionId);

        // 清除缓存
        invalidateCache(tenantId);
    }

    /**
     * 获取权限树
     * <p>
     * 先从Redis缓存获取，缓存未命中则查询数据库并构建树结构。
     * 树结构以parentId=0的节点为根节点，递归构建子节点。
     * </p>
     *
     * @param tenantId 租户ID
     * @return 权限树DTO列表
     */
    @Override
    public List<PermissionTreeDTO> getPermissionTree(Long tenantId) {
        String cacheKey = PERMISSION_CACHE_PREFIX + tenantId;

        // 先查Redis缓存
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.readValue(cached,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, PermissionTreeDTO.class));
            }
        } catch (JsonProcessingException e) {
            log.warn("解析权限树缓存失败，将从数据库查询", e);
        }

        // 缓存未命中，查数据库
        List<Permission> allPermissions = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getTenantId, tenantId)
                        .eq(Permission::getDeleted, 0)
                        .orderByAsc(Permission::getSort)
        );

        // 转换为DTO
        List<PermissionTreeDTO> allDTOs = allPermissions.stream().map(p -> {
            PermissionTreeDTO dto = new PermissionTreeDTO();
            dto.setId(p.getId());
            dto.setCode(p.getPermissionCode());
            dto.setName(p.getPermissionName());
            dto.setParentId(p.getParentId());
            dto.setLevel(p.getSort());
            dto.setDescription(p.getResourcePath());
            dto.setChildren(new ArrayList<>());
            return dto;
        }).collect(Collectors.toList());

        // 构建树结构（parentId=0的是根节点）
        List<PermissionTreeDTO> tree = buildTree(allDTOs, 0L);

        // 缓存结果（30分钟TTL）
        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(tree),
                    CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.warn("缓存权限树失败", e);
        }

        return tree;
    }

    /**
     * 递归构建权限树
     *
     * @param allNodes 所有节点列表
     * @param parentId 父节点ID
     * @return 当前层级的子节点列表
     */
    private List<PermissionTreeDTO> buildTree(List<PermissionTreeDTO> allNodes, Long parentId) {
        List<PermissionTreeDTO> tree = new ArrayList<>();
        for (PermissionTreeDTO node : allNodes) {
            if (Objects.equals(node.getParentId(), parentId)) {
                node.setChildren(buildTree(allNodes, node.getId()));
                tree.add(node);
            }
        }
        return tree;
    }

    /**
     * 清除权限缓存
     *
     * @param tenantId 租户ID
     */
    @Override
    public void invalidateCache(Long tenantId) {
        redisTemplate.delete(PERMISSION_CACHE_PREFIX + tenantId);
        redisTemplate.delete(PERMISSION_CODE_PREFIX + tenantId);
        log.info("已清除租户 {} 的权限缓存", tenantId);
    }
}
