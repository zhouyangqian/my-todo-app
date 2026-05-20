package com.example.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.entity.LoginSession;
import com.example.auth.mapper.LoginSessionMapper;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话管理控制器
 * <p>
 * 查询在线会话、强制下线指定会话。
 * </p>
 */
@Tag(name = "会话管理", description = "在线会话查询和管理API")
@RestController
@RequestMapping("/api/auth/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final LoginSessionMapper loginSessionMapper;

    @RequiresPermission(code = "system:session:list", name = "查询在线会话")
    @Operation(summary = "分页查询在线会话")
    @GetMapping("/get-session-page")
    public ApiResponse<PageResult<LoginSession>> getSessionPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId) {
        LambdaQueryWrapper<LoginSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoginSession::getTenantId, tenantId)
               .eq(LoginSession::getStatus, 1)
               .eq(userId != null, LoginSession::getUserId, userId)
               .orderByDesc(LoginSession::getLoginTime);
        Page<LoginSession> result = loginSessionMapper.selectPage(new Page<>(page, size), wrapper);
        PageResult<LoginSession> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "system:session:kick", name = "强制下线会话")
    @Operation(summary = "强制下线指定会话")
    @PostMapping("/force-logout/{id}")
    public ApiResponse<Void> forceLogout(@PathVariable Long id) {
        LoginSession session = loginSessionMapper.selectById(id);
        if (session != null && session.getStatus() == 1) {
            session.setStatus(0);
            session.setLogoutTime(LocalDateTime.now());
            loginSessionMapper.updateById(session);
        }
        return ApiResponse.success();
    }

    @RequiresPermission(code = "system:session:list", name = "查询在线会话")
    @Operation(summary = "获取在线会话数量")
    @GetMapping("/get-online-count")
    public ApiResponse<Long> getOnlineCount(@RequestHeader("X-Tenant-Id") Long tenantId) {
        Long count = loginSessionMapper.selectCount(
                new LambdaQueryWrapper<LoginSession>()
                        .eq(LoginSession::getTenantId, tenantId)
                        .eq(LoginSession::getStatus, 1));
        return ApiResponse.success(count);
    }
}
