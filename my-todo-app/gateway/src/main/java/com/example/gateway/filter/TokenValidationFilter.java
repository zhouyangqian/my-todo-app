package com.example.gateway.filter;

import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Token 验证全局过滤器
 * <p>
 * 对经过网关的请求进行 JWT 令牌验证（认证相关端点除外）。
 * 验证通过后，将用户ID、用户名、租户ID等信息解析并注入请求头，
 * 传递给下游微服务使用。
 * </p>
 */
@Slf4j
@Component
public class TokenValidationFilter implements GlobalFilter, Ordered {

    /** JWT 签名密钥，从配置文件读取 */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /** Authorization 请求头名称 */
    private static final String AUTHORIZATION_HEADER = "Authorization";
    /** Bearer 令牌前缀 */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 过滤器核心逻辑
     * <p>
     * 1. 认证相关端点（/api/auth/）跳过验证
     * 2. 检查 Authorization 请求头是否存在
     * 3. 解析并验证 JWT 令牌
     * 4. 将用户信息注入请求头，传递给下游服务
     * </p>
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 认证相关端点和健康检查端点跳过 Token 验证
        if (path.startsWith("/api/auth/") || path.equals("/actuator/health")) {
            return chain.filter(exchange);
        }

        // 获取 Authorization 请求头
        String authorization = request.getHeaders().getFirst(AUTHORIZATION_HEADER);

        // 检查请求头是否有效
        if (StrUtil.isBlank(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, "缺少或无效的 Authorization 请求头");
        }

        // 提取 Bearer 后面的 Token 字符串
        String token = authorization.substring(BEARER_PREFIX.length());

        try {
            // 解析 Token，获取声明信息
            Claims claims = parseToken(token);

            // 将用户信息注入请求头，传递给下游微服务
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(claims.get("userId")))      // 用户ID
                    .header("X-Username", String.valueOf(claims.get("username")))    // 用户名
                    .header("X-Tenant-Id", String.valueOf(claims.get("tenantId")))   // 租户ID
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (ExpiredJwtException e) {
            log.warn("令牌已过期: {}", e.getMessage());
            return unauthorized(exchange, "令牌已过期");
        } catch (Exception e) {
            log.warn("无效令牌: {}", e.getMessage());
            return unauthorized(exchange, "无效的令牌");
        }
    }

    /**
     * 解析 JWT 令牌
     *
     * @param token JWT 令牌字符串
     * @return Claims 声明数据
     */
    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 返回 401 未授权响应
     * <p>构造统一的 JSON 错误响应体</p>
     *
     * @param exchange ServerWebExchange 对象
     * @param message  错误信息
     * @return Mono<Void>
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 构造统一的错误响应 JSON
        String body = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}",
                message, System.currentTimeMillis());

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 过滤器优先级，在 RequestLogFilter 之后执行
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
