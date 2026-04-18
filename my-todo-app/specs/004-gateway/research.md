# Research: API 网关与集成管理技术调研

**Feature**: API 网关与集成管理 (004-gateway)
**Date**: 2026-01-10
**Purpose**: 技术选型和研究决策记录

## 概述

本文档记录 API 网关与集成管理模块的技术研究和选型决策。

## 技术选型

### 1. Spring Cloud Gateway 路由配置

**选择**: Spring Cloud Gateway + Spring Cloud Loadbalancer

**理由**:
- Spring Cloud Gateway 是 Spring 官方推荐的网关解决方案
- 基于 WebFlux 非阻塞 I/O，性能优异
- 与 Spring Boot 生态完美集成
- 支持动态路由配置
- 内置负载均衡支持

**关键配置**:

```java
@Configuration
public class GatewayConfig {

    @Bean
    public RouteDefinitionLocator routeDefinitionLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r
                .path("/api/v1/users/**")
                .filters(f -> f
                    .stripPrefix(2)
                    .filter(authenticationFilter)
                    .filter(rateLimitFilter)
                )
                .uri("lb://user-service"))
            .route("permission-service", r -> r
                .path("/api/v1/permissions/**")
                .filters(f -> f.stripPrefix(2))
                .uri("lb://permission-service"))
            .build();
    }
}
```

**动态路由**:

```java
@Service
public class RouteService {

    @Autowired
    private RouteDefinitionRepository routeRepository;

    public void addRoute(String id, String path, String uri) {
        RouteDefinition definition = new RouteDefinition();
        definition.setId(id);
        definition.setPredicates(Collections.singletonList(
            new PathPredicate(path)
        ));
        definition.setUri(URI.create(uri));
        routeRepository.save(Mono.just(definition)).subscribe();
    }
}
```

### 2. 限流算法实现

**选择**: 令牌桶算法（内部API）+ 滑动窗口算法（对外API）

**理由**:
- 令牌桶算法支持突发流量，适合内部API限流
- 滑动窗口算法精度高，适合对外API的精确限流
- Redis 存储计数状态，支持分布式部署
- 性能优秀，限流判断响应 <10ms

**令牌桶实现**:

```java
@Component
public class RateLimitService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String TOKEN_BUCKET_PREFIX = "ratelimit:bucket:";

    /**
     * 令牌桶算法限流
     * @param key 限流维度标识（用户ID/租户ID/API/IP）
     * @param capacity 桶容量
     * @param refillRate 令牌补充速率（令牌/秒）
     * @return 是否允许请求
     */
    public boolean allowRequest(String key, long capacity, double refillRate) {
        String redisKey = TOKEN_BUCKET_PREFIX + key;

        // 获取当前令牌数和时间戳
        String script =
            "local tokens = tonumber(redis.call('get', KEYS[1])) or 0\n" +
            "local lastRefill = tonumber(redis.call('get', KEYS[2])) or 0\n" +
            "local now = ARGV[1]\n" +
            "local capacity = ARGV[2]\n" +
            "local refillRate = ARGV[3]\n" +
            "local interval = math.min(now - lastRefill, 10)\n" + // 最多补充10秒
            "local newTokens = math.floor(interval * refillRate)\n" +
            "tokens = math.min(tokens + newTokens, capacity)\n" +
            "if tokens >= 1 then\n" +
            "  tokens = tokens - 1\n" +
            "  redis.call('set', KEYS[1], tokens)\n" +
            "  redis.call('set', KEYS[2], now)\n" +
            "  redis.call('expire', KEYS[1], 3600)\n" +
            "  redis.call('expire', KEYS[2], 3600)\n" +
            "  return 1\n" +
            "else\n" +
            "  redis.call('set', KEYS[1], tokens)\n" +
            "  redis.call('set', KEYS[2], now)\n" +
            "  redis.call('expire', KEYS[1], 3600)\n" +
            "  redis.call('expire', KEYS[2], 3600)\n" +
            "  return 0\n" +
            "end";

        Long result = redisTemplate.execute(
            RedisScript.of(script, Long.class),
            Arrays.asList(redisKey + ":tokens", redisKey + ":time"),
            String.valueOf(System.currentTimeMillis() / 1000),
            String.valueOf(capacity),
            String.valueOf(refillRate)
        );

        return result != null && result == 1;
    }
}
```

**滑动窗口实现**:

```java
/**
 * 滑动窗口算法限流（精确控制）
 */
public boolean allowRequestSlidingWindow(String key, int limit, int windowSeconds) {
    String redisKey = "ratelimit:window:" + key;
    long now = System.currentTimeMillis();
    long windowStart = now - windowSeconds * 1000;

    // 使用 ZSET 存储请求时间戳，分数为时间戳
    // 移除窗口外的记录
    redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);

    // 统计窗口内请求数
    Long count = redisTemplate.opsForZSet().count(redisKey, windowStart, now);

    if (count < limit) {
        // 添加当前请求
        redisTemplate.opsForZSet().add(redisKey, UUID.randomUUID().toString(), now);
        redisTemplate.expire(redisKey, windowSeconds + 1, TimeUnit.SECONDS);
        return true;
    }

    return false;
}
```

### 3. 服务发现与健康检查

**选择**: Nacos Discovery + 自定义健康检查

**理由**:
- Nacos 提供服务注册和发现功能
- 支持主动健康检查
- 与 Spring Cloud 集成良好
- 支持配置管理
- 社区活跃，文档完善

**服务发现配置**:

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: public
        group: DEFAULT_GROUP
        register-enabled: true
```

**自定义健康检查**:

```java
@Component
public class HealthCheckService {

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Scheduled(fixedRate = 10000) // 每10秒检查一次
    public void checkServices() {
        // 获取所有服务实例
        List<ServiceInstance> instances = loadBalancerClient.getInstances("user-service");

        for (ServiceInstance instance : instances) {
            boolean healthy = checkInstance(instance);
            updateInstanceStatus(instance, healthy);
        }
    }

    private boolean checkInstance(ServiceInstance instance) {
        try {
            // 发送健康检查请求
            RestTemplate restTemplate = new RestTemplate();
            String healthUrl = instance.getUri() + "/actuator/health";
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    private void updateInstanceStatus(ServiceInstance instance, boolean healthy) {
        // 更新实例状态到本地缓存
        // Nacos 会自动摘除不健康的实例
    }
}
```

### 4. 熔断器设计

**选择**: Resilience4j Circuit Breaker

**理由**:
- Resilience4j 是 Spring Cloud 推荐的熔断器
- 轻量级，性能优秀
- 支持熔断、限流、重试、隔离
- 配置灵活，支持动态调整

**熔断器配置**:

```java
@Configuration
public class CircuitBreakerConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .slidingWindowType(SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(100) // 滑动窗口大小
            .failureRateThreshold(50) // 失败率阈值50%
            .waitDurationInOpenState(Duration.ofSeconds(60)) // 熔断60秒
            .permittedNumberOfCallsInHalfOpenState(10) // 半开状态允许10次调用
            .recordException(e -> true) // 记录所有异常
            .build();

        return CircuitBreakerRegistry.of(
            CircuitBreakerConfig.builder()
                .addCircuitBreakerConfig("user-service", config)
                .addCircuitBreakerConfig("permission-service", config)
                .build()
        );
    }
}
```

**熔断器过滤器**:

```java
@Component
public class CircuitBreakerFilter implements GatewayFilter {

    @Autowired
    private CircuitBreakerRegistry registry;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String serviceId = extractServiceId(exchange);

        CircuitBreaker circuitBreaker = registry.circuitBreaker(serviceId);
        CircuitBreakerMono circuitBreakerMono = CircuitBreakerMono.of(circuitBreaker);

        return circuitBreakerMono.run(
            chain.filter(exchange),
            throwable -> {
                // 熔断器打开，返回降级响应
                return handleFallback(exchange, throwable);
            }
        );
    }
}
```

### 5. 缓存策略

**选择**: Caffeine（本地缓存）+ Redis（分布式缓存）

**理由**:
- Caffeine 性能优秀，命中率高
- 支持基于大小的淘汰策略
- 支持异步加载
- Redis 用于分布式场景，支持缓存共享

**Caffeine 配置**:

```java
@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, Object> responseCache() {
        return Caffeine.newBuilder()
            .maximumSize(10000) // 最多10000个缓存条目
            .expireAfterWrite(60, TimeUnit.SECONDS) // 写入60秒后过期
            .recordStats() // 记录统计信息
            .build();
    }
}
```

**缓存服务**:

```java
@Service
public class CacheService {

    @Autowired
    private Cache<String, Object> localCache;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 获取缓存（本地 + Redis 二级缓存）
     */
    public Object get(String key) {
        // 先查本地缓存
        Object value = localCache.getIfPresent(key);
        if (value != null) {
            return value;
        }

        // 再查 Redis
        String redisValue = redisTemplate.opsForValue().get("cache:" + key);
        if (redisValue != null) {
            value = deserialize(redisValue);
            localCache.put(key, value); // 回填本地缓存
            return value;
        }

        return null;
    }

    /**
     * 设置缓存
     */
    public void put(String key, Object value, long ttl, TimeUnit unit) {
        localCache.put(key, value);
        redisTemplate.opsForValue().set("cache:" + key, serialize(value), ttl, unit);
    }

    /**
     * 清除缓存
     */
    public void evict(String key) {
        localCache.invalidate(key);
        redisTemplate.delete("cache:" + key);
    }
}
```

### 6. 灰度发布实现

**选择**: 基于用户ID哈希 + 请求头匹配

**理由**:
- 用户ID哈希确保一致性路由
- 请求头匹配支持内部测试
- 支持快速回滚
- 支持按比例平滑切换

**灰度策略实现**:

```java
@Service
public class CanaryService {

    @Autowired
    private CanaryStrategyRepository strategyRepository;

    /**
     * 判断请求应该路由到哪个版本
     */
    public String determineVersion(String serviceId, String userId, HttpHeaders headers) {
        List<CanaryStrategy> strategies = strategyRepository.findByServiceId(serviceId);

        for (CanaryStrategy strategy : strategies) {
            if (matchStrategy(strategy, userId, headers)) {
                return strategy.getTargetVersion();
            }
        }

        return "default"; // 默认版本
    }

    private boolean matchStrategy(CanaryStrategy strategy, String userId, HttpHeaders headers) {
        // 检查请求头匹配
        if (strategy.getHeaderName() != null) {
            String headerValue = headers.getFirst(strategy.getHeaderName());
            if (strategy.getHeaderValue().equals(headerValue)) {
                return true;
            }
        }

        // 检查用户ID匹配
        if (strategy.getUserIds() != null && !strategy.getUserIds().isEmpty()) {
            if (strategy.getUserIds().contains(userId)) {
                return true;
            }
        }

        // 检查流量比例
        if (strategy.getTrafficPercentage() > 0) {
            int hash = Math.abs(userId.hashCode()) % 100;
            return hash < strategy.getTrafficPercentage();
        }

        return false;
    }
}
```

### 7. 监控指标收集

**选择**: Micrometer + Prometheus + Grafana

**理由**:
- Micrometer 是 Spring 官方推荐的监控门面
- 支持多种监控系统（Prometheus、InfluxDB等）
- Prometheus 强大的数据查询和告警能力
- Grafana 丰富的可视化图表

**指标定义**:

```java
@Component
public class MetricsCollector {

    private final Counter requestCounter;
    private final Timer responseTimer;
    private final Gauge activeConnections;

    public MetricsCollector(MeterRegistry registry) {
        // 请求计数器
        this.requestCounter = Counter.builder("gateway.requests.total")
            .tag("service", "all")
            .description("Total number of requests")
            .register(registry);

        // 响应时间
        this.responseTimer = Timer.builder("gateway.response.time")
            .description("Response time")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry);

        // 活跃连接数
        this.activeConnections = Gauge.builder("gateway.active.connections")
            .description("Active connections")
            .register(registry, this, MetricsCollector::getActiveConnections);
    }

    public void recordRequest(String service, String path, int status) {
        requestCounter.tags("service", service, "path", path, "status", String.valueOf(status))
            .increment();
    }

    public void recordResponseTime(long milliseconds) {
        responseTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }
}
```

**Prometheus 配置**:

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
    scrape_interval: 15s
```

## 性能优化

### 1. 网关性能优化

- 使用 WebFlux 非阻塞 I/O
- 连接池配置优化
- 响应压缩（Gzip）
- 本地缓存减少后端调用

### 2. 限流性能优化

- Redis Pipeline 批量操作
- 本地缓存限流状态减少 Redis 访问
- Lua 脚本原子操作

### 3. 缓存性能优化

- 二级缓存（本地 + Redis）
- 异步刷新缓存
- 预热热点数据

### 4. 监控性能优化

- 采样率控制（如10%采样）
- 异步上报指标
- 本地聚合减少网络传输

## 安全考虑

### 1. Token 验证

- JWT Token 签名验证
- Token 黑名单检查
- Token 过期检查

### 2. 权限验证

- 基于 Token 中的权限列表
- API 级别的权限控制
- 租户隔离

### 3. 第三方接口

- 认证信息加密存储
- 请求/响应日志脱敏
- 接口白名单机制

### 4. 配置管理

- 配置变更审计
- 权限控制
- 敏感信息加密

## 依赖配置

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
        <artifactId>spring-boot-starter-data-redis</artifactId>
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

## 下一步

- 查看完整数据模型: `data-model.md`
- 查看快速开始指南: `quickstart.md`
- 查看 API 契约: `contracts/`
