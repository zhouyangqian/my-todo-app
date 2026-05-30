package com.example.auth.controller;

import com.example.auth.service.SseService;
import com.example.common.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 控制器
 * <p>
 * 提供 SSE（Server-Sent Events）连接端点，用于实时推送踢出通知等事件。
 * 前端通过 EventSource 建立连接，后端通过 SseEmitter 推送事件。
 * </p>
 */
@Slf4j
@Tag(name = "SSE推送", description = "实时消息推送接口")
@RestController
@RequestMapping("/api/auth/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 建立 SSE 连接
     * <p>
     * 前端通过 EventSource 连接此端点，支持两种方式传递 Token：
     * 1. 通过 query 参数 token（推荐，因为 EventSource 不支持自定义请求头）
     * 2. 通过 Authorization 请求头
     * </p>
     *
     * @param token         query 参数中的 JWT Token
     * @param authorization Authorization 请求头中的 Bearer Token
     * @return SseEmitter 实例
     */
    @Operation(summary = "建立SSE连接")
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(
            @RequestParam(value = "token", required = false) String token,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        // 优先从 query 参数获取 token，否则从 Authorization 请求头获取
        String jwtToken = null;
        if (token != null && !token.isEmpty()) {
            jwtToken = token;
        } else if (authorization != null && authorization.startsWith("Bearer ")) {
            jwtToken = authorization.substring(7);
        }

        if (jwtToken == null || !jwtTokenProvider.validateToken(jwtToken)) {
            log.warn("SSE 连接认证失败：无效或缺失的令牌");
            SseEmitter emitter = new SseEmitter(0L);
            emitter.completeWithError(new RuntimeException("认证失败"));
            return emitter;
        }

        Long userId = jwtTokenProvider.getUserId(jwtToken);
        log.info("建立 SSE 连接: userId={}", userId);

        return sseService.createEmitter(userId);
    }
}
