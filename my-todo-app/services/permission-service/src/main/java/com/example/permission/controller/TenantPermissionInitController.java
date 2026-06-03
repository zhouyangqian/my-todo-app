package com.example.permission.controller;

import com.example.common.core.result.ApiResponse;
import com.example.permission.api.vo.TenantInitVO;
import com.example.permission.service.TenantPermissionInitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 租户权限初始化控制器（内部接口）
 * <p>
 * 提供给 auth-service 在租户注册成功后调用的内部接口，
 * 用于初始化新租户的默认角色和权限。
 * 此接口为服务间内部调用，不暴露给前端，不要求认证。
 * </p>
 */
@Slf4j
@Tag(name = "租户权限初始化", description = "新租户角色权限初始化（内部接口）")
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class TenantPermissionInitController {

    private final TenantPermissionInitService tenantPermissionInitService;

    /**
     * 初始化新租户的默认角色和权限
     * <p>
     * 从模板租户（tenantId=1）复制默认权限和角色到新租户，
     * 并将管理员用户绑定到 ADMIN 角色。
     * 此接口为服务间内部调用，无需认证。
     * </p>
     *
     * @param request 包含 tenantId、adminUserId、adminUsername
     * @return 成功响应
     */
    @Operation(summary = "初始化租户权限")
    @PostMapping("/init-tenant")
    public ApiResponse<Void> initTenant(@RequestBody TenantInitVO request) {
        log.info("收到租户权限初始化请求: tenantId={}, adminUserId={}",
                request.getTenantId(), request.getAdminUserId());
        tenantPermissionInitService.initTenantPermissions(
                request.getTenantId(),
                request.getAdminUserId(),
                request.getAdminUsername()
        );
        return ApiResponse.success();
    }
}
