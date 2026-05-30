package com.example.gateway.filter;

import com.example.gateway.service.CircuitBreakerService;
import com.example.gateway.service.DegradationServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 断路器全局过滤器
 * <p>
 * 在 TokenValidation 之后执行（order=-90），根据目标服务的断路器状态
 * 决定是否放行请求。OPEN 状态返回 503 + 降级响应（优先使用 DegradationService 配置）；
 * 请求完成后根据响应状态码记录成功/失败。
 * </p>
 */
@Slf4j
@Component
public class CircuitBreakerFilter implements GlobalFilter, Ordered {

    private final CircuitBreakerService circuitBreakerService;
    private final DegradationServiceImpl degradationService;
    private final ObjectMapper objectMapper;

    public CircuitBreakerFilter(CircuitBreakerService circuitBreakerService,
                                DegradationServiceImpl degradationService,
                                ObjectMapper objectMapper) {
        this.circuitBreakerService = circuitBreakerService;
        this.degradationService = degradationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取目标服务的路由ID作为 serviceId
        Route route = exchange.getAttribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRoute");
        if (route == null) {
            return chain.filter(exchange);
        }

        String serviceId = route.getId();

        // 检查断路器是否允许请求通过
        if (!circuitBreakerService.allowRequest(serviceId)) {
            log.warn("断路器打开，拒绝请求: serviceId={}, path={}", serviceId, exchange.getRequest().getPath());
            return serviceUnavailable(exchange, serviceId);
        }

        // 放行请求，并在完成后根据状态码记录结果
        return chain.filter(exchange)
                .doOnSuccess(v -> {
                    HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
                    if (statusCode != null && (statusCode.is5xxServerError() || statusCode == HttpStatus.BAD_GATEWAY)) {
                        circuitBreakerService.recordFailure(serviceId);
                    } else {
                        circuitBreakerService.recordSuccess(serviceId);
                    }
                })
                .doOnError(e -> {
                    // 网络异常、超时等也记录为失败
                    circuitBreakerService.recordFailure(serviceId);
                    log.error("请求异常，记录断路器失败: serviceId={}, error={}", serviceId, e.getMessage());
                });
    }

    /**
     * 返回 503 服务不可用的降级响应
     * <p>
     * 优先使用 DegradationService 配置的自定义降级响应，
     * 没有配置时使用默认 503 降级响应。
     * </p>
     *
     * @param exchange  ServerWebExchange
     * @param serviceId 服务标识
     * @return Mono<Void>
     */
    private Mono<Void> serviceUnavailable(ServerWebExchange exchange, String serviceId) {
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> fallbackResponse = degradationService.getFallbackResponse(serviceId);
        String body;
        try {
            body = objectMapper.writeValueAsString(fallbackResponse);
        } catch (JsonProcessingException e) {
            log.error("序列化降级响应失败", e);
            body = String.format(
                    "{\"code\":503,\"message\":\"服务[%s]暂时不可用，请稍后重试\",\"data\":null,\"timestamp\":%d}",
                    serviceId, System.currentTimeMillis()
            );
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -90;
    }
}
