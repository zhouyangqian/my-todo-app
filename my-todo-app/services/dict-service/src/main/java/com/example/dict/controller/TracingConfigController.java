package com.example.dict.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.dict.entity.TraceAlert;
import com.example.dict.entity.TraceConfig;
import com.example.dict.service.TracingConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 分布式追踪配置控制器
 */
@Tag(name = "分布式追踪", description = "追踪配置与告警管理")
@RestController
@RequestMapping("/api/tracing")
@RequiredArgsConstructor
public class TracingConfigController {

    private final TracingConfigService tracingConfigService;

    // ==================== 追踪配置 ====================

    @RequiresPermission(code = "dict:trace:config:list", name = "查询追踪配置列表")
    @Operation(summary = "分页查询追踪配置")
    @GetMapping("/configs/page")
    public ApiResponse<PageResult<TraceConfig>> getTraceConfigPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TraceConfig> result = tracingConfigService.getTraceConfigPage(tenantId, page, size);
        return ApiResponse.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @RequiresPermission(code = "dict:trace:config:create", name = "创建追踪配置")
    @Operation(summary = "创建追踪配置")
    @PostMapping("/configs/create")
    public ApiResponse<TraceConfig> createTraceConfig(@RequestBody TraceConfig config) {
        return ApiResponse.success(tracingConfigService.createTraceConfig(config));
    }

    @RequiresPermission(code = "dict:trace:config:update", name = "更新追踪配置")
    @Operation(summary = "更新追踪配置")
    @PutMapping("/configs/update/{id}")
    public ApiResponse<TraceConfig> updateTraceConfig(
            @PathVariable Long id,
            @RequestBody TraceConfig config) {
        config.setId(id);
        return ApiResponse.success(tracingConfigService.updateTraceConfig(config));
    }

    @RequiresPermission(code = "dict:trace:config:delete", name = "删除追踪配置")
    @Operation(summary = "删除追踪配置")
    @DeleteMapping("/configs/delete/{id}")
    public ApiResponse<Void> deleteTraceConfig(@PathVariable Long id) {
        tracingConfigService.deleteTraceConfig(id);
        return ApiResponse.success();
    }

    // ==================== 追踪告警 ====================

    @RequiresPermission(code = "dict:trace:alert:list", name = "查询告警列表")
    @Operation(summary = "分页查询追踪告警")
    @GetMapping("/alerts/page")
    public ApiResponse<PageResult<TraceAlert>> getTraceAlertPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TraceAlert> result = tracingConfigService.getTraceAlertPage(tenantId, page, size);
        return ApiResponse.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @RequiresPermission(code = "dict:trace:alert:create", name = "创建追踪告警")
    @Operation(summary = "创建追踪告警")
    @PostMapping("/alerts/create")
    public ApiResponse<TraceAlert> createTraceAlert(@RequestBody TraceAlert alert) {
        return ApiResponse.success(tracingConfigService.createTraceAlert(alert));
    }

    @RequiresPermission(code = "dict:trace:alert:update", name = "更新追踪告警")
    @Operation(summary = "更新追踪告警")
    @PutMapping("/alerts/update/{id}")
    public ApiResponse<TraceAlert> updateTraceAlert(
            @PathVariable Long id,
            @RequestBody TraceAlert alert) {
        alert.setId(id);
        return ApiResponse.success(tracingConfigService.updateTraceAlert(alert));
    }

    @RequiresPermission(code = "dict:trace:alert:delete", name = "删除追踪告警")
    @Operation(summary = "删除追踪告警")
    @DeleteMapping("/alerts/delete/{id}")
    public ApiResponse<Void> deleteTraceAlert(@PathVariable Long id) {
        tracingConfigService.deleteTraceAlert(id);
        return ApiResponse.success();
    }
}
