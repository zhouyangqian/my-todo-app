package com.example.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE（Server-Sent Events）服务实现
 * <p>
 * 管理用户的 SSE 长连接，用于实时推送踢出通知等事件。
 * 使用 ConcurrentHashMap 存储 userId -> SseEmitter 的映射关系。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

    /** SSE 连接超时时间：30分钟 */
    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    /** userId -> SseEmitter 映射 */
    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    @Override
    public SseEmitter createEmitter(Long userId) {
        String key = String.valueOf(userId);

        // 关闭已存在的连接
        removeEmitter(userId);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emitter.onCompletion(() -> {
            log.debug("SSE 连接完成: userId={}", userId);
            emitterMap.remove(key);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE 连接超时: userId={}", userId);
            emitterMap.remove(key);
        });

        emitter.onError(e -> {
            log.debug("SSE 连接错误: userId={}, error={}", userId, e.getMessage());
            emitterMap.remove(key);
        });

        emitterMap.put(key, emitter);
        log.info("创建 SSE 连接: userId={}, 当前连接数={}", userId, emitterMap.size());

        return emitter;
    }

    @Override
    public void sendKickNotification(Long userId, String reason) {
        String key = String.valueOf(userId);
        SseEmitter emitter = emitterMap.get(key);

        if (emitter == null) {
            log.debug("用户 {} 无活跃 SSE 连接，跳过踢出通知", userId);
            return;
        }

        try {
            emitter.send(
                SseEmitter.event()
                    .name("kick")
                    .data(reason)
            );
            log.info("已发送踢出通知: userId={}, reason={}", userId, reason);

            // 发送踢出通知后关闭连接
            emitter.complete();
            emitterMap.remove(key);
        } catch (IOException e) {
            log.warn("发送踢出通知失败: userId={}, error={}", userId, e.getMessage());
            emitterMap.remove(key);
        }
    }

    @Override
    public void removeEmitter(Long userId) {
        String key = String.valueOf(userId);
        SseEmitter emitter = emitterMap.remove(key);
        if (emitter != null) {
            emitter.complete();
        }
    }

    /**
     * 定时心跳，保持 SSE 连接活跃
     * 每30秒向所有连接发送心跳事件
     */
    @Scheduled(fixedRate = 30000)
    public void heartbeat() {
        if (emitterMap.isEmpty()) {
            return;
        }

        emitterMap.forEach((key, emitter) -> {
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("heartbeat")
                        .data("ping")
                );
            } catch (IOException e) {
                log.debug("心跳发送失败，移除连接: key={}", key);
                emitterMap.remove(key);
            }
        });
    }

    @Override
    public int getConnectionCount() {
        return emitterMap.size();
    }
}
