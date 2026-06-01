package com.example.dict.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.dict.entity.ApiDefinition;
import com.example.dict.entity.ApiSubscription;
import com.example.dict.entity.ApiUsageRecord;
import com.example.dict.service.ApiMarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * API市场控制器
 */
@Tag(name = "API市场", description = "API定义、订阅与使用统计管理")
@RestController
@RequestMapping("/api/api-market")
@RequiredArgsConstructor
public class ApiMarketController {

    private final ApiMarketService apiMarketService;

    // ==================== API定义 ====================

    @RequiresPermission(code = "dict:api-market:list", name = "查询API定义列表")
    @Operation(summary = "分页查询API定义")
    @GetMapping("/definitions/page")
    public ApiResponse<PageResult<ApiDefinition>> getApiDefinitionPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String apiName) {
        Page<ApiDefinition> result = apiMarketService.getApiDefinitionPage(tenantId, page, size, apiName);
        return ApiResponse.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @RequiresPermission(code = "dict:api-market:detail", name = "查询API定义详情")
    @Operation(summary = "获取API定义详情")
    @GetMapping("/definitions/{id}")
    public ApiResponse<ApiDefinition> getApiDefinition(@PathVariable Long id) {
        return ApiResponse.success(apiMarketService.getById(id));
    }

    @RequiresPermission(code = "dict:api-market:create", name = "创建API定义")
    @Operation(summary = "创建API定义")
    @PostMapping("/definitions/create")
    public ApiResponse<ApiDefinition> createApiDefinition(
            @RequestBody ApiDefinition apiDefinition,
            @RequestHeader("X-User-Id") Long userId) {
        apiDefinition.setCreatedBy(userId);
        return ApiResponse.success(apiMarketService.createApiDefinition(apiDefinition));
    }

    @RequiresPermission(code = "dict:api-market:update", name = "更新API定义")
    @Operation(summary = "更新API定义")
    @PutMapping("/definitions/update/{id}")
    public ApiResponse<ApiDefinition> updateApiDefinition(
            @PathVariable Long id,
            @RequestBody ApiDefinition apiDefinition,
            @RequestHeader("X-User-Id") Long userId) {
        apiDefinition.setId(id);
        apiDefinition.setUpdatedBy(userId);
        return ApiResponse.success(apiMarketService.updateApiDefinition(apiDefinition));
    }

    @RequiresPermission(code = "dict:api-market:delete", name = "删除API定义")
    @Operation(summary = "删除API定义")
    @DeleteMapping("/definitions/delete/{id}")
    public ApiResponse<Void> deleteApiDefinition(@PathVariable Long id) {
        apiMarketService.deleteApiDefinition(id);
        return ApiResponse.success();
    }

    // ==================== API订阅 ====================

    @RequiresPermission(code = "dict:api-market:list", name = "查询订阅列表")
    @Operation(summary = "分页查询API订阅")
    @GetMapping("/subscriptions/page")
    public ApiResponse<PageResult<ApiSubscription>> getSubscriptionPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long apiId) {
        Page<ApiSubscription> result = apiMarketService.getSubscriptionPage(tenantId, page, size, apiId);
        return ApiResponse.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @RequiresPermission(code = "dict:api-market:subscribe", name = "订阅API")
    @Operation(summary = "订阅API")
    @PostMapping("/subscriptions/subscribe")
    public ApiResponse<ApiSubscription> subscribe(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam Long apiId,
            @RequestParam String subscriberName,
            @RequestParam(required = false) Integer callLimit) {
        return ApiResponse.success(apiMarketService.subscribe(tenantId, apiId, subscriberName, callLimit, null));
    }

    @RequiresPermission(code = "dict:api-market:unsubscribe", name = "取消订阅")
    @Operation(summary = "取消订阅")
    @DeleteMapping("/subscriptions/{id}")
    public ApiResponse<Void> unsubscribe(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        apiMarketService.unsubscribe(id, tenantId);
        return ApiResponse.success();
    }

    // ==================== 使用记录 ====================

    @RequiresPermission(code = "dict:api-market:list", name = "记录API使用")
    @Operation(summary = "记录API使用")
    @PostMapping("/usage/record")
    public ApiResponse<ApiUsageRecord> recordUsage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam Long apiId,
            @RequestParam Long subscriptionId,
            @RequestParam Integer responseStatus,
            @RequestParam(required = false) Integer responseTimeMs) {
        return ApiResponse.success(apiMarketService.recordUsage(tenantId, apiId, subscriptionId, responseStatus, responseTimeMs));
    }

    @RequiresPermission(code = "dict:api-market:list", name = "查询使用记录")
    @Operation(summary = "分页查询使用记录")
    @GetMapping("/usage/records/page")
    public ApiResponse<PageResult<ApiUsageRecord>> getUsageRecordPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long apiId) {
        Page<ApiUsageRecord> result = apiMarketService.getUsageRecordPage(tenantId, page, size, apiId);
        return ApiResponse.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @RequiresPermission(code = "dict:api-market:list", name = "查询API使用统计")
    @Operation(summary = "获取使用统计")
    @GetMapping("/usage/stats")
    public ApiResponse<Map<String, Object>> getUsageStats(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(required = false) Long apiId) {
        return ApiResponse.success(apiMarketService.getUsageStats(tenantId, apiId));
    }

    // ==================== 前端兼容接口 ====================

    @RequiresPermission(code = "dict:api-market:list", name = "查询API列表")
    @Operation(summary = "API列表（分页）")
    @GetMapping("/list")
    public ApiResponse<PageResult<ApiDefinition>> list(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String apiName,
            @RequestParam(required = false) Integer status) {
        Page<ApiDefinition> result = apiMarketService.getApiDefinitionPage(tenantId, page, size, apiName);
        return ApiResponse.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @RequiresPermission(code = "dict:api-market:detail", name = "查询API详情")
    @Operation(summary = "获取API详情")
    @GetMapping("/detail/{id}")
    public ApiResponse<ApiDefinition> detail(@PathVariable Long id) {
        return ApiResponse.success(apiMarketService.getById(id));
    }

    @RequiresPermission(code = "dict:api-market:create", name = "创建API")
    @Operation(summary = "创建API")
    @PostMapping("/create")
    public ApiResponse<ApiDefinition> create(
            @RequestBody ApiDefinition apiDefinition,
            @RequestHeader("X-User-Id") Long userId) {
        apiDefinition.setCreatedBy(userId);
        return ApiResponse.success(apiMarketService.createApiDefinition(apiDefinition));
    }

    @RequiresPermission(code = "dict:api-market:update", name = "更新API")
    @Operation(summary = "更新API")
    @PutMapping("/update/{id}")
    public ApiResponse<ApiDefinition> update(
            @PathVariable Long id,
            @RequestBody ApiDefinition apiDefinition,
            @RequestHeader("X-User-Id") Long userId) {
        apiDefinition.setId(id);
        apiDefinition.setUpdatedBy(userId);
        return ApiResponse.success(apiMarketService.updateApiDefinition(apiDefinition));
    }

    @RequiresPermission(code = "dict:api-market:delete", name = "删除API")
    @Operation(summary = "删除API")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        apiMarketService.deleteApiDefinition(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "dict:api-market:subscribe", name = "订阅API")
    @Operation(summary = "订阅API（JSON Body）")
    @PostMapping("/subscribe")
    public ApiResponse<ApiSubscription> subscribeWithBody(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestBody Map<String, Object> body) {
        Long apiId = Long.valueOf(body.get("apiId").toString());
        String subscriberName = body.getOrDefault("subscriberName", "默认调用方").toString();
        Integer callLimit = body.get("quota") != null ? Integer.valueOf(body.get("quota").toString()) : null;
        return ApiResponse.success(apiMarketService.subscribe(tenantId, apiId, subscriberName, callLimit, null));
    }

    @RequiresPermission(code = "dict:api-market:list", name = "查询API使用统计")
    @Operation(summary = "API使用统计")
    @GetMapping("/usage")
    public ApiResponse<Map<String, Object>> usage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(required = false) Long apiId) {
        return ApiResponse.success(apiMarketService.getUsageStats(tenantId, apiId));
    }

    @RequiresPermission(code = "dict:api-market:list", name = "记录API使用")
    @Operation(summary = "记录API使用（JSON Body）")
    @PostMapping("/record-usage")
    public ApiResponse<ApiUsageRecord> recordUsageWithBody(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestBody Map<String, Object> body) {
        Long apiId = Long.valueOf(body.get("apiId").toString());
        Long subscriptionId = Long.valueOf(body.get("subscriptionId").toString());
        Integer responseStatus = Integer.valueOf(body.get("responseStatus").toString());
        Integer responseTimeMs = body.get("responseTimeMs") != null ? Integer.valueOf(body.get("responseTimeMs").toString()) : null;
        return ApiResponse.success(apiMarketService.recordUsage(tenantId, apiId, subscriptionId, responseStatus, responseTimeMs));
    }
}
