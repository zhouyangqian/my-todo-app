package com.example.gateway.filter;

import com.example.gateway.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 响应缓存全局过滤器
 * <p>
 * 仅缓存 GET 请求，跳过认证相关路径。
 * 缓存命中直接返回，未命中则放行并在响应完成后缓存结果。
 * 使用 ServerHttpResponseDecorator 拦截响应体。
 * 仅在 Redis 可用时生效。
 * </p>
 */
@Slf4j
@Component
public class CacheFilter implements GlobalFilter, Ordered {

    private final CacheService cacheService;

    public CacheFilter(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 仅缓存 GET 请求
        if (exchange.getRequest().getMethod() != HttpMethod.GET) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getPath().value();

        // 跳过认证相关路径和网关内部路径
        if (isSkipPath(path)) {
            return chain.filter(exchange);
        }

        String cacheKey = cacheService.buildCacheKey(exchange.getRequest());

        // 先查缓存是否存在，再决定读取或放行
        return cacheService.get(cacheKey)
                .flatMap(cachedBody -> {
                    // 缓存命中，直接返回
                    log.debug("缓存命中，直接返回: {}", path);
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.OK);
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    response.getHeaders().add("X-Cache", "HIT");
                    DataBuffer buffer = response.bufferFactory()
                            .wrap(cachedBody.getBytes(StandardCharsets.UTF_8));
                    return response.writeWith(Mono.just(buffer));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // 缓存未命中，放行请求并拦截响应体
                    log.debug("缓存未命中，放行请求: {}", path);
                    return chain.filter(exchange.mutate().response(
                            new CacheResponseDecorator(exchange.getResponse(), cacheKey)).build());
                }));
    }

    /**
     * 响应体拦截装饰器
     * <p>
     * 拦截响应体，仅缓存成功的 JSON 响应。
     * </p>
     */
    private class CacheResponseDecorator extends ServerHttpResponseDecorator {

        private final String cacheKey;

        CacheResponseDecorator(ServerHttpResponse delegate, String cacheKey) {
            super(delegate);
            this.cacheKey = cacheKey;
        }

        @Override
        public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
            if (body instanceof Flux) {
                Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                return super.writeWith(fluxBody.collectList()
                        .flatMap(dataBuffers -> {
                            // 合并所有DataBuffer
                            StringBuilder builder = new StringBuilder();
                            for (DataBuffer dataBuffer : dataBuffers) {
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);
                                builder.append(new String(content, StandardCharsets.UTF_8));
                            }
                            String responseBody = builder.toString();

                            // 仅缓存成功的JSON响应
                            HttpStatusCode status = getStatusCode();
                            if (status == HttpStatus.OK) {
                                String contentType = getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
                                if (contentType != null && contentType.contains("application/json")) {
                                    cacheService.put(cacheKey, responseBody,
                                            CacheService.getDefaultTtlSeconds()).subscribe();
                                    getHeaders().add("X-Cache", "MISS");
                                }
                            }

                            // 将响应体写回客户端
                            DataBuffer wrappedBuffer = bufferFactory()
                                    .wrap(responseBody.getBytes(StandardCharsets.UTF_8));
                            return Mono.just(wrappedBuffer);
                        }));
            }
            return super.writeWith(body);
        }
    }

    /**
     * 判断路径是否需要跳过缓存
     */
    private boolean isSkipPath(String path) {
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/gateway/")
                || path.startsWith("/gateway/")
                || path.startsWith("/actuator/");
    }

    @Override
    public int getOrder() {
        return -80;
    }
}
