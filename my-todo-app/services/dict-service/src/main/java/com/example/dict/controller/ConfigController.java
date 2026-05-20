package com.example.dict.controller;

import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.dict.entity.SystemConfig;
import com.example.dict.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置控制器
 */
@Tag(name = "系统配置", description = "系统参数配置管理API")
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @RequiresPermission(code = "dict:config:detail", name = "查询配置详情")
    @Operation(summary = "获取配置值")
    @GetMapping("/value/{configCode}")
    public ApiResponse<String> getConfigValue(
            @PathVariable String configCode,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        String value = configService.getConfigValue(configCode, tenantId);
        return ApiResponse.success(value);
    }

    @RequiresPermission(code = "dict:config:detail", name = "查询配置详情")
    @Operation(summary = "获取配置详情")
    @GetMapping("/{configCode}")
    public ApiResponse<SystemConfig> getConfig(
            @PathVariable String configCode,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        SystemConfig config = configService.getConfig(configCode, tenantId);
        return ApiResponse.success(config);
    }

    @RequiresPermission(code = "dict:config:list", name = "查询配置列表")
    @Operation(summary = "获取所有配置")
    @GetMapping("/list")
    public ApiResponse<List<SystemConfig>> getAllConfigs(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        List<SystemConfig> configs = configService.getAllConfigs(tenantId);
        return ApiResponse.success(configs);
    }

    @RequiresPermission(code = "dict:config:update", name = "设置配置值")
    @Operation(summary = "设置配置值")
    @PutMapping("/value/{configCode}")
    public ApiResponse<Void> setConfigValue(
            @PathVariable String configCode,
            @RequestBody String configValue,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        configService.setConfigValue(configCode, configValue, tenantId, userId);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "dict:config:create", name = "创建配置")
    @Operation(summary = "创建配置")
    @PostMapping
    public ApiResponse<SystemConfig> createConfig(
            @RequestBody SystemConfig config,
            @RequestHeader("X-User-Id") Long userId) {
        config.setCreatedBy(userId);
        SystemConfig created = configService.createConfig(config);
        return ApiResponse.success(created);
    }

    @RequiresPermission(code = "dict:config:update", name = "更新配置")
    @Operation(summary = "更新配置")
    @PutMapping("/{id}")
    public ApiResponse<SystemConfig> updateConfig(
            @PathVariable Long id,
            @RequestBody SystemConfig config,
            @RequestHeader("X-User-Id") Long userId) {
        config.setId(id);
        config.setUpdatedBy(userId);
        configService.updateById(config);
        configService.clearConfigCache(config.getConfigCode(), config.getTenantId());
        return ApiResponse.success(config);
    }

    @RequiresPermission(code = "dict:config:delete", name = "删除配置")
    @Operation(summary = "删除配置")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteConfig(@PathVariable Long id) {
        SystemConfig config = configService.getById(id);
        if (config != null) {
            if (config.getIsSystem() == 1) {
                return ApiResponse.error(400, "系统内置配置不能删除");
            }
            configService.removeById(id);
            configService.clearConfigCache(config.getConfigCode(), config.getTenantId());
        }
        return ApiResponse.success();
    }

    @Operation(summary = "清除配置缓存")
    @DeleteMapping("/cache/{configCode}")
    public ApiResponse<Void> clearConfigCache(
            @PathVariable String configCode,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        configService.clearConfigCache(configCode, tenantId);
        return ApiResponse.success();
    }
}
