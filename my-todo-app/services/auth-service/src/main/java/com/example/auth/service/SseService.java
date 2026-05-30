package com.example.auth.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE（Server-Sent Events）服务接口
 * <p>
 * 管理用户的 SSE 长连接，用于实时推送踢出通知等事件。
 * </p>
 */
public interface SseService {

    /**
     * 为用户创建 SSE 连接
     *
     * @param userId 用户ID
     * @return SseEmitter 实例
     */
    SseEmitter createEmitter(Long userId);

    /**
     * 向用户发送踢出通知
     *
     * @param userId 用户ID
     * @param reason 踢出原因
     */
    void sendKickNotification(Long userId, String reason);

    /**
     * 移除用户的 SSE 连接
     *
     * @param userId 用户ID
     */
    void removeEmitter(Long userId);

    /**
     * 获取当前活跃 SSE 连接数
     */
    int getConnectionCount();
}
