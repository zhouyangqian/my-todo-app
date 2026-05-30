package com.example.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.permission.entity.PermissionTemplate;
import com.example.permission.mapper.PermissionTemplateMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 权限模板管理服务实现类
 * <p>
 * 提供权限模板的创建、更新、删除、查询、应用到角色等功能。
 * 系统预设模板不可修改和删除。
 * permissionIds 字段使用 JSON 数组格式存储，如 [1,2,3]。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionTemplateServiceImpl extends ServiceImpl<PermissionTemplateMapper, PermissionTemplate> implements PermissionTemplateService {

    private final RoleService roleService;
    private final ObjectMapper objectMapper;

    /**
     * 创建权限模板
     * <p>
     * 校验模板编码在租户内的唯一性，将 permissionIds 转为 JSON 字符串存储。
     * </p>
     *
     * @param template 模板实体
     * @return 创建的模板
     */
    @Override
    @Transactional
    public PermissionTemplate createTemplate(PermissionTemplate template) {
        // 校验编码唯一性
        PermissionTemplate existing = getOne(
                new LambdaQueryWrapper<PermissionTemplate>()
                        .eq(PermissionTemplate::getTenantId, template.getTenantId())
                        .eq(PermissionTemplate::getTemplateCode, template.getTemplateCode())
        );
        if (existing != null) {
            throw new IllegalArgumentException("模板编码已存在: " + template.getTemplateCode());
        }

        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        save(template);

        log.info("创建权限模板: {}, 编码: {}", template.getTemplateName(), template.getTemplateCode());
        return template;
    }

    /**
     * 更新权限模板
     * <p>
     * 系统预设模板不可修改。
     * </p>
     *
     * @param template 模板实体（含ID）
     * @return 更新后的模板
     */
    @Override
    @Transactional
    public PermissionTemplate updateTemplate(PermissionTemplate template) {
        PermissionTemplate existing = getById(template.getId());
        if (existing == null) {
            throw new IllegalArgumentException("模板不存在");
        }
        if (existing.getIsSystem() == 1) {
            throw new IllegalArgumentException("系统预设模板不可修改");
        }

        // 校验编码唯一性（排除自身）
        PermissionTemplate dup = getOne(
                new LambdaQueryWrapper<PermissionTemplate>()
                        .eq(PermissionTemplate::getTenantId, existing.getTenantId())
                        .eq(PermissionTemplate::getTemplateCode, template.getTemplateCode())
                        .ne(PermissionTemplate::getId, template.getId())
        );
        if (dup != null) {
            throw new IllegalArgumentException("模板编码已存在: " + template.getTemplateCode());
        }

        template.setUpdatedAt(LocalDateTime.now());
        updateById(template);

        log.info("更新权限模板: {}", template.getId());
        return template;
    }

    /**
     * 删除权限模板
     * <p>
     * 系统预设模板不可删除。
     * </p>
     *
     * @param id 模板ID
     */
    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        PermissionTemplate existing = getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("模板不存在");
        }
        if (existing.getIsSystem() == 1) {
            throw new IllegalArgumentException("系统预设模板不可删除");
        }

        removeById(id);
        log.info("删除权限模板: {}", id);
    }

    /**
     * 分页查询权限模板
     *
     * @param tenantId 租户ID
     * @param page     当前页码
     * @param size     每页条数
     * @return 分页结果
     */
    @Override
    public Page<PermissionTemplate> getTemplatePage(Long tenantId, int page, int size) {
        LambdaQueryWrapper<PermissionTemplate> wrapper = new LambdaQueryWrapper<>();
        // 查询租户自己的模板 + 系统预设模板
        wrapper.and(w -> w.eq(PermissionTemplate::getTenantId, tenantId)
                        .or()
                        .eq(PermissionTemplate::getTenantId, 0L))
                .orderByDesc(PermissionTemplate::getIsSystem)
                .orderByDesc(PermissionTemplate::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 获取模板详情
     *
     * @param id 模板ID
     * @return 模板详情（含解析后的权限ID列表）
     */
    @Override
    public PermissionTemplate getTemplateDetail(Long id) {
        return getById(id);
    }

    /**
     * 将模板权限应用到角色
     * <p>
     * 解析模板中的 permissionIds JSON 数组，调用 RoleService 将权限分配给角色。
     * </p>
     *
     * @param templateId 模板ID
     * @param roleId     角色ID
     * @param tenantId   租户ID
     * @param operatorId 操作人ID
     */
    @Override
    @Transactional
    public void applyTemplateToRole(Long templateId, Long roleId, Long tenantId, Long operatorId) {
        PermissionTemplate template = getById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在");
        }

        List<Long> permissionIds = parsePermissionIds(template.getPermissionIds());
        roleService.assignPermissionsToRole(roleId, tenantId, permissionIds, operatorId);

        log.info("模板 {} 的权限已应用到角色 {}", templateId, roleId);
    }

    /**
     * 初始化系统预设模板
     * <p>
     * 创建三个系统预设模板：BASIC_USER（基本查看）、DEPT_MANAGER（部门管理）、SYSTEM_ADMIN（全部权限）。
     * 仅在系统模板不存在时创建。
     * </p>
     */
    @Override
    @Transactional
    public void initSystemTemplates() {
        // 检查是否已有系统模板
        long count = count(new LambdaQueryWrapper<PermissionTemplate>()
                .eq(PermissionTemplate::getTenantId, 0L)
                .eq(PermissionTemplate::getIsSystem, 1));
        if (count > 0) {
            log.info("系统预设模板已存在，跳过初始化");
            return;
        }

        // BASIC_USER - 基本查看权限
        createSystemTemplate("BASIC_USER", "基本用户", "基本的查看权限，适用于普通用户", "[]");

        // DEPT_MANAGER - 部门管理权限
        createSystemTemplate("DEPT_MANAGER", "部门经理", "部门管理相关权限，适用于部门经理", "[]");

        // SYSTEM_ADMIN - 全部权限
        createSystemTemplate("SYSTEM_ADMIN", "系统管理员", "拥有系统全部权限", "[]");

        log.info("系统预设模板初始化完成");
    }

    /**
     * 创建单个系统模板
     */
    private void createSystemTemplate(String code, String name, String description, String permissionIds) {
        PermissionTemplate template = new PermissionTemplate();
        template.setTenantId(0L);
        template.setTemplateCode(code);
        template.setTemplateName(name);
        template.setDescription(description);
        template.setIsSystem(1);
        template.setPermissionIds(permissionIds);
        template.setStatus(1);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        save(template);
    }

    /**
     * 解析 permissionIds JSON 字符串为 List
     *
     * @param json JSON字符串，如 "[1,2,3]"
     * @return 权限ID列表
     */
    private List<Long> parsePermissionIds(String json) {
        if (json == null || json.isEmpty() || "[]".equals(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (JsonProcessingException e) {
            log.error("解析权限ID列表失败: {}", json, e);
            return Collections.emptyList();
        }
    }
}
