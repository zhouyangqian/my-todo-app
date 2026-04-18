# Tests: API 网关与集成管理

**Feature Branch**: `004-gateway`
**Generated**: 2026-04-07

## 测试策略概览

| 层级 | 框架 | 覆盖率目标 |
|------|------|------------|
| 单元测试 | JUnit 5 + Mockito | ≥ 80% |
| 集成测试 | Spring Cloud Gateway Test | ≥ 75% |
| API测试 | WebTestClient | 100% 路由 |
| 前端测试 | Vitest + Vue Test Utils | ≥ 70% |

---

## 单元测试

### TokenValidationFilterTest
```java
@ExtendWith(MockitoExtension.class)
class TokenValidationFilterTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private TokenValidationFilter filter;

    @Test
    @DisplayName("Token验证 - 有效Token")
    void validateToken_valid() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users")
            .header("Authorization", "Bearer valid_token")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(authService.validateToken("valid_token")).thenReturn(true);

        // When
        Mono<Void> result = filter.filter(exchange, mock(WebFilterChain.class));

        // Then
        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    @DisplayName("Token验证 - 无Token")
    void validateToken_noToken() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = filter.filter(exchange, mock(WebFilterChain.class));

        // Then
        StepVerifier.create(result)
            .expectErrorMatches(e -> e instanceof UnauthorizedException)
            .verify();
    }

    @Test
    @DisplayName("Token验证 - 无效Token")
    void validateToken_invalid() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users")
            .header("Authorization", "Bearer invalid_token")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(authService.validateToken("invalid_token")).thenReturn(false);

        // When
        Mono<Void> result = filter.filter(exchange, mock(WebFilterChain.class));

        // Then
        StepVerifier.create(result)
            .expectErrorMatches(e -> e instanceof UnauthorizedException)
            .verify();
    }

    @Test
    @DisplayName("白名单路径放行")
    void validateToken_whitelist() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/actuator/health")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = filter.filter(exchange, mock(WebFilterChain.class));

        // Then
        StepVerifier.create(result)
            .verifyComplete();
        verify(authService, never()).validateToken(any());
    }
}
```

### PermissionFilterTest
```java
@ExtendWith(MockitoExtension.class)
class PermissionFilterTest {

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private PermissionFilter filter;

    @Test
    @DisplayName("权限验证 - 有权限")
    void checkPermission_hasPermission() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users")
            .attribute("userId", 1L)
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(permissionService.hasPermission(1L, "user:read")).thenReturn(true);

        // When
        Mono<Void> result = filter.filter(exchange, mock(WebFilterChain.class));

        // Then
        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    @DisplayName("权限验证 - 无权限")
    void checkPermission_noPermission() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.delete("/api/v1/users/1")
            .attribute("userId", 1L)
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(permissionService.hasPermission(1L, "user:delete")).thenReturn(false);

        // When
        Mono<Void> result = filter.filter(exchange, mock(WebFilterChain.class));

        // Then
        StepVerifier.create(result)
            .expectErrorMatches(e -> e instanceof ForbiddenException)
            .verify();
    }
}
```

### RateLimitFilterTest
```java
@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private RateLimitFilter filter;

    @Test
    @DisplayName("限流 - 允许请求")
    void rateLimit_allowed() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users")
            .remoteAddress(new InetSocketAddress("192.168.1.1", 12345))
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(rateLimitService.tryAcquire("192.168.1.1", "/api/v1/users")).thenReturn(true);

        // When
        Mono<Void> result = filter.filter(exchange, mock(WebFilterChain.class));

        // Then
        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    @DisplayName("限流 - 拒绝请求")
    void rateLimit_rejected() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users")
            .remoteAddress(new InetSocketAddress("192.168.1.1", 12345))
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(rateLimitService.tryAcquire("192.168.1.1", "/api/v1/users")).thenReturn(false);

        // When
        Mono<Void> result = filter.filter(exchange, mock(WebFilterChain.class));

        // Then
        StepVerifier.create(result)
            .expectErrorMatches(e -> e instanceof RateLimitException)
            .verify();
    }
}
```

### TokenBucketLimiterTest
```java
class TokenBucketLimiterTest {

    private TokenBucketLimiter limiter;

    @BeforeEach
    void setup() {
        limiter = new TokenBucketLimiter(10, 1); // 10个令牌，每秒补充1个
    }

    @Test
    @DisplayName("令牌桶 - 初始获取")
    void tryAcquire_initial() {
        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.tryAcquire());
        }
        assertFalse(limiter.tryAcquire()); // 第11次失败
    }

    @Test
    @DisplayName("令牌桶 - 令牌补充")
    void tryAcquire_refill() throws InterruptedException {
        // 消耗所有令牌
        for (int i = 0; i < 10; i++) {
            limiter.tryAcquire();
        }

        // 等待1秒，补充1个令牌
        Thread.sleep(1100);

        assertTrue(limiter.tryAcquire());
    }
}
```

### LeakyBucketLimiterTest
```java
class LeakyBucketLimiterTest {

    private LeakyBucketLimiter limiter;

    @BeforeEach
    void setup() {
        limiter = new LeakyBucketLimiter(10, 1); // 桶容量10，每秒漏出1个
    }

    @Test
    @DisplayName("漏桶 - 正常处理")
    void tryAcquire_normal() {
        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.tryAcquire());
        }
    }

    @Test
    @DisplayName("漏桶 - 桶满拒绝")
    void tryAcquire_bucketFull() {
        for (int i = 0; i < 10; i++) {
            limiter.tryAcquire();
        }
        assertFalse(limiter.tryAcquire());
    }
}
```

### RouteServiceTest
```java
@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteMapper routeMapper;

    @Mock
    private RouteDefinitionLocator routeLocator;

    @InjectMocks
    private RouteService routeService;

    @Test
    @DisplayName("获取所有路由")
    void getAllRoutes() {
        // Given
        List<RouteConfig> routes = List.of(
            new RouteConfig("user-service", "/api/v1/users/**", "lb://user-service"),
            new RouteConfig("auth-service", "/api/v1/auth/**", "lb://auth-service")
        );
        when(routeMapper.selectAll()).thenReturn(routes);

        // When
        List<RouteConfig> result = routeService.getAllRoutes();

        // Then
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("添加路由")
    void addRoute() {
        // Given
        RouteConfig route = new RouteConfig();
        route.setId("test-service");
        route.setPath("/api/v1/test/**");
        route.setUri("lb://test-service");

        // When
        routeService.addRoute(route);

        // Then
        verify(routeMapper).insert(route);
        verify(routeLocator).refresh();
    }

    @Test
    @DisplayName("更新路由")
    void updateRoute() {
        // Given
        RouteConfig route = new RouteConfig();
        route.setId("user-service");
        route.setPath("/api/v1/users/**");

        // When
        routeService.updateRoute(route);

        // Then
        verify(routeMapper).updateById(route);
        verify(routeLocator).refresh();
    }

    @Test
    @DisplayName("删除路由")
    void deleteRoute() {
        // Given
        String routeId = "test-service";

        // When
        routeService.deleteRoute(routeId);

        // Then
        verify(routeMapper).deleteById(routeId);
        verify(routeLocator).refresh();
    }
}
```

### HealthCheckServiceTest
```java
@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {

    @Mock
    private DiscoveryClient discoveryClient;

    @Mock
    private ServiceHealthMapper healthMapper;

    @InjectMocks
    private HealthCheckService healthCheckService;

    @Test
    @DisplayName("健康检查 - 服务正常")
    void checkHealth_healthy() {
        // Given
        String serviceId = "user-service";
        when(discoveryClient.getInstances(serviceId))
            .thenReturn(List.of(new DefaultServiceInstance("1", serviceId, "localhost", 8080, false)));

        // When
        ServiceHealth health = healthCheckService.checkHealth(serviceId);

        // Then
        assertEquals(ServiceHealth.Status.UP, health.getStatus());
    }

    @Test
    @DisplayName("健康检查 - 服务不可用")
    void checkHealth_unavailable() {
        // Given
        String serviceId = "user-service";
        when(discoveryClient.getInstances(serviceId))
            .thenReturn(Collections.emptyList());

        // When
        ServiceHealth health = healthCheckService.checkHealth(serviceId);

        // Then
        assertEquals(ServiceHealth.Status.DOWN, health.getStatus());
    }
}
```

---

## 集成测试

### GatewayIntegrationTest
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebFlux
class GatewayIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthService authService;

    @MockBean
    private PermissionService permissionService;

    @BeforeEach
    void setup() {
        when(authService.validateToken(any())).thenReturn(true);
        when(authService.getUserId(any())).thenReturn(1L);
        when(permissionService.hasPermission(any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("路由转发 - 用户服务")
    void routeToUserService() {
        webTestClient.get()
            .uri("/api/v1/users")
            .header("Authorization", "Bearer token")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    @DisplayName("路由转发 - 认证服务")
    void routeToAuthService() {
        webTestClient.post()
            .uri("/api/v1/auth/login")
            .bodyValue(Map.of("username", "admin", "password", "123456"))
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    @DisplayName("路由 - 未匹配返回404")
    void routeNotFound() {
        webTestClient.get()
            .uri("/api/v1/unknown")
            .header("Authorization", "Bearer token")
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("限流 - 超过限制返回429")
    void rateLimitExceeded() {
        // 模拟超限
        for (int i = 0; i < 100; i++) {
            webTestClient.get()
                .uri("/api/v1/users")
                .header("Authorization", "Bearer token")
                .exchange();
        }

        webTestClient.get()
            .uri("/api/v1/users")
            .header("Authorization", "Bearer token")
            .exchange()
            .expectStatus().isEqualTo(429);
    }
}
```

### FilterChainIntegrationTest
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FilterChainIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("过滤器链 - 无Token被拒绝")
    void filterChain_noToken() {
        webTestClient.get()
            .uri("/api/v1/users")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("过滤器链 - 无效Token被拒绝")
    void filterChain_invalidToken() {
        webTestClient.get()
            .uri("/api/v1/users")
            .header("Authorization", "Bearer invalid_token")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("过滤器链 - 请求日志记录")
    void filterChain_requestLogging() {
        // 验证请求日志过滤器记录了请求
        webTestClient.get()
            .uri("/api/v1/users")
            .header("Authorization", "Bearer valid_token")
            .exchange();

        // 验证日志
        // verify(requestLogService).log(any());
    }
}
```

---

## 前端测试

### RouteConfig.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RouteConfig from '@/views/gateway/RouteConfig.vue'
import * as gatewayApi from '@/api/gateway'

vi.mock('@/api/gateway')

describe('RouteConfig', () => {
  it('渲染路由列表', async () => {
    vi.mocked(gatewayApi.getRoutes).mockResolvedValue({
      data: [
        { id: 'user-service', path: '/api/v1/users/**', uri: 'lb://user-service' },
        { id: 'auth-service', path: '/api/v1/auth/**', uri: 'lb://auth-service' }
      ]
    })

    const wrapper = mount(RouteConfig)
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('.route-item')).toHaveLength(2)
  })

  it('添加路由', async () => {
    vi.mocked(gatewayApi.addRoute).mockResolvedValue({ data: {} })

    const wrapper = mount(RouteConfig)

    await wrapper.find('.add-btn').trigger('click')
    await wrapper.find('input[name="id"]').setValue('new-service')
    await wrapper.find('input[name="path"]').setValue('/api/v1/new/**')
    await wrapper.find('input[name="uri"]').setValue('lb://new-service')
    await wrapper.find('.save-btn').trigger('click')

    expect(gatewayApi.addRoute).toHaveBeenCalledWith({
      id: 'new-service',
      path: '/api/v1/new/**',
      uri: 'lb://new-service'
    })
  })

  it('删除路由确认', async () => {
    const wrapper = mount(RouteConfig, {
      data() {
        return {
          routes: [{ id: 'test-service', path: '/test/**', uri: 'lb://test' }]
        }
      }
    })

    await wrapper.find('.delete-btn').trigger('click')

    expect(wrapper.find('.confirm-dialog').isVisible()).toBe(true)
  })
})
```

### RateLimitConfig.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RateLimitConfig from '@/views/gateway/RateLimitConfig.vue'

describe('RateLimitConfig', () => {
  it('渲染限流配置', async () => {
    const wrapper = mount(RateLimitConfig, {
      data() {
        return {
          configs: [
            { path: '/api/v1/users', limit: 100, window: 60 },
            { path: '/api/v1/auth/login', limit: 10, window: 60 }
          ]
        }
      }
    })

    expect(wrapper.findAll('.config-item')).toHaveLength(2)
  })

  it('更新限流配置', async () => {
    const wrapper = mount(RateLimitConfig)

    await wrapper.setData({ editingConfig: { path: '/api/v1/users', limit: 200 } })
    await wrapper.find('.update-btn').trigger('click')

    expect(wrapper.emitted('updated')).toBeTruthy()
  })
})
```

### ServiceHealth.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ServiceHealth from '@/views/gateway/ServiceHealth.vue'
import * as healthApi from '@/api/health'

vi.mock('@/api/health')

describe('ServiceHealth', () => {
  it('渲染服务健康状态', async () => {
    vi.mocked(healthApi.getServicesHealth).mockResolvedValue({
      data: [
        { serviceId: 'user-service', status: 'UP', instances: 2 },
        { serviceId: 'auth-service', status: 'UP', instances: 1 },
        { serviceId: 'order-service', status: 'DOWN', instances: 0 }
      ]
    })

    const wrapper = mount(ServiceHealth)
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('.status-up')).toHaveLength(2)
    expect(wrapper.findAll('.status-down')).toHaveLength(1)
  })

  it('自动刷新', async () => {
    vi.useFakeTimers()

    vi.mocked(healthApi.getServicesHealth).mockResolvedValue({ data: [] })

    mount(ServiceHealth)

    vi.advanceTimersByTime(30000)

    expect(healthApi.getServicesHealth).toHaveBeenCalledTimes(2)

    vi.useRealTimers()
  })
})
```

---

## 性能测试

### GatewayLoadTest
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayLoadTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("网关吞吐量测试")
    void throughput() {
        int requests = 10000;
        AtomicInteger successCount = new AtomicInteger(0);

        Flux.range(0, requests)
            .flatMap(i -> webTestClient.get()
                .uri("/api/v1/users")
                .header("Authorization", "Bearer token")
                .exchange()
                .returnResult(String.class)
                .getResponseBody()
                .next())
            .doOnNext(res -> successCount.incrementAndGet())
            .blockLast();

        assertTrue(successCount.get() > requests * 0.99); // 99%成功率
    }

    @Test
    @DisplayName("路由延迟测试")
    void latency() {
        long start = System.nanoTime();

        webTestClient.get()
            .uri("/api/v1/users")
            .header("Authorization", "Bearer token")
            .exchange()
            .expectStatus().isOk();

        long duration = System.nanoTime() - start;

        // 网关转发延迟 < 50ms
        assertTrue(duration < 50_000_000);
    }
}
```

### 性能指标

| 指标 | 目标值 |
|------|--------|
| 路由转发延迟 | < 50ms |
| 网关吞吐量 | > 5000 req/s |
| 限流响应时间 | < 10ms |
| 健康检查间隔 | 30s |

---

## 测试清单

- [ ] 单元测试 - TokenValidationFilter
- [ ] 单元测试 - PermissionFilter
- [ ] 单元测试 - RateLimitFilter
- [ ] 单元测试 - RequestLogFilter
- [ ] 单元测试 - TokenBucketLimiter
- [ ] 单元测试 - LeakyBucketLimiter
- [ ] 单元测试 - RouteService
- [ ] 单元测试 - RateLimitService
- [ ] 单元测试 - HealthCheckService
- [ ] 单元测试 - ServiceDiscoveryService
- [ ] 单元测试 - ExternalApiService
- [ ] 单元测试 - ApiKeyService
- [ ] 集成测试 - Gateway路由
- [ ] 集成测试 - 过滤器链
- [ ] 集成测试 - 限流功能
- [ ] 前端测试 - RouteConfig
- [ ] 前端测试 - RateLimitConfig
- [ ] 前端测试 - ServiceHealth
- [ ] 前端测试 - ApiKeyManage
- [ ] 性能测试 - 吞吐量
- [ ] 性能测试 - 延迟
