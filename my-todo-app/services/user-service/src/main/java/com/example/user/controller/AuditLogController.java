package com.example.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.user.entity.AuditLog;
import com.example.user.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 审计日志控制器
 * <p>
 * 提供审计日志的分页查询接口。
 * </p>
 */
@Tag(name = "审计日志", description = "审计日志查询API")
@RestController
@RequestMapping("/api/users/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * 分页查询审计日志
     */
    @RequiresPermission(code = "system:audit:list", name = "查询审计日志")
    @Operation(summary = "分页查询审计日志")
    @GetMapping
    public ApiResponse<PageResult<AuditLog>> getAuditLogPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) Long operatorId) {
        Page<AuditLog> result = auditLogService.getAuditLogPage(tenantId, page, size, operationType, operatorId);
        PageResult<AuditLog> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }
}
