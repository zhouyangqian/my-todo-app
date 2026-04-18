# Tests: 权限模块

**Feature Branch**: `002-permission-module`
**Generated**: 2026-04-07

## 测试策略概览

| 层级 | 框架 | 覆盖率目标 |
|------|------|------------|
| 单元测试 | JUnit 5 + Mockito | ≥ 85% |
| 集成测试 | Spring Boot Test + Testcontainers | ≥ 75% |
| API测试 | MockMvc | 100% 端点 |
| 前端测试 | Vitest + Vue Test Utils | ≥ 75% |

---

## 单元测试

### PermissionServiceTest
```java
@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private PermissionCache permissionCache;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @Test
    @DisplayName("检查权限 - 有权限")
    void hasPermission_true() {
        // Given
        Long userId = 1L;
        String permission = "user:create";

        when(permissionMapper.selectUserPermissions(userId))
            .thenReturn(List.of("user:create", "user:read"));

        // When
        boolean result = permissionService.hasPermission(userId, permission);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("检查权限 - 无权限")
    void hasPermission_false() {
        // Given
        Long userId = 1L;
        String permission = "user:delete";

        when(permissionMapper.selectUserPermissions(userId))
            .thenReturn(List.of("user:create"));

        // When
        boolean result = permissionService.hasPermission(userId, permission);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("权限缓存 - 命中")
    void hasPermission_cacheHit() {
        // Given
        Long userId = 1L;
        String permission = "user:create";

        when(permissionCache.getPermissions(userId))
            .thenReturn(Set.of("user:create"));

        // When
        boolean result = permissionService.hasPermission(userId, permission);

        // Then
        assertTrue(result);
        verify(permissionMapper, never()).selectUserPermissions(any());
    }
}
```

### PermissionInterceptorTest
```java
class PermissionInterceptorTest {

    private PermissionInterceptor interceptor;

    @Mock
    private PermissionService permissionService;

    @BeforeEach
    void setup() {
        interceptor = new PermissionInterceptor(permissionService);
    }

    @Test
    @DisplayName("拦截器 - 无注解放行")
    void preHandle_noAnnotation() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handler = mock(HandlerMethod.class);
        when(handler.getMethodAnnotation(RequiresPermission.class)).thenReturn(null);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("拦截器 - 有权限放行")
    void preHandle_hasPermission() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 1L);

        when(permissionService.hasPermission(1L, "user:create")).thenReturn(true);

        HandlerMethod handler = mock(HandlerMethod.class);
        RequiresPermission annotation = mock(RequiresPermission.class);
        when(annotation.value()).thenReturn("user:create");
        when(handler.getMethodAnnotation(RequiresPermission.class)).thenReturn(annotation);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("拦截器 - 无权限拒绝")
    void preHandle_noPermission() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 1L);

        when(permissionService.hasPermission(1L, "user:delete")).thenReturn(false);

        HandlerMethod handler = mock(HandlerMethod.class);
        RequiresPermission annotation = mock(RequiresPermission.class);
        when(annotation.value()).thenReturn("user:delete");
        when(handler.getMethodAnnotation(RequiresPermission.class)).thenReturn(annotation);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertFalse(result);
        assertEquals(403, response.getStatus());
    }
}
```

### DataScopeInterceptorTest
```java
class DataScopeInterceptorTest {

    private DataScopeInterceptor interceptor;

    @Test
    @DisplayName("数据权限 - 全部数据")
    void buildDataScope_all() {
        // Given
        User user = new User();
        user.setDataScope(DataScopeType.ALL);

        // When
        String scope = interceptor.buildDataScope(user);

        // Then
        assertEquals("1=1", scope);
    }

    @Test
    @DisplayName("数据权限 - 本部门")
    void buildDataScope_dept() {
        // Given
        User user = new User();
        user.setDataScope(DataScopeType.DEPT);
        user.setDeptId(100L);

        // When
        String scope = interceptor.buildDataScope(user);

        // Then
        assertEquals("dept_id = 100", scope);
    }

    @Test
    @DisplayName("数据权限 - 仅本人")
    void buildDataScope_self() {
        // Given
        User user = new User();
        user.setDataScope(DataScopeType.SELF);
        user.setId(1L);

        // When
        String scope = interceptor.buildDataScope(user);

        // Then
        assertEquals("created_by = 1", scope);
    }
}
```

### RoleServiceTest
```java
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private RolePermissionMapper rolePermissionMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    @DisplayName("创建角色 - 成功")
    void createRole_success() {
        // Given
        RoleCreateRequest request = new RoleCreateRequest();
        request.setName("管理员");
        request.setCode("ADMIN");
        request.setPermissionIds(List.of(1L, 2L, 3L));

        when(roleMapper.selectByCode("ADMIN")).thenReturn(null);
        when(roleMapper.insert(any())).thenReturn(1);

        // When
        Long roleId = roleService.createRole(request);

        // Then
        assertNotNull(roleId);
        verify(rolePermissionMapper, times(3)).insert(any());
    }

    @Test
    @DisplayName("创建角色 - 编码重复")
    void createRole_duplicateCode() {
        // Given
        RoleCreateRequest request = new RoleCreateRequest();
        request.setCode("ADMIN");

        when(roleMapper.selectByCode("ADMIN")).thenReturn(new Role());

        // When & Then
        assertThrows(BusinessException.class,
            () -> roleService.createRole(request));
    }
}
```

### SessionServiceTest
```java
@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionMapper sessionMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private SessionService sessionService;

    @Test
    @DisplayName("获取在线用户")
    void getOnlineUsers() {
        // Given
        Long tenantId = 1L;
        List<Session> sessions = List.of(
            new Session(1L, "user1", "192.168.1.1"),
            new Session(2L, "user2", "192.168.1.2")
        );
        when(sessionMapper.selectOnlineByTenant(tenantId)).thenReturn(sessions);

        // When
        List<Session> result = sessionService.getOnlineUsers(tenantId);

        // Then
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("强制下线")
    void kickout() {
        // Given
        Long sessionId = 1L;
        Session session = new Session();
        session.setId(sessionId);
        session.setToken("token123");

        when(sessionMapper.selectById(sessionId)).thenReturn(session);

        // When
        sessionService.kickout(sessionId);

        // Then
        verify(redisTemplate).delete("token:token123");
        verify(sessionMapper).updateStatus(sessionId, SessionStatus.KICKED);
    }
}
```

---

## 集成测试

### PermissionControllerIntegrationTest
```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PermissionControllerIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.0");

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("获取权限列表")
    void getPermissions() throws Exception {
        mockMvc.perform(get("/api/v1/permissions")
                .header("Authorization", "Bearer token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("检查权限 API")
    void checkPermission() throws Exception {
        mockMvc.perform(post("/api/v1/permissions/check")
                .param("permission", "user:create")
                .header("Authorization", "Bearer token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isBoolean());
    }
}
```

### RoleControllerIntegrationTest
```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RoleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleMapper roleMapper;

    @Test
    @DisplayName("创建角色完整流程")
    void createRole_fullFlow() throws Exception {
        String body = """
            {
                "name": "测试角色",
                "code": "TEST_ROLE",
                "permissionIds": [1, 2, 3]
            }
            """;

        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").exists());

        // 验证数据库
        Role role = roleMapper.selectByCode("TEST_ROLE");
        assertNotNull(role);
    }

    @Test
    @DisplayName("分配权限")
    void assignPermissions() throws Exception {
        Long roleId = 1L;
        String body = "[1, 2, 3, 4]";

        mockMvc.perform(put("/api/v1/roles/{id}/permissions", roleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());
    }
}
```

---

## WebSocket 测试

### SessionWebSocketTest
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SessionWebSocketTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setup() {
        stompClient = new WebSocketStompClient(new SockJsClient(
            List.of(new WebSocketTransport(new StandardWebSocketClient()))
        ));
    }

    @Test
    @DisplayName("WebSocket 连接")
    void connectWebSocket() throws Exception {
        String url = "ws://localhost:" + port + "/ws/session";

        StompSession session = stompClient.connect(url, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);

        assertTrue(session.isConnected());
    }

    @Test
    @DisplayName("接收挤号通知")
    void receiveKickoutNotification() throws Exception {
        String url = "ws://localhost:" + port + "/ws/session";
        CompletableFuture<String> messageFuture = new CompletableFuture<>();

        StompSession session = stompClient.connect(url, new StompSessionHandlerAdapter() {}).get();

        session.subscribe("/user/queue/kickout", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageFuture.complete((String) payload);
            }
        });

        // 触发挤号
        kickoutService.kickout(userId, "异地登录");

        String message = messageFuture.get(5, TimeUnit.SECONDS);
        assertThat(message).contains("异地登录");
    }
}
```

---

## 前端测试

### PermissionList.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import PermissionList from '@/views/permission/PermissionList.vue'
import * as permissionApi from '@/api/permission'

vi.mock('@/api/permission')

describe('PermissionList', () => {
  it('渲染权限树', async () => {
    vi.mocked(permissionApi.getPermissionTree).mockResolvedValue({
      data: [
        { id: 1, name: '用户管理', code: 'user', children: [
          { id: 2, name: '创建用户', code: 'user:create' },
          { id: 3, name: '删除用户', code: 'user:delete' }
        ]}
      ]
    })

    const wrapper = mount(PermissionList)
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('.permission-node')).toHaveLength(3)
  })

  it('搜索权限', async () => {
    const wrapper = mount(PermissionList)

    await wrapper.find('.search-input').setValue('创建')
    await wrapper.vm.filterPermissions()

    expect(wrapper.vm.filteredPermissions.length).toBeGreaterThan(0)
  })
})
```

### RoleList.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RoleList from '@/views/permission/RoleList.vue'

describe('RoleList', () => {
  it('渲染角色列表', async () => {
    const wrapper = mount(RoleList, {
      data() {
        return {
          roles: [
            { id: 1, name: '管理员', code: 'ADMIN' },
            { id: 2, name: '普通用户', code: 'USER' }
          ]
        }
      }
    })

    expect(wrapper.findAll('.role-item')).toHaveLength(2)
  })

  it('打开权限配置弹窗', async () => {
    const wrapper = mount(RoleList)

    await wrapper.find('.config-permission-btn').trigger('click')

    expect(wrapper.find('.permission-dialog').isVisible()).toBe(true)
  })

  it('保存权限配置', async () => {
    const wrapper = mount(RoleList)

    await wrapper.setData({ selectedRole: { id: 1 } })
    await wrapper.setData({ selectedPermissions: [1, 2, 3] })
    await wrapper.find('.save-btn').trigger('click')

    expect(wrapper.emitted('saved')).toBeTruthy()
  })
})
```

### KickoutNotification.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import KickoutNotification from '@/components/KickoutNotification.vue'

describe('KickoutNotification', () => {
  it('显示挤号通知', async () => {
    const wrapper = mount(KickoutNotification)

    await wrapper.setData({
      visible: true,
      reason: '您的账号在另一设备登录'
    })

    expect(wrapper.find('.kickout-message').text()).toContain('另一设备登录')
  })

  it('点击确定跳转登录页', async () => {
    const mockRouter = { push: vi.fn() }
    const wrapper = mount(KickoutNotification, {
      global: {
        mocks: { $router: mockRouter }
      }
    })

    await wrapper.setData({ visible: true })
    await wrapper.find('.confirm-btn').trigger('click')

    expect(mockRouter.push).toHaveBeenCalledWith('/login')
  })
})
```

---

## 安全测试

### 权限绕过测试
```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("无Token访问受保护接口")
    void accessProtected_withoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/permissions"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("越权访问其他租户数据")
    void crossTenantAccess() throws Exception {
        // 用户属于租户1，尝试访问租户2的角色
        mockMvc.perform(get("/api/v1/roles/2")
                .header("Authorization", "Bearer tenant1_token"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("权限提升攻击")
    void privilegeEscalation() throws Exception {
        // 普通用户尝试给自己分配管理员权限
        String body = """
            {
                "userId": 2,
                "roleIds": [1]
            }
            """;

        mockMvc.perform(post("/api/v1/users/assign-roles")
                .header("Authorization", "Bearer normal_user_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());
    }
}
```

### SQL注入测试
```java
@SpringBootTest
@AutoConfigureMockMvc
class SqlInjectionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("角色名称SQL注入")
    void sqlInjection_roleName() throws Exception {
        String maliciousName = "Admin' OR '1'='1";

        mockMvc.perform(get("/api/v1/roles")
                .param("name", maliciousName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records").isArray());

        // 验证没有返回额外数据
    }
}
```

---

## 性能测试

### 权限检查性能
```java
@SpringBootTest
class PermissionPerformanceTest {

    @Autowired
    private PermissionService permissionService;

    @Test
    @DisplayName("权限检查性能 - 缓存命中")
    void hasPermission_cachePerformance() {
        Long userId = 1L;
        String permission = "user:create";

        // 预热缓存
        permissionService.hasPermission(userId, permission);

        // 测试
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            permissionService.hasPermission(userId, permission);
        }
        long duration = System.nanoTime() - start;

        // 平均响应时间 < 1ms
        assertTrue(duration / 10000 < 1_000_000);
    }
}
```

### 性能指标

| 接口 | 并发数 | 平均响应时间 | 吞吐量 |
|------|--------|--------------|--------|
| POST /permissions/check | 200 | < 50ms | > 2000/s |
| GET /roles | 100 | < 100ms | > 500/s |
| PUT /roles/{id}/permissions | 50 | < 200ms | > 100/s |

---

## 测试清单

- [ ] 单元测试 - PermissionService
- [ ] 单元测试 - RoleService
- [ ] 单元测试 - SessionService
- [ ] 单元测试 - BlacklistService
- [ ] 单元测试 - PermissionInterceptor
- [ ] 单元测试 - DataScopeInterceptor
- [ ] 集成测试 - PermissionController
- [ ] 集成测试 - RoleController
- [ ] WebSocket 测试 - 连接
- [ ] WebSocket 测试 - 挤号通知
- [ ] 前端测试 - PermissionList
- [ ] 前端测试 - RoleList
- [ ] 前端测试 - KickoutNotification
- [ ] 安全测试 - 权限绕过
- [ ] 安全测试 - SQL注入
- [ ] 性能测试 - 权限检查
