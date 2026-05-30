package com.example.gateway.filter;

import com.example.gateway.service.MetricsCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * API 指标收集全局过滤器
 * <p>
 * 在请求完成后调用 MetricsCollector.record() 记录响应时间、状态码等指标。
 * order=-60，在其他业务过滤器之后执行以确保记录到最终状态。
 * </p>
 */
@Slf4j
@Component
public class MetricsFilter implements GlobalFilter, Ordered {

    private static final String METRICS_START_TIME = "metricsStartTime";

    private final MetricsCollector metricsCollector;

    public MetricsFilter(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 跳过内部端点
        if (path.startsWith("/actuator") || path.startsWith("/gateway/")) {
            return chain.filter(exchange);
        }

        // 记录开始时间
        exchange.getAttributes().put(METRICS_START_TIME, System.currentTimeMillis());

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    Long startTime = exchange.getAttribute(METRICS_START_TIME);
                    if (startTime != null) {
                        long responseTime = System.currentTimeMillis() - startTime;
                        String method = exchange.getRequest().getMethod().name();
                        HttpStatusCode status = exchange.getResponse().getStatusCode();
                        int statusCode = status != null ? status.value() : 500;

                        metricsCollector.record(path, method, responseTime, statusCode);
                        log.debug("指标记录: {} {} - {}ms - {}", method, path, responseTime, statusCode);
                    }
                }));
    }

    @Override
    public int getOrder() {
        return -60;
    }
}
