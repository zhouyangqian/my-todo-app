# Quick Start: API 网关与集成管理集成指南

**Feature**: API 网关与集成管理 (004-gateway)
**Date**: 2026-01-10
**Purpose**: 快速集成和使用 API 网关功能

## 概述

本指南帮助开发者快速集成 API 网关到多租户 SaaS 系统中。

## 前置条件

- Java 17+
- Spring Boot 3.0+
- Nacos Server
- Redis 7.0+
- Vue 3.0 + Ant Design 6.1.4

## 快速开始

### 1. 添加依赖

在 `gateway/pom.xml` 中添加：

```xml
<dependencies>
    <!-- Spring Cloud Gateway -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>

    <!-- Nacos Discovery -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        <version>2022.0.0.0.0</version>
    </dependency>

    <!-- Nacos Config -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        <version>2022.0.0.0.0</version>
    </dependency>

    <!-- Resilience4j -->
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-spring-boot2</artifactId>
        <version>1.7.1</version>
    </dependency>

    <!-- Caffeine Cache -->
    <dependency>
        <groupId>com.github.ben-manes.caffeine</groupId>
        <artifactId>caffeine</artifactId>
        <version>3.1.8</version>
    </dependency>

    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
    </dependency>

    <!-- Micrometer Prometheus -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.5</version>
    </dependency>

    <!-- MySQL Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <runtime>true</runtime>
    </dependency>
</dependencies>
```

### 2. 配置网关

在 `application.yml` 中配置：

```yaml
spring:
  application:
    name: gateway

  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: public
        group: DEFAULT_GROUP
      config:
        server-addr: localhost:8848
        file-extension: yaml

    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true

      routes:
        # 用户服务
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/v1/users/**
          filters:
            - StripPrefix=2

        # 权限服务
        - id: permission-service
          uri: lb://permission-service
          predicates:
            - Path=/api/v1/permissions/**
          filters:
            - StripPrefix=2

  redis:
    host: localhost
    port: 6379
    database: 0

  datasource:
    url: jdbc:mysql://localhost:3306/saas_db?useSSL=false
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  metrics:
    tags:
      application: ${spring.application.name}
```

### 3. 创建认证过滤器

```java
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private AuthClient authClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 验证 Token
        return authClient.validateToken(token.substring(7))
            .flatMap(response -> {
                if (response.isValid()) {
                    // 添加用户信息到请求头
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", String.valueOf(response.getUserId()))
                        .header("X-Tenant-Id", String.valueOf(response.getTenantId()))
                        .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest));
                } else {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            });
    }

    @Override
    public int getOrder() {
        return -100; // 最高优先级
    }
}
```

### 4. 配置限流过滤器

```java
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    @Autowired
    private RateLimitService rateLimitService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String apiPath = exchange.getRequest().getPath().value();

        // 检查限流
        if (!rateLimitService.allowRequest(userId, apiPath)) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().set("Retry-After", "60");
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -99;
    }
}
```

### 5. 配置熔断器

```java
@Configuration
public class CircuitBreakerConfig {

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory
            .configureDefault(id -> new Resilience4JConfigBuilder()
                .circuitBreakerConfig(Consumer -> {
                    Consumer
                        .slidingWindowSize(100)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(60))
                        .permittedNumberOfCallsInHalfOpenState(10)
                        .slowCallDurationThreshold(Duration.ofSeconds(5))
                        .build();
                })
                .build());
    }
}
```

### 6. 配置缓存

```java
@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, Object> responseCache() {
        return Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .recordStats()
            .build();
    }

    @Bean
    public CacheManager cacheManager() {
        return CaffeineCacheManager.newBuilder()
            .build();
    }
}
```

### 7. 前端 API 客户端

```javascript
// frontend/src/api/gateway.js
import request from '@/utils/request'

export function getRoutes(params) {
  return request({
    url: '/api/v1/gateway/routes',
    method: 'get',
    params
  })
}

export function createRoute(data) {
  return request({
    url: '/api/v1/gateway/routes',
    method: 'post',
    data
  })
}

export function getRateLimits(params) {
  return request({
    url: '/api/v1/gateway/rate-limits',
    method: 'get',
    params
  })
}

export function createRateLimit(data) {
  return request({
    url: '/api/v1/gateway/rate-limits',
    method: 'post',
    data
  })
}

export function getRealtimeMetrics() {
  return request({
    url: '/api/v1/gateway/metrics/realtime',
    method: 'get'
  })
}
```

### 8. 前端路由列表页面

```vue
<template>
  <a-card title="路由管理">
    <template #extra>
      <a-button type="primary" @click="showCreateModal">
        创建路由
      </a-button>
    </template>

    <a-table
      :columns="columns"
      :data-source="routes"
      :loading="loading"
      :pagination="pagination"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '启用' : '禁用' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click="editRoute(record)">编辑</a-button>
            <a-button size="small" danger @click="deleteRoute(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </a-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRoutes, createRoute } from '@/api/gateway'

const routes = ref([])
const loading = ref(false)

const columns = [
  { title: '路径', dataIndex: 'path', key: 'path' },
  { title: '目标服务', dataIndex: 'serviceId', key: 'serviceId' },
  { title: '超时(ms)', dataIndex: 'timeout', key: 'timeout' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action' }
]

const fetchRoutes = async () => {
  loading.value = true
  try {
    const { data } = await getRoutes()
    routes.value = data
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRoutes()
})
</script>
```

## 常见问题

### Q: 如何动态添加路由？

A: 通过 RouteDefinitionRepository 动态添加路由：

```java
@Service
public class RouteService {

    @Autowired
    private RouteDefinitionRepository repository;

    public void addRoute(String id, String path, String uri) {
        RouteDefinition definition = new RouteDefinition();
        definition.setId(id);
        definition.setPredicates(Collections.singletonList(
            new PathPredicate(path)
        ));
        definition.setUri(URI.create(uri));
        repository.save(Mono.just(definition)).subscribe();
    }
}
```

### Q: 如何配置不同服务的限流？

A: 创建不同的 RateLimitConfig，按服务维度限流：

```java
rateLimitService.createConfig(API, "user-service", 1000, 60);
rateLimitService.createConfig(API, "permission-service", 500, 60);
```

### Q: 如何实现灰度发布？

A: 配置 CanaryStrategy，指定流量比例：

```java
canaryService.createStrategy("user-service", "v2", 10); // 10%流量到v2
```

### Q: 监控数据如何查询？

A: 通过 Prometheus 查询 API，或使用 Grafana 可视化：

```java
// 查询实时指标
meterRegistry.get("gateway.requests.total").counter().count();
```

## 下一步

- 查看完整 API 文档: `contracts/`
- 了解数据模型: `data-model.md`
- 查看技术选型: `research.md`
