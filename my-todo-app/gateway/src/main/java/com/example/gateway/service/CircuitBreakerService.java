package com.example.gateway.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 断路器服务
 * <p>
 * 为每个下游服务维护独立的断路器状态。基于失败次数阈值和冷却时间
 * 实现 CLOSED -> OPEN -> HALF_OPEN 状态转换。
 * 不依赖外部库，纯手写实现。
 * </p>
 */
@Slf4j
@Component
public class CircuitBreakerService {

    /** 断路器状态枚举 */
    public enum CircuitState {
        /** 关闭 — 正常放行请求 */
        CLOSED,
        /** 打开 — 拒绝所有请求，返回降级响应 */
        OPEN,
        /** 半开 — 放行1个试探请求 */
        HALF_OPEN
    }

    /**
     * 断路器状态信息
     */
    @Data
    public static class CircuitBreakerState {
        /** 当前状态 */
        private CircuitState state = CircuitState.CLOSED;
        /** 连续失败次数 */
        private int failureCount = 0;
        /** 最后一次失败时间 */
        private String lastFailureTime;
        /** 连续成功次数（HALF_OPEN状态下使用） */
        private int successCount = 0;
        /** 进入OPEN状态的时间戳（毫秒），用于计算冷却时间 */
        private long openedAt = 0;
    }

    /** 失败阈值：连续失败超过此数值则打开断路器 */
    private static final int FAILURE_THRESHOLD = 5;

    /** OPEN 状态持续时间（毫秒），超过后转为 HALF_OPEN */
    private static final long OPEN_DURATION_MS = 60_000;

    /** 各服务的断路器状态 */
    private final ConcurrentHashMap<String, CircuitBreakerState> breakerMap = new ConcurrentHashMap<>();

    /**
     * 记录请求失败
     * <p>
     * CLOSED 状态下累加失败计数，达到阈值转 OPEN；
     * HALF_OPEN 状态下立即转 OPEN。
     * </p>
     *
     * @param serviceId 服务标识
     */
    public void recordFailure(String serviceId) {
        CircuitBreakerState state = getOrCreate(serviceId);
        state.setSuccessCount(0);

        switch (state.getState()) {
            case CLOSED:
                state.setFailureCount(state.getFailureCount() + 1);
                state.setLastFailureTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                if (state.getFailureCount() >= FAILURE_THRESHOLD) {
                    state.setState(CircuitState.OPEN);
                    state.setOpenedAt(System.currentTimeMillis());
                    log.warn("断路器打开: {}，连续失败{}次", serviceId, state.getFailureCount());
                }
                break;
            case HALF_OPEN:
                state.setState(CircuitState.OPEN);
                state.setOpenedAt(System.currentTimeMillis());
                state.setFailureCount(state.getFailureCount() + 1);
                state.setLastFailureTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                log.warn("断路器重新打开: {}，试探请求失败", serviceId);
                break;
            default:
                break;
        }
    }

    /**
     * 记录请求成功
     * <p>
     * CLOSED 状态下重置失败计数；
     * HALF_OPEN 状态下转回 CLOSED。
     * </p>
     *
     * @param serviceId 服务标识
     */
    public void recordSuccess(String serviceId) {
        CircuitBreakerState state = getOrCreate(serviceId);

        switch (state.getState()) {
            case CLOSED:
                state.setFailureCount(0);
                state.setSuccessCount(state.getSuccessCount() + 1);
                break;
            case HALF_OPEN:
                state.setState(CircuitState.CLOSED);
                state.setFailureCount(0);
                state.setSuccessCount(state.getSuccessCount() + 1);
                log.info("断路器关闭: {}，试探请求成功", serviceId);
                break;
            default:
                break;
        }
    }

    /**
     * 获取服务的断路器状态
     *
     * @param serviceId 服务标识
     * @return 断路器状态
     */
    public CircuitBreakerState getState(String serviceId) {
        return getOrCreate(serviceId);
    }

    /**
     * 判断断路器是否处于 OPEN 状态
     * <p>
     * 如果 OPEN 持续时间超过阈值，自动转为 HALF_OPEN。
     * </p>
     *
     * @param serviceId 服务标识
     * @return true 表示断路器打开（应拒绝请求）
     */
    public boolean isOpen(String serviceId) {
        CircuitBreakerState state = getOrCreate(serviceId);
        if (state.getState() == CircuitState.OPEN) {
            // 检查是否超过冷却时间
            if (System.currentTimeMillis() - state.getOpenedAt() >= OPEN_DURATION_MS) {
                state.setState(CircuitState.HALF_OPEN);
                state.setSuccessCount(0);
                log.info("断路器进入半开状态: {}", serviceId);
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 判断是否允许请求通过
     * <p>
     * CLOSED: 放行；OPEN: 拒绝；HALF_OPEN: 仅放行1个请求。
     * </p>
     *
     * @param serviceId 服务标识
     * @return true 表示允许请求通过
     */
    public boolean allowRequest(String serviceId) {
        CircuitBreakerState state = getOrCreate(serviceId);

        switch (state.getState()) {
            case CLOSED:
                return true;
            case OPEN:
                // 检查是否超过冷却时间
                if (System.currentTimeMillis() - state.getOpenedAt() >= OPEN_DURATION_MS) {
                    state.setState(CircuitState.HALF_OPEN);
                    state.setSuccessCount(0);
                    log.info("断路器进入半开状态: {}", serviceId);
                    return true;
                }
                return false;
            case HALF_OPEN:
                // HALF_OPEN 状态下，仅允许一个请求通过（通过 successCount 判断）
                if (state.getSuccessCount() == 0 && state.getFailureCount() < FAILURE_THRESHOLD) {
                    return true;
                }
                return false;
            default:
                return true;
        }
    }

    /**
     * 获取所有服务的断路器状态列表
     *
     * @return 断路器状态列表
     */
    public List<Map<String, Object>> getAllStates() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, CircuitBreakerState> entry : breakerMap.entrySet()) {
            CircuitBreakerState state = entry.getValue();
            result.add(Map.of(
                    "serviceId", entry.getKey(),
                    "state", state.getState().name(),
                    "failureCount", state.getFailureCount(),
                    "successCount", state.getSuccessCount(),
                    "lastFailureTime", state.getLastFailureTime() != null ? state.getLastFailureTime() : ""
            ));
        }
        return result;
    }

    /**
     * 获取或创建服务的断路器状态
     */
    private CircuitBreakerState getOrCreate(String serviceId) {
        return breakerMap.computeIfAbsent(serviceId, id -> new CircuitBreakerState());
    }
}
