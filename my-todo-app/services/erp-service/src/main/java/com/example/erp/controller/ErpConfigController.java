package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.erp.entity.ErpConfig;
import com.example.erp.service.ErpConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@Tag(name = "系统配置", description = "ERP系统配置管理API")
@RestController
@RequestMapping("/api/erp/configs")
@RequiredArgsConstructor
public class ErpConfigController {

    private final ErpConfigService erpConfigService;

    @Operation(summary = "分页查询配置")
    @GetMapping("/get-config-page")
    public ApiResponse<PageResult<ErpConfig>> getConfigPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String configType,
            @RequestParam(required = false) String configName) {
        Page<ErpConfig> result = erpConfigService.getConfigPage(tenantId, page, size, configType, configName);
        PageResult<ErpConfig> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @Operation(summary = "根据类型获取配置列表")
    @GetMapping("/get-configs-by-type/{configType}")
    public ApiResponse<List<ErpConfig>> getConfigsByType(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @PathVariable String configType) {
        List<ErpConfig> configs = erpConfigService.getConfigsByType(tenantId, configType);
        return ApiResponse.success(configs);
    }

    @Operation(summary = "获取配置值")
    @GetMapping("/get-config-value")
    public ApiResponse<String> getConfigValue(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam String configKey) {
        String value = erpConfigService.getConfigValue(tenantId, configKey);
        return ApiResponse.success(value);
    }

    @Operation(summary = "创建配置")
    @PostMapping("/create-config")
    public ApiResponse<ErpConfig> createConfig(
            @RequestBody ErpConfig config,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        config.setTenantId(tenantId);
        config.setCreatedBy(userId);
        ErpConfig created = erpConfigService.createConfig(config);
        return ApiResponse.success(created);
    }

    @Operation(summary = "更新配置")
    @PutMapping("/update-config/{id}")
    public ApiResponse<ErpConfig> updateConfig(
            @PathVariable Long id,
            @RequestBody ErpConfig config,
            @RequestHeader("X-User-Id") Long userId) {
        config.setId(id);
        config.setUpdatedBy(userId);
        ErpConfig updated = erpConfigService.updateConfig(config);
        return ApiResponse.success(updated);
    }

    @Operation(summary = "删除配置")
    @DeleteMapping("/delete-config/{id}")
    public ApiResponse<Void> deleteConfig(@PathVariable Long id) {
        erpConfigService.deleteConfig(id);
        return ApiResponse.success();
    }

    @Operation(summary = "批量更新配置")
    @PostMapping("/batch-update")
    public ApiResponse<Void> batchUpdateConfigs(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String configType,
            @RequestBody Map<String, String> configs) {
        erpConfigService.batchUpdateConfigs(tenantId, configs, configType, userId);
        return ApiResponse.success();
    }
}
