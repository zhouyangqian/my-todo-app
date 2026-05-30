package com.example.dict.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.dict.entity.TraceAlert;
import com.example.dict.entity.TraceConfig;
import com.example.dict.service.TraceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 分布式追踪控制器
 */
@Tag(name = "分布式追踪", description = "追踪配置与告警规则管理")
@RestController
@RequestMapping("/api/trace")
@RequiredArgsConstructor
public class TraceController {

    private final TraceService traceService;

    // ==================== 追踪配置 ====================

    @Operation(summary = "分页查询追踪配置")
    @GetMapping("/configs/page")
    public ApiResponse<PageResult<TraceConfig>> getTraceConfigPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String serviceName) {
        Page<TraceConfig> result = traceService.getTraceConfigPage(tenantId, page, size, serviceName);
        return ApiResponse.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "获取追踪配置详情")
    @GetMapping("/configs/{id}")
    public ApiResponse<TraceConfig> getTraceConfig(@PathVariable Long id) {
        return ApiResponse.success(traceService.getById(id));
    }

    @Operation(summary = "创建追踪配置")
    @PostMapping("/configs/create")
    public ApiResponse<TraceConfig> createTraceConfig(@RequestBody TraceConfig traceConfig) {
        return ApiResponse.success(traceService.createTraceConfig(traceConfig));
    }

    @Operation(summary = "更新追踪配置")
    @PutMapping("/configs/update/{id}")
    public ApiResponse<TraceConfig> updateTraceConfig(
            @PathVariable Long id,
            @RequestBody TraceConfig traceConfig) {
        traceConfig.setId(id);
        return ApiResponse.success(traceService.updateTraceConfig(traceConfig));
    }

    @Operation(summary = "删除追踪配置")
    @DeleteMapping("/configs/delete/{id}")
    public ApiResponse<Void> deleteTraceConfig(@PathVariable Long id) {
        traceService.deleteTraceConfig(id);
        return ApiResponse.success();
    }

    @Operation(summary = "启用/禁用追踪配置")
    @PutMapping("/configs/toggle/{id}")
    public ApiResponse<TraceConfig> toggleTraceConfig(
            @PathVariable Long id,
            @RequestParam Integer enabled) {
        return ApiResponse.success(traceService.toggleTraceConfig(id, enabled));
    }

    // ==================== 追踪告警 ====================

    @Operation(summary = "分页查询告警规则")
    @GetMapping("/alerts/page")
    public ApiResponse<PageResult<TraceAlert>> getTraceAlertPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String serviceName) {
        Page<TraceAlert> result = traceService.getTraceAlertPage(tenantId, page, size, serviceName);
        return ApiResponse.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "获取告警规则详情")
    @GetMapping("/alerts/{id}")
    public ApiResponse<TraceAlert> getTraceAlert(@PathVariable Long id) {
        return ApiResponse.success(traceService.getTraceAlert(id));
    }

    @Operation(summary = "创建告警规则")
    @PostMapping("/alerts/create")
    public ApiResponse<TraceAlert> createTraceAlert(@RequestBody TraceAlert traceAlert) {
        return ApiResponse.success(traceService.createTraceAlert(traceAlert));
    }

    @Operation(summary = "更新告警规则")
    @PutMapping("/alerts/update/{id}")
    public ApiResponse<TraceAlert> updateTraceAlert(
            @PathVariable Long id,
            @RequestBody TraceAlert traceAlert) {
        traceAlert.setId(id);
        return ApiResponse.success(traceService.updateTraceAlert(traceAlert));
    }

    @Operation(summary = "删除告警规则")
    @DeleteMapping("/alerts/delete/{id}")
    public ApiResponse<Void> deleteTraceAlert(@PathVariable Long id) {
        traceService.deleteTraceAlert(id);
        return ApiResponse.success();
    }

    @Operation(summary = "启用/禁用告警规则")
    @PutMapping("/alerts/toggle/{id}")
    public ApiResponse<TraceAlert> toggleTraceAlert(
            @PathVariable Long id,
            @RequestParam Integer enabled) {
        return ApiResponse.success(traceService.toggleTraceAlert(id, enabled));
    }

    @Operation(summary = "确认告警")
    @PutMapping("/alerts/acknowledge/{id}")
    public ApiResponse<TraceAlert> acknowledgeAlert(@PathVariable Long id) {
        return ApiResponse.success(traceService.acknowledgeAlert(id));
    }

    @Operation(summary = "获取告警统计")
    @GetMapping("/alerts/stats")
    public ApiResponse<Map<String, Object>> getAlertStats(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ApiResponse.success(traceService.getAlertStats(tenantId));
    }
}
