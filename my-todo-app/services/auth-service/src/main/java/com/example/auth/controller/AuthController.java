package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.service.AuthService;
import com.example.common.core.result.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 提供用户登录、注册、令牌刷新、退出登录、修改密码等接口。
 * 登录和注册接口无需认证，其他接口需要通过网关的 Token 验证。
 * </p>
 */
@Tag(name = "认证管理", description = "用户登录、注册、令牌刷新等认证接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     *
     * @param request     登录请求（用户名、密码、租户ID等）
     * @param httpRequest HTTP 请求对象（用于获取客户端IP）
     * @return 登录响应（包含访问令牌、刷新令牌和用户信息）
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        // 如果未传入设备信息，使用 User-Agent 作为设备信息
        if (request.getDeviceInfo() == null) {
            request.setDeviceInfo(httpRequest.getHeader("User-Agent"));
        }
        LoginResponse response = authService.login(request, ipAddress);
        return ApiResponse.success(response);
    }

    /**
     * 刷新访问令牌
     *
     * @param request 刷新令牌请求（包含刷新令牌）
     * @return 新的访问令牌和刷新令牌
     */
    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ApiResponse.success(response);
    }

    /**
     * 用户退出登录（当前设备）
     * <p>使当前令牌对应的会话失效</p>
     *
     * @param authorization Authorization 请求头（Bearer Token）
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            authService.logout(token);
        }
        return ApiResponse.success();
    }

    /**
     * 退出所有设备的登录
     * <p>使该用户所有会话和刷新令牌失效</p>
     *
     * @param userId 用户ID（由网关从 Token 中解析注入）
     */
    @Operation(summary = "退出所有设备登录")
    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAll(@RequestHeader("X-User-Id") Long userId) {
        authService.logoutAll(userId);
        return ApiResponse.success();
    }

    /**
     * 用户注册
     *
     * @param request 注册请求（用户名、密码、邮箱等）
     * @return 新创建的用户ID
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<Long> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = authService.register(request);
        return ApiResponse.success(userId);
    }

    /**
     * 修改密码
     * <p>修改成功后会强制所有设备重新登录</p>
     *
     * @param userId  用户ID（由网关注入）
     * @param request 修改密码请求（旧密码、新密码、确认密码）
     */
    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@RequestHeader("X-User-Id") Long userId,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        return ApiResponse.success();
    }

    /** 健康检查接口 */
    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("认证服务运行正常");
    }

    /**
     * 获取客户端真实IP地址
     * <p>支持通过代理（Nginx等）转发的请求，依次检查 X-Forwarded-For、X-Real-IP 请求头</p>
     *
     * @param request HTTP 请求对象
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");   // 代理转发的客户端IP
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");            // Nginx 代理设置的真实IP
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();                    // 直接连接的远程地址
        }
        // 多级代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
