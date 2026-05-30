package com.example.permission.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.permission.entity.PermissionLog;
import com.example.permission.service.PermissionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "权限审计日志", description = "权限变更审计日志查询")
@RestController
@RequestMapping("/api/permissions/logs")
@RequiredArgsConstructor
public class PermissionLogController {

    private final PermissionLogService permissionLogService;

    @Operation(summary = "分页查询权限审计日志")
    @RequiresPermission(code = "system:permission:log", name = "查询权限日志")
    @GetMapping
    public ApiResponse<Page<PermissionLog>> getLogPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Integer result,
            HttpServletRequest request) {
        Long tenantId = parseLongHeader(request, "X-Tenant-Id");
        if (tenantId == null) {
            tenantId = 1L;
        }
        Page<PermissionLog> logPage = permissionLogService.getPermissionLogPage(tenantId, page, size, userId, action, result);
        return ApiResponse.success(logPage);
    }

    private Long parseLongHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
