package com.example.permission.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.permission.api.vo.ApplyTemplateVO;
import com.example.permission.api.vo.PermissionTemplateCreateVO;
import com.example.permission.entity.PermissionTemplate;
import com.example.permission.service.PermissionTemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限模板管理控制器
 * <p>
 * 提供权限模板的创建、更新、删除、分页查询、详情查询、应用到角色等接口。
 * 系统预设模板不可修改和删除。
 * </p>
 */
@Tag(name = "权限模板管理")
@RestController
@RequestMapping("/api/permissions/templates")
@RequiredArgsConstructor
public class PermissionTemplateController {

    private final PermissionTemplateService templateService;
    private final ObjectMapper objectMapper;

    /**
     * 创建权限模板
     *
     * @param vo       模板参数
     * @param tenantId 租户ID
     * @param userId   当前操作用户ID
     * @return 创建的模板
     */
    @Operation(summary = "创建权限模板")
    @RequiresPermission(code = "system:template:create", name = "创建权限模板")
    @PostMapping("/create")
    public ApiResponse<PermissionTemplate> createTemplate(
            @RequestBody PermissionTemplateCreateVO vo,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        PermissionTemplate template = new PermissionTemplate();
        template.setTenantId(tenantId);
        template.setTemplateName(vo.getTemplateName());
        template.setTemplateCode(vo.getTemplateCode());
        template.setDescription(vo.getDescription());
        template.setIsSystem(0);
        template.setPermissionIds(toJson(vo.getPermissionIds()));
        template.setStatus(vo.getStatus() != null ? vo.getStatus() : 1);
        template.setCreatedBy(userId);
        template.setUpdatedBy(userId);

        PermissionTemplate created = templateService.createTemplate(template);
        return ApiResponse.success(created);
    }

    /**
     * 更新权限模板
     *
     * @param id       模板ID
     * @param vo       模板参数
     * @param userId   当前操作用户ID
     * @return 更新后的模板
     */
    @Operation(summary = "更新权限模板")
    @RequiresPermission(code = "system:template:update", name = "更新权限模板")
    @PutMapping("/update/{id}")
    public ApiResponse<PermissionTemplate> updateTemplate(
            @PathVariable Long id,
            @RequestBody PermissionTemplateCreateVO vo,
            @RequestHeader("X-User-Id") Long userId) {
        PermissionTemplate template = new PermissionTemplate();
        template.setId(id);
        template.setTemplateName(vo.getTemplateName());
        template.setTemplateCode(vo.getTemplateCode());
        template.setDescription(vo.getDescription());
        template.setPermissionIds(toJson(vo.getPermissionIds()));
        template.setStatus(vo.getStatus());
        template.setUpdatedBy(userId);

        PermissionTemplate updated = templateService.updateTemplate(template);
        return ApiResponse.success(updated);
    }

    /**
     * 删除权限模板
     *
     * @param id 模板ID
     * @return 空响应
     */
    @Operation(summary = "删除权限模板")
    @RequiresPermission(code = "system:template:delete", name = "删除权限模板")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ApiResponse.success();
    }

    /**
     * 分页查询权限模板
     *
     * @param tenantId 租户ID
     * @param page     当前页码
     * @param size     每页条数
     * @return 分页数据
     */
    @Operation(summary = "分页查询权限模板")
    @RequiresPermission(code = "system:template:list", name = "查询权限模板")
    @GetMapping("/page")
    public ApiResponse<PageResult<PermissionTemplate>> getTemplatePage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PermissionTemplate> result = templateService.getTemplatePage(tenantId, page, size);
        PageResult<PermissionTemplate> pageResult = PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    /**
     * 获取模板详情
     *
     * @param id 模板ID
     * @return 模板详情
     */
    @Operation(summary = "获取模板详情")
    @RequiresPermission(code = "system:template:detail", name = "查询模板详情")
    @GetMapping("/detail/{id}")
    public ApiResponse<PermissionTemplate> getTemplateDetail(@PathVariable Long id) {
        PermissionTemplate template = templateService.getTemplateDetail(id);
        return ApiResponse.success(template);
    }

    /**
     * 将模板应用到角色
     *
     * @param vo       应用参数（templateId, roleId）
     * @param tenantId 租户ID
     * @param userId   当前操作用户ID
     * @return 空响应
     */
    @Operation(summary = "将模板应用到角色")
    @RequiresPermission(code = "system:template:apply", name = "应用模板到角色")
    @PostMapping("/apply-to-role")
    public ApiResponse<Void> applyTemplateToRole(
            @RequestBody ApplyTemplateVO vo,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        templateService.applyTemplateToRole(vo.getTemplateId(), vo.getRoleId(), tenantId, userId);
        return ApiResponse.success();
    }

    /**
     * 将权限ID列表转为JSON字符串
     */
    private String toJson(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(permissionIds);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
