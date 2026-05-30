package com.example.dict.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.dict.entity.ThirdPartyApi;
import com.example.dict.entity.ThirdPartyCallLog;
import com.example.dict.service.ThirdPartyApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 第三方API控制器
 */
@Tag(name = "第三方API", description = "第三方API管理与调用日志")
@RestController
@RequestMapping("/api/third-party")
@RequiredArgsConstructor
public class ThirdPartyApiController {

    private final ThirdPartyApiService thirdPartyApiService;

    @Operation(summary = "分页查询第三方API")
    @GetMapping("/page")
    public ApiResponse<PageResult<ThirdPartyApi>> getThirdPartyApiPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String apiName) {
        Page<ThirdPartyApi> result = thirdPartyApiService.getThirdPartyApiPage(tenantId, page, size, apiName);
        return ApiResponse.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "获取第三方API详情")
    @GetMapping("/{id}")
    public ApiResponse<ThirdPartyApi> getThirdPartyApi(@PathVariable Long id) {
        return ApiResponse.success(thirdPartyApiService.getById(id));
    }

    @Operation(summary = "创建第三方API")
    @PostMapping("/create")
    public ApiResponse<ThirdPartyApi> createThirdPartyApi(@RequestBody ThirdPartyApi api) {
        return ApiResponse.success(thirdPartyApiService.createThirdPartyApi(api));
    }

    @Operation(summary = "更新第三方API")
    @PutMapping("/update/{id}")
    public ApiResponse<ThirdPartyApi> updateThirdPartyApi(
            @PathVariable Long id,
            @RequestBody ThirdPartyApi api) {
        api.setId(id);
        return ApiResponse.success(thirdPartyApiService.updateThirdPartyApi(api));
    }

    @Operation(summary = "删除第三方API")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deleteThirdPartyApi(@PathVariable Long id, @RequestHeader("X-Tenant-Id") Long tenantId) {
        thirdPartyApiService.deleteThirdPartyApi(id, tenantId);
        return ApiResponse.success();
    }

    @Operation(summary = "分页查询调用日志")
    @GetMapping("/call-logs/page")
    public ApiResponse<PageResult<ThirdPartyCallLog>> getCallLogPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(required = false) Long apiId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ThirdPartyCallLog> result = thirdPartyApiService.getCallLogPage(tenantId, apiId, page, size);
        return ApiResponse.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "API健康检查")
    @GetMapping("/health-check/{id}")
    public ApiResponse<Map<String, Object>> healthCheck(@PathVariable Long id,
                                                        @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ApiResponse.success(thirdPartyApiService.healthCheck(id, tenantId));
    }
}
