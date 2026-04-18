package com.example.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 网关全局异常处理器
 * <p>
 * 捕获网关层面（路由、过滤器等）产生的异常，统一转换为 JSON 格式的错误响应。
 * 处理 ResponseStatusException 和其他未知异常。
 * </p>
 */
@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    /** JSON 序列化工具 */
    private final ObjectMapper objectMapper;

    /**
     * 处理异常，返回统一的错误响应
     *
     * @param exchange ServerWebExchange 对象
     * @param ex       捕获到的异常
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // 如果响应已提交（已发送给客户端），直接返回异常
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 默认500内部服务器错误
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "服务器内部错误";

        // 处理 Spring WebFlux 的响应状态异常（如404、503等）
        if (ex instanceof ResponseStatusException) {
            status = ((ResponseStatusException) ex).getStatus();
            message = ((ResponseStatusException) ex).getReason();
        }

        log.error("网关异常: {}", ex.getMessage(), ex);

        // 设置响应状态码和内容类型
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 构造统一的错误响应 JSON
        Map<String, Object> result = new HashMap<>();
        result.put("code", status.value());
        result.put("message", message);
        result.put("data", null);
        result.put("timestamp", System.currentTimeMillis());

        try {
            String body = objectMapper.writeValueAsString(result);
            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("写入错误响应失败", e);
            return Mono.error(e);
        }
    }
}
