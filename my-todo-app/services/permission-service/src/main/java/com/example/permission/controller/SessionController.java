package com.example.permission.controller;

import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.permission.entity.Session;
import com.example.permission.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话管理控制器
 * <p>
 * 提供在线用户查询、会话踢出、过期会话清理等接口。
 * 通过请求头 X-Tenant-Id 实现多租户隔离。
 * </p>
 */
@Tag(name = "会话管理")
@RestController
@RequestMapping("/api/permissions/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    /**
     * 获取在线用户列表
     *
     * @param tenantId 租户ID
     * @return 活跃会话列表
     */
    @Operation(summary = "获取在线用户列表")
    @RequiresPermission(code = "system:session:list", name = "查询在线用户")
    @GetMapping("/online")
    public ApiResponse<List<Session>> getOnlineSessions(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        List<Session> sessions = sessionService.getOnlineSessions(tenantId);
        return ApiResponse.success(sessions);
    }

    /**
     * 踢出指定会话
     *
     * @param sessionId 会话ID
     * @return 空响应
     */
    @Operation(summary = "踢出指定会话")
    @RequiresPermission(code = "system:session:kick", name = "踢出会话")
    @PostMapping("/kick/{sessionId}")
    public ApiResponse<Void> kickSession(@PathVariable String sessionId) {
        sessionService.kickSession(sessionId);
        return ApiResponse.success();
    }

    /**
     * 踢出用户所有会话
     *
     * @param userId 用户ID
     * @return 空响应
     */
    @Operation(summary = "踢出用户所有会话")
    @RequiresPermission(code = "system:session:kick", name = "踢出会话")
    @PostMapping("/kick-user/{userId}")
    public ApiResponse<Void> kickUserSessions(@PathVariable Long userId) {
        sessionService.kickAllUserSessions(userId);
        return ApiResponse.success();
    }

    /**
     * 清理过期会话
     *
     * @return 空响应
     */
    @Operation(summary = "清理过期会话")
    @RequiresPermission(code = "system:session:clean", name = "清理过期会话")
    @PostMapping("/clean-expired")
    public ApiResponse<Void> cleanExpiredSessions() {
        sessionService.cleanExpiredSessions();
        return ApiResponse.success();
    }
}
