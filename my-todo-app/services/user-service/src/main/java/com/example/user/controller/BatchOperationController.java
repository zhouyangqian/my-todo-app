package com.example.user.controller;

import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.user.api.dto.BatchResultDTO;
import com.example.user.api.vo.BatchOperationVO;
import com.example.user.service.BatchOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 批量操作控制器
 * <p>
 * 提供用户的批量禁用、批量删除、批量分配角色等接口。
 * </p>
 */
@Tag(name = "用户批量操作", description = "用户批量禁用、删除、分配角色API")
@RestController
@RequestMapping("/api/users/batch")
@RequiredArgsConstructor
public class BatchOperationController {

    private final BatchOperationService batchOperationService;

    /**
     * 批量禁用用户
     */
    @RequiresPermission(code = "system:user:disable", name = "批量禁用用户")
    @Operation(summary = "批量禁用用户")
    @PostMapping("/disable")
    public ApiResponse<BatchResultDTO> batchDisableUsers(
            @Valid @RequestBody BatchOperationVO vo,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long operatorId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        BatchResultDTO result = batchOperationService.batchDisableUsers(
                vo.getUserIds(), tenantId, operatorId, ipAddress);
        return ApiResponse.success(result);
    }

    /**
     * 批量删除用户（软删除）
     */
    @RequiresPermission(code = "system:user:delete", name = "批量删除用户")
    @Operation(summary = "批量删除用户")
    @PostMapping("/delete")
    public ApiResponse<BatchResultDTO> batchDeleteUsers(
            @Valid @RequestBody BatchOperationVO vo,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long operatorId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        BatchResultDTO result = batchOperationService.batchDeleteUsers(
                vo.getUserIds(), tenantId, operatorId, ipAddress);
        return ApiResponse.success(result);
    }

    /**
     * 批量分配角色
     */
    @RequiresPermission(code = "system:user:assignToUser", name = "批量分配角色")
    @Operation(summary = "批量分配角色")
    @PostMapping("/assign-roles")
    public ApiResponse<BatchResultDTO> batchAssignRoles(
            @Valid @RequestBody BatchOperationVO vo,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long operatorId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        BatchResultDTO result = batchOperationService.batchAssignRoles(
                vo.getUserIds(), vo.getRoleIds(), tenantId, operatorId, ipAddress);
        return ApiResponse.success(result);
    }
}
