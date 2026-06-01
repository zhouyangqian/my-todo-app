package com.example.auth.controller;

import com.example.auth.api.dto.LoginDTO;
import com.example.auth.api.dto.UserProfileDTO;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.permission.checker.PermissionChecker;
import com.example.auth.api.vo.ChangePasswordVO;
import com.example.auth.api.vo.LoginVO;
import com.example.auth.api.vo.RefreshTokenVO;
import com.example.auth.api.vo.RegisterVO;
import com.example.auth.api.vo.ResetPasswordVO;
import com.example.auth.service.AuthService;
import com.example.common.core.result.ApiResponse;
import com.example.common.security.jwt.JwtTokenProvider;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final PermissionChecker permissionChecker;

    /**
     * 用户登录
     *
     * @param request     登录请求（用户名、密码、租户ID等）
     * @param httpRequest HTTP 请求对象（用于获取客户端IP）
     * @return 登录响应（包含访问令牌、刷新令牌和用户信息）
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<LoginDTO> login(@Valid @RequestBody LoginVO request,
                                            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        // 如果未传入设备信息，使用 User-Agent 作为设备信息
        if (request.getDeviceInfo() == null) {
            request.setDeviceInfo(httpRequest.getHeader("User-Agent"));
        }
        LoginDTO response = authService.login(request, ipAddress);
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
    public ApiResponse<LoginDTO> refreshToken(@Valid @RequestBody RefreshTokenVO request) {
        LoginDTO response = authService.refreshToken(request);
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
     * <p>从 JWT Token 中解析用户ID，防止请求头伪造</p>
     *
     * @param authorization Authorization 请求头（Bearer Token）
     */
    @Operation(summary = "退出所有设备登录")
    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAll(@RequestHeader(value = "Authorization", required = false) String authorization) {
        // 从 Token 中解析用户ID，防止请求头伪造
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ApiResponse.error(401, "缺少有效的认证令牌");
        }
        String token = authorization.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ApiResponse.error(401, "认证令牌无效或已过期");
        }
        Long userId = jwtTokenProvider.getUserId(token);
        authService.logoutAll(userId);
        return ApiResponse.success();
    }

    /**
     * 管理员踢出用户（强制下线）
     * <p>使目标用户的所有会话和刷新令牌失效，需要 system:user:kick 权限</p>
     *
     * @param id 要踢出的用户ID
     */
    @Operation(summary = "踢出用户")
    @RequiresPermission(code = "system:user:kick", name = "踢出用户")
    @PostMapping("/kick-user/{id}")
    public ApiResponse<Void> kickUser(@PathVariable Long id) {
        authService.logoutAll(id);
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
    public ApiResponse<Long> register(@Valid @RequestBody RegisterVO request) {
        Long userId = authService.register(request);
        return ApiResponse.success(userId);
    }

    /**
     * 修改密码
     * <p>修改成功后会强制所有设备重新登录</p>
     * <p>从 JWT Token 中解析用户ID，防止请求头伪造</p>
     *
     * @param authorization Authorization 请求头（Bearer Token）
     * @param request 修改密码请求（旧密码、新密码、确认密码）
     */
    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@RequestHeader(value = "Authorization", required = false) String authorization,
                                            @Valid @RequestBody ChangePasswordVO request) {
        // 从 Token 中解析用户ID，防止请求头伪造
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ApiResponse.error(401, "缺少有效的认证令牌");
        }
        String token = authorization.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ApiResponse.error(401, "认证令牌无效或已过期");
        }
        Long userId = jwtTokenProvider.getUserId(token);
        authService.changePassword(userId, request);
        return ApiResponse.success();
    }

    /**
     * 管理员重置用户密码
     * <p>无需验证旧密码，直接设置新密码，修改成功后强制该用户所有设备重新登录</p>
     * <p>需要管理员权限，从请求头获取当前用户信息进行权限验证</p>
     *
     * @param request     重置密码请求（用户ID、新密码）
     * @param authorization Authorization 请求头（Bearer Token）
     */
    @Operation(summary = "重置用户密码")
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordVO request,
                                            @RequestHeader(value = "Authorization", required = false) String authorization) {
        // 验证认证令牌
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ApiResponse.error(401, "缺少有效的认证令牌");
        }

        String token = authorization.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ApiResponse.error(401, "认证令牌无效或已过期");
        }
        Long currentUserId = jwtTokenProvider.getUserId(token);
        Long tenantId = jwtTokenProvider.getTenantId(token);

        // 权限检查：只有管理员可以重置其他用户的密码
        // 普通用户只能重置自己的密码（需要验证旧密码，应该使用 changePassword 接口）
        // 这里简化处理：检查当前用户是否与目标用户相同，或者当前用户是否为管理员
        // 完整实现应该调用 permission-service 检查 "user:reset-password" 权限
        if (!currentUserId.equals(request.getUserId())) {
            // 重置其他用户的密码，需要管理员权限
            // TODO: 调用 permission-service 检查 "user:reset-password" 权限
            // 当前简化实现：检查用户是否为管理员（假设管理员用户 ID 为 1）
            // 生产环境应该使用完整的权限检查
            boolean isAdmin = checkAdminPermission(currentUserId, tenantId);
            if (!isAdmin) {
                return ApiResponse.error(403, "没有权限重置其他用户的密码");
            }
        }

        authService.resetPassword(request.getUserId(), request.getNewPassword());
        return ApiResponse.success();
    }

    /**
     * 检查用户是否有重置密码的权限
     * <p>
     * 通过调用 permission-service 的权限检查接口，
     * 验证用户是否拥有 "user:reset-password" 权限。
     * </p>
     *
     * @param userId 用户ID
     * @return true-有权限，false-无权限
     */
    private boolean checkAdminPermission(Long userId, Long tenantId) {
        try {
            return permissionChecker.checkPermission(userId, tenantId, "user:reset-password");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取当前用户档案信息
     * <p>从请求头中读取网关注入的用户ID，返回用户详细信息</p>
     *
     * @param userId 用户ID（由网关从 JWT 解析后注入 X-User-Id 请求头）
     * @return 用户档案信息
     */
    @Operation(summary = "获取当前用户档案")
    @GetMapping("/profile")
    public ApiResponse<UserProfileDTO> getProfile(
            @RequestHeader("X-User-Id") Long userId) {
        UserProfileDTO profile = authService.getUserProfile(userId);
        return ApiResponse.success(profile);
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
