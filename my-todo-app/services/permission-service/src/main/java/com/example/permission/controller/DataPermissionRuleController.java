package com.example.permission.controller;

import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.permission.entity.DataPermissionRule;
import com.example.permission.service.DataPermissionRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据权限规则管理控制器
 * <p>
 * 提供数据权限规则的RESTful API接口，包括：
 * - 创建、更新、删除数据权限规则
 * - 按角色ID查询数据权限规则列表
 * </p>
 */
@Tag(name = "DataPermissionRule", description = "数据权限规则管理 API")
@RestController
@RequestMapping("/api/permissions/data-rules")
@RequiredArgsConstructor
public class DataPermissionRuleController {

    /** 数据权限规则服务 */
    private final DataPermissionRuleService dataPermissionRuleService;

    /**
     * 创建数据权限规则
     *
     * @param rule   规则实体对象（请求体）
     * @param userId 当前操作用户ID（请求头）
     * @return 创建成功的规则对象
     */
    @Operation(summary = "创建数据权限规则")
    @RequiresPermission(code = "system:data-rule:create", name = "创建数据权限规则")
    @PostMapping("/create")
    public ApiResponse<DataPermissionRule> createRule(
            @RequestBody DataPermissionRule rule,
            @RequestHeader("X-User-Id") Long userId) {
        rule.setCreatedBy(userId);
        DataPermissionRule created = dataPermissionRuleService.createRule(rule);
        return ApiResponse.success(created);
    }

    /**
     * 更新数据权限规则
     *
     * @param id     规则ID（路径参数）
     * @param rule   规则实体对象（请求体）
     * @param userId 当前操作用户ID（请求头）
     * @return 更新后的规则对象
     */
    @Operation(summary = "更新数据权限规则")
    @RequiresPermission(code = "system:data-rule:update", name = "更新数据权限规则")
    @PutMapping("/update/{id}")
    public ApiResponse<DataPermissionRule> updateRule(
            @PathVariable Long id,
            @RequestBody DataPermissionRule rule,
            @RequestHeader("X-User-Id") Long userId) {
        rule.setId(id);
        rule.setUpdatedBy(userId);
        DataPermissionRule updated = dataPermissionRuleService.updateRule(rule);
        return ApiResponse.success(updated);
    }

    /**
     * 删除数据权限规则
     *
     * @param id 规则ID（路径参数）
     * @return 空响应
     */
    @Operation(summary = "删除数据权限规则")
    @RequiresPermission(code = "system:data-rule:delete", name = "删除数据权限规则")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deleteRule(@PathVariable Long id) {
        dataPermissionRuleService.deleteRule(id);
        return ApiResponse.success();
    }

    /**
     * 获取指定角色的数据权限规则列表
     *
     * @param roleId 角色ID（路径参数）
     * @return 该角色的数据权限规则列表
     */
    @Operation(summary = "获取角色的数据权限规则")
    @RequiresPermission(code = "system:data-rule:list", name = "查询数据权限规则")
    @GetMapping("/role/{roleId}")
    public ApiResponse<List<DataPermissionRule>> getRulesByRole(@PathVariable Long roleId) {
        List<DataPermissionRule> rules = dataPermissionRuleService.getRulesByRoleId(roleId);
        return ApiResponse.success(rules);
    }
}
