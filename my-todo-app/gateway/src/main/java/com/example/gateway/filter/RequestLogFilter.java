package com.example.gateway.filter;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求日志全局过滤器
 * <p>
 * 为每个经过网关的请求生成唯一请求ID，记录请求信息和响应耗时。
 * 请求ID通过 X-Request-Id 请求头传递给下游服务，用于链路追踪。
 * </p>
 */
@Slf4j
@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    /** 请求ID请求头名称 */
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    /** 请求开始时间的 Exchange 属性键 */
    private static final String START_TIME_ATTR = "startTime";

    /**
     * 过滤器核心逻辑
     * <p>
     * 1. 生成唯一请求ID
     * 2. 将请求ID添加到请求头
     * 3. 记录请求开始时间
     * 4. 打印请求日志（方法、路径、来源IP）
     * 5. 请求完成后打印响应日志（状态码、耗时）
     * </p>
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 生成唯一请求ID
        String requestId = IdUtil.fastSimpleUUID();

        // 将请求ID添加到请求头，传递给下游服务
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(REQUEST_ID_HEADER, requestId)
                .build();

        // 记录请求开始时间，用于计算耗时
        exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());

        log.info("[{}] 请求: {} {} 来源: {}",
                requestId,
                request.getMethod(),
                request.getPath(),
                request.getRemoteAddress());

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    // 请求完成后计算并打印耗时
                    Long startTime = exchange.getAttribute(START_TIME_ATTR);
                    if (startTime != null) {
                        long duration = System.currentTimeMillis() - startTime;
                        log.info("[{}] 响应: {} - 耗时{}ms",
                                requestId,
                                exchange.getResponse().getStatusCode(),
                                duration);
                    }
                }));
    }

    /**
     * 过滤器优先级，值越小优先级越高
     * <p>设置为 -200，确保在其他过滤器之前执行</p>
     */
    @Override
    public int getOrder() {
        return -200;
    }
}
