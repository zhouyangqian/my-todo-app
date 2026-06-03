package com.example.auth.controller;

import com.example.auth.api.dto.TenantDTO;
import com.example.auth.api.dto.TenantRegisterResultDTO;
import com.example.auth.api.vo.TenantRegisterVO;
import com.example.auth.service.TenantService;
import com.example.common.core.result.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 租户管理控制器
 * <p>
 * 提供租户注册和租户信息查询接口。
 * </p>
 */
@Tag(name = "租户管理", description = "租户注册和租户信息查询接口")
@RestController
@RequestMapping("/api/auth/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    /**
     * 租户注册
     * <p>创建租户并自动创建管理员账户，返回管理员凭据供前端展示</p>
     *
     * @param request 租户注册请求
     * @return 注册结果（含租户ID和管理员凭据）
     */
    @Operation(summary = "租户注册")
    @PostMapping("/register")
    public ApiResponse<TenantRegisterResultDTO> register(@Valid @RequestBody TenantRegisterVO request) {
        TenantRegisterResultDTO result = tenantService.registerTenant(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取当前租户信息
     * <p>从请求头 X-Tenant-Id 获取租户ID</p>
     *
     * @param tenantId 租户ID（从请求头获取）
     * @return 租户信息
     */
    @Operation(summary = "获取当前租户信息")
    @GetMapping("/info")
    public ApiResponse<TenantDTO> getTenantInfo(@RequestHeader("X-Tenant-Id") Long tenantId) {
        TenantDTO tenantDTO = tenantService.getTenantInfo(tenantId);
        return ApiResponse.success(tenantDTO);
    }
}
