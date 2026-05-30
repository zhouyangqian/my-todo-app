package com.example.gateway.filter;

import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
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
     * 初始化时验证 JWT 配置
     */
    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            log.error("JWT Secret 未配置！");
        } else if (jwtSecret.length() < 32) {
            log.warn("JWT Secret 长度不足 32 字符，当前长度: {}", jwtSecret.length());
        } else {
            log.info("JWT 配置加载成功，Secret 长度: {}，前缀: {}", jwtSecret.length(), jwtSecret.substring(0, 5) + "...");
        }
    }

    /**
     * 过滤器核心逻辑
     * <p>
     * 1. 公开端点（登录、注册、刷新令牌、健康检查）跳过验证
     * 2. 检查 Authorization 请求头是否存在
     * 3. 解析并验证 JWT 令牌
     * 4. 将用户信息注入请求头，传递给下游服务
     * </p>
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        log.debug("处理请求路径: {}", path);

        // 公开端点跳过 Token 验证（只跳过明确的公开端点，而不是整个 /api/auth/ 路径）
        if (isPublicEndpoint(path)) {
            log.debug("跳过 Token 验证: {}", path);
            return chain.filter(exchange);
        }

        // 获取 Authorization 请求头
        String authorization = request.getHeaders().getFirst(AUTHORIZATION_HEADER);
        log.debug("Authorization 请求头: {}", authorization != null ? "存在 (" + authorization.substring(0, Math.min(20, authorization.length())) + "...)" : "不存在");

        // 检查请求头是否有效
        if (StrUtil.isBlank(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            log.warn("缺少或无效的 Authorization 请求头");
            return unauthorized(exchange, "缺少或无效的 Authorization 请求头");
        }

        // 提取 Bearer 后面的 Token 字符串
        String token = authorization.substring(BEARER_PREFIX.length());
        log.debug("Token 长度: {}", token.length());

        try {
            // 解析 Token，获取声明信息
            Claims claims = parseToken(token);
            log.debug("Token 解析成功 - userId: {}, username: {}, tenantId: {}",
                claims.get("userId"), claims.get("username"), claims.get("tenantId"));

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
            log.error("无效令牌: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return unauthorized(exchange, "无效的令牌");
        }
    }

    /**
     * 判断是否为公开端点
     * <p>
     * 只有明确列出的公开端点才跳过 Token 验证，其他所有请求都需要认证。
     * 这比之前按路径前缀判断更安全、更精确。
     * </p>
     *
     * @param path 请求路径
     * @return true-公开端点，false-需要认证
     */
    private boolean isPublicEndpoint(String path) {
        // 认证相关的公开端点（登录、注册、刷新令牌、健康检查、网关监控）
        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/refresh")
                || path.equals("/api/auth/captcha")
                || path.equals("/api/auth/health")
                || path.startsWith("/api/auth/sse/")
                || path.equals("/actuator/health")
                || path.equals("/actuator/info")
                || path.equals("/actuator/gateway-health")
                || path.equals("/actuator/gateway-status")
                || path.startsWith("/gateway/")
                || path.startsWith("/api/gateway/");
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
