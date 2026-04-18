# Tests: 用户认证与账号管理

**Feature Branch**: `003-user-auth`
**Generated**: 2026-04-07

## 测试策略概览

| 层级 | 框架 | 覆盖率目标 |
|------|------|------------|
| 单元测试 | JUnit 5 + Mockito | ≥ 85% |
| 集成测试 | Spring Boot Test + Testcontainers | ≥ 80% |
| API测试 | MockMvc | 100% 端点 |
| 前端测试 | Vitest + Vue Test Utils | ≥ 75% |

---

## 单元测试

### TenantServiceTest
```java
@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantMapper tenantMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TenantServiceImpl tenantService;

    @Test
    @DisplayName("租户注册 - 成功")
    void registerTenant_success() {
        // Given
        TenantRegisterRequest request = new TenantRegisterRequest();
        request.setCompanyName("测试公司");
        request.setAdminUsername("admin");
        request.setAdminEmail("admin@test.com");
        request.setAdminPassword("Password123!");

        when(tenantMapper.selectByCompanyName("测试公司")).thenReturn(null);
        when(userMapper.selectByUsername("admin")).thenReturn(null);
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");

        // When
        Long tenantId = tenantService.registerTenant(request);

        // Then
        assertNotNull(tenantId);
        verify(tenantMapper).insert(any(Tenant.class));
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("租户注册 - 公司名称重复")
    void registerTenant_duplicateCompany() {
        // Given
        TenantRegisterRequest request = new TenantRegisterRequest();
        request.setCompanyName("已存在公司");

        when(tenantMapper.selectByCompanyName("已存在公司"))
            .thenReturn(new Tenant());

        // When & Then
        assertThrows(BusinessException.class,
            () -> tenantService.registerTenant(request));
    }

    @Test
    @DisplayName("租户注册 - 管理员用户名重复")
    void registerTenant_duplicateAdminUsername() {
        // Given
        TenantRegisterRequest request = new TenantRegisterRequest();
        request.setCompanyName("新公司");
        request.setAdminUsername("existing_user");

        when(tenantMapper.selectByCompanyName(any())).thenReturn(null);
        when(userMapper.selectByUsername("existing_user"))
            .thenReturn(new User());

        // When & Then
        assertThrows(BusinessException.class,
            () -> tenantService.registerTenant(request));
    }
}
```

### AuthServiceTest
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("用户登录 - 成功")
    void login_success() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Password123!");

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encoded_password");
        user.setStatus(UserStatus.ACTIVE);
        user.setTenantId(100L);

        when(userMapper.selectByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("Password123!", "encoded_password")).thenReturn(true);
        when(jwtUtil.generateToken(any())).thenReturn("access_token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh_token");

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());
    }

    @Test
    @DisplayName("用户登录 - 用户不存在")
    void login_userNotFound() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");

        when(userMapper.selectByUsername("nonexistent")).thenReturn(null);

        // When & Then
        assertThrows(AuthenticationException.class,
            () -> authService.login(request));
    }

    @Test
    @DisplayName("用户登录 - 密码错误")
    void login_wrongPassword() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong_password");

        User user = new User();
        user.setPassword("encoded_password");

        when(userMapper.selectByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("wrong_password", "encoded_password")).thenReturn(false);

        // When & Then
        assertThrows(AuthenticationException.class,
            () -> authService.login(request));
    }

    @Test
    @DisplayName("用户登录 - 账户已禁用")
    void login_accountDisabled() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");

        User user = new User();
        user.setStatus(UserStatus.DISABLED);

        when(userMapper.selectByUsername("admin")).thenReturn(user);

        // When & Then
        assertThrows(AccountDisabledException.class,
            () -> authService.login(request));
    }

    @Test
    @DisplayName("用户登录 - 登录失败次数限制")
    void login_failedAttemptsLimit() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");

        User user = new User();
        user.setFailedAttempts(5);

        when(userMapper.selectByUsername("admin")).thenReturn(user);

        // When & Then
        assertThrows(AccountLockedException.class,
            () -> authService.login(request));
    }
}
```

### TokenServiceTest
```java
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TokenService tokenService;

    @Test
    @DisplayName("生成Token")
    void generateToken() {
        // Given
        Long userId = 1L;
        Long tenantId = 100L;

        when(jwtUtil.generateToken(any())).thenReturn("token123");

        // When
        String token = tokenService.generateToken(userId, tenantId);

        // Then
        assertNotNull(token);
        verify(redisTemplate).opsForValue().set(
            contains("token:"),
            any(),
            eq(Duration.ofHours(2))
        );
    }

    @Test
    @DisplayName("验证Token - 有效")
    void validateToken_valid() {
        // Given
        String token = "valid_token";

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(redisTemplate.hasKey("token:" + token)).thenReturn(true);

        // When
        boolean result = tokenService.validateToken(token);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("验证Token - 已过期")
    void validateToken_expired() {
        // Given
        String token = "expired_token";

        when(jwtUtil.validateToken(token)).thenReturn(false);

        // When
        boolean result = tokenService.validateToken(token);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("验证Token - 已登出")
    void validateToken_loggedOut() {
        // Given
        String token = "logged_out_token";

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(redisTemplate.hasKey("token:" + token)).thenReturn(false);

        // When
        boolean result = tokenService.validateToken(token);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("刷新Token")
    void refreshToken() {
        // Given
        String refreshToken = "refresh_token";
        String newAccessToken = "new_access_token";

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.extractUserId(refreshToken)).thenReturn(1L);
        when(jwtUtil.generateToken(any())).thenReturn(newAccessToken);

        // When
        String result = tokenService.refreshToken(refreshToken);

        // Then
        assertEquals(newAccessToken, result);
    }
}
```

### JwtUtilTest
```java
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        jwtUtil = new JwtUtil();
        jwtUtil.setSecret("test-secret-key-must-be-at-least-256-bits-long");
        jwtUtil.setExpiration(3600000L);
    }

    @Test
    @DisplayName("生成Token")
    void generateToken() {
        // Given
        Long userId = 1L;
        Long tenantId = 100L;

        // When
        String token = jwtUtil.generateToken(userId, tenantId);

        // Then
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    @DisplayName("解析Token")
    void parseToken() {
        // Given
        String token = jwtUtil.generateToken(1L, 100L);

        // When
        Long userId = jwtUtil.extractUserId(token);
        Long tenantId = jwtUtil.extractTenantId(token);

        // Then
        assertEquals(1L, userId);
        assertEquals(100L, tenantId);
    }

    @Test
    @DisplayName("验证Token - 有效")
    void validateToken_valid() {
        // Given
        String token = jwtUtil.generateToken(1L, 100L);

        // When
        boolean result = jwtUtil.validateToken(token);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("验证Token - 篡改")
    void validateToken_tampered() {
        // Given
        String token = jwtUtil.generateToken(1L, 100L);
        String tamperedToken = token + "tampered";

        // When
        boolean result = jwtUtil.validateToken(tamperedToken);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("验证Token - 过期")
    void validateToken_expired() {
        // Given - 创建一个立即过期的token
        jwtUtil.setExpiration(0L);
        String token = jwtUtil.generateToken(1L, 100L);

        // When
        boolean result = jwtUtil.validateToken(token);

        // Then
        assertFalse(result);
    }
}
```

---

## 集成测试

### AuthControllerIntegrationTest
```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("auth_test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.0");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private UserMapper userMapper;

    @BeforeEach
    void setup() {
        // 清理数据
        userMapper.delete(null);
        tenantMapper.delete(null);
    }

    @Test
    @DisplayName("租户注册完整流程")
    void registerTenant_fullFlow() throws Exception {
        String body = """
            {
                "companyName": "测试公司",
                "adminUsername": "admin",
                "adminEmail": "admin@test.com",
                "adminPassword": "Password123!",
                "adminPhone": "13800138000"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.tenantId").exists())
            .andExpect(jsonPath("$.data.adminUserId").exists());
    }

    @Test
    @DisplayName("登录完整流程")
    void login_fullFlow() throws Exception {
        // 先注册
        createTestTenant();

        String body = """
            {
                "username": "admin",
                "password": "Password123!"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists())
            .andExpect(jsonPath("$.data.expiresIn").isNumber());
    }

    @Test
    @DisplayName("登出")
    void logout() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        // 验证Token已失效
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token刷新")
    void refreshToken() throws Exception {
        LoginResponse loginResponse = login();

        mockMvc.perform(post("/api/v1/auth/refresh")
                .param("refreshToken", loginResponse.getRefreshToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists());
    }

    private void createTestTenant() {
        // 创建测试租户和用户
    }

    private String loginAndGetToken() {
        // 登录并返回token
        return "test_token";
    }
}
```

### TenantControllerIntegrationTest
```java
@SpringBootTest
@AutoConfigureMockMvc
class TenantControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("获取租户信息")
    void getTenantInfo() throws Exception {
        String token = getTenantAdminToken();

        mockMvc.perform(get("/api/v1/tenant/info")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.companyName").exists())
            .andExpect(jsonPath("$.data.status").exists());
    }

    @Test
    @DisplayName("更新租户信息")
    void updateTenantInfo() throws Exception {
        String token = getTenantAdminToken();

        String body = """
            {
                "companyName": "新公司名称",
                "contactEmail": "new@test.com"
            }
            """;

        mockMvc.perform(put("/api/v1/tenant/info")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());
    }
}
```

---

## 前端测试

### Login.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import Login from '@/views/auth/Login.vue'
import * as authApi from '@/api/auth'

vi.mock('@/api/auth')

describe('Login', () => {
  it('渲染登录表单', () => {
    const wrapper = mount(Login)

    expect(wrapper.find('input[name="username"]').exists()).toBe(true)
    expect(wrapper.find('input[name="password"]').exists()).toBe(true)
    expect(wrapper.find('.login-btn').exists()).toBe(true)
  })

  it('表单验证 - 必填字段', async () => {
    const wrapper = mount(Login)

    await wrapper.find('.login-btn').trigger('click')

    expect(wrapper.find('.username-error').exists()).toBe(true)
    expect(wrapper.find('.password-error').exists()).toBe(true)
  })

  it('登录成功', async () => {
    vi.mocked(authApi.login).mockResolvedValue({
      data: {
        accessToken: 'token123',
        refreshToken: 'refresh123',
        expiresIn: 7200
      }
    })

    const mockRouter = { push: vi.fn() }
    const wrapper = mount(Login, {
      global: {
        mocks: { $router: mockRouter }
      }
    })

    await wrapper.find('input[name="username"]').setValue('admin')
    await wrapper.find('input[name="password"]').setValue('Password123!')
    await wrapper.find('.login-btn').trigger('click')

    expect(authApi.login).toHaveBeenCalledWith({
      username: 'admin',
      password: 'Password123!'
    })
  })

  it('登录失败 - 显示错误信息', async () => {
    vi.mocked(authApi.login).mockRejectedValue({
      response: { data: { message: '用户名或密码错误' } }
    })

    const wrapper = mount(Login)

    await wrapper.find('input[name="username"]').setValue('admin')
    await wrapper.find('input[name="password"]').setValue('wrong')
    await wrapper.find('.login-btn').trigger('click')

    expect(wrapper.find('.error-message').text()).toContain('用户名或密码错误')
  })

  it('记住密码', async () => {
    const wrapper = mount(Login)

    await wrapper.find('.remember-checkbox').setChecked(true)
    await wrapper.find('input[name="username"]').setValue('admin')
    await wrapper.find('.login-btn').trigger('click')

    // 验证localStorage
    expect(localStorage.getItem('remembered_username')).toBe('admin')
  })
})
```

### Register.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import Register from '@/views/auth/Register.vue'
import * as authApi from '@/api/auth'

vi.mock('@/api/auth')

describe('Register', () => {
  it('渲染注册表单', () => {
    const wrapper = mount(Register)

    expect(wrapper.find('input[name="companyName"]').exists()).toBe(true)
    expect(wrapper.find('input[name="username"]').exists()).toBe(true)
    expect(wrapper.find('input[name="email"]').exists()).toBe(true)
    expect(wrapper.find('input[name="password"]').exists()).toBe(true)
  })

  it('密码强度验证', async () => {
    const wrapper = mount(Register)

    // 弱密码
    await wrapper.find('input[name="password"]').setValue('123')
    expect(wrapper.find('.password-strength').classes()).toContain('weak')

    // 中等密码
    await wrapper.find('input[name="password"]').setValue('Password123')
    expect(wrapper.find('.password-strength').classes()).toContain('medium')

    // 强密码
    await wrapper.find('input[name="password"]').setValue('Password123!')
    expect(wrapper.find('.password-strength').classes()).toContain('strong')
  })

  it('确认密码匹配', async () => {
    const wrapper = mount(Register)

    await wrapper.find('input[name="password"]').setValue('Password123!')
    await wrapper.find('input[name="confirmPassword"]').setValue('Different123!')
    await wrapper.find('.register-btn').trigger('click')

    expect(wrapper.find('.confirm-password-error').text()).toContain('不一致')
  })

  it('注册成功', async () => {
    vi.mocked(authApi.register).mockResolvedValue({
      data: { tenantId: 1, adminUserId: 1 }
    })

    const mockRouter = { push: vi.fn() }
    const wrapper = mount(Register, {
      global: {
        mocks: { $router: mockRouter }
      }
    })

    await fillRegisterForm(wrapper)
    await wrapper.find('.register-btn').trigger('click')

    expect(mockRouter.push).toHaveBeenCalledWith('/login')
  })
})
```

### tokenRefresh.spec.ts
```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setupTokenRefresh } from '@/utils/tokenRefresh'
import * as authApi from '@/api/auth'

vi.mock('@/api/auth')

describe('TokenRefresh', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('Token即将过期时自动刷新', async () => {
    vi.useFakeTimers()

    // 设置即将过期的token
    localStorage.setItem('token', 'old_token')
    localStorage.setItem('refreshToken', 'refresh_token')
    localStorage.setItem('tokenExpiry', String(Date.now() + 5 * 60 * 1000)) // 5分钟后过期

    vi.mocked(authApi.refreshToken).mockResolvedValue({
      data: { accessToken: 'new_token', expiresIn: 7200 }
    })

    setupTokenRefresh()

    // 快进4分钟
    vi.advanceTimersByTime(4 * 60 * 1000)

    expect(authApi.refreshToken).toHaveBeenCalled()
    expect(localStorage.getItem('token')).toBe('new_token')

    vi.useRealTimers()
  })

  it('刷新失败跳转登录页', async () => {
    vi.mocked(authApi.refreshToken).mockRejectedValue(new Error('Invalid refresh token'))

    const mockRouter = { push: vi.fn() }
    setupTokenRefresh(mockRouter)

    // 触发刷新
    await new Promise(resolve => setTimeout(resolve, 0))

    expect(mockRouter.push).toHaveBeenCalledWith('/login')
  })
})
```

---

## 安全测试

### 密码安全测试
```java
@SpringBootTest
class PasswordSecurityTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("密码加密 - BCrypt")
    void passwordEncoding_bcrypt() {
        String rawPassword = "Password123!";
        String encoded = passwordEncoder.encode(rawPassword);

        assertNotEquals(rawPassword, encoded);
        assertTrue(encoded.startsWith("$2a$"));
    }

    @Test
    @DisplayName("密码加密 - 每次结果不同")
    void passwordEncoding_differentSalt() {
        String rawPassword = "Password123!";
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);

        assertNotEquals(encoded1, encoded2);
    }

    @Test
    @DisplayName("密码验证")
    void passwordVerification() {
        String rawPassword = "Password123!";
        String encoded = passwordEncoder.encode(rawPassword);

        assertTrue(passwordEncoder.matches(rawPassword, encoded));
        assertFalse(passwordEncoder.matches("wrong", encoded));
    }

    @Test
    @DisplayName("密码强度验证")
    void passwordStrength() {
        // 弱密码
        assertFalse(authService.isPasswordStrong("123456"));

        // 中等
        assertTrue(authService.isPasswordStrong("Password123"));

        // 强
        assertTrue(authService.isPasswordStrong("Password123!"));
    }
}
```

### JWT安全测试
```java
@SpringBootTest
class JwtSecurityTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("JWT签名验证")
    void jwtSignatureVerification() {
        String token = jwtUtil.generateToken(1L, 100L);

        // 篡改token
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + ".tampered";

        assertFalse(jwtUtil.validateToken(tampered));
    }

    @Test
    @DisplayName("JWT过期验证")
    void jwtExpiration() {
        // 创建已过期的token
        String expiredToken = Jwts.builder()
            .setSubject("1")
            .setIssuedAt(new Date(System.currentTimeMillis() - 3600000))
            .setExpiration(new Date(System.currentTimeMillis() - 1000))
            .signWith(SignatureAlgorithm.HS256, "secret")
            .compact();

        assertFalse(jwtUtil.validateToken(expiredToken));
    }

    @Test
    @DisplayName("JWT防重放攻击")
    void jwtReplayAttack() {
        // Token已登出后不能重用
        String token = jwtUtil.generateToken(1L, 100L);

        // 模拟登出
        tokenService.invalidateToken(token);

        // 重用token
        assertFalse(tokenService.validateToken(token));
    }
}
```

### 会话安全测试
```java
@SpringBootTest
@AutoConfigureMockMvc
class SessionSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("并发登录控制")
    void concurrentLogin() throws Exception {
        // 第一次登录
        String token1 = login("admin", "Password123!");

        // 第二次登录（同一用户）
        String token2 = login("admin", "Password123!");

        // 第一个token应该失效
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + token1))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("异地登录检测")
    void异地Login() throws Exception {
        // 从IP1登录
        String token = loginWithIp("admin", "Password123!", "192.168.1.1");

        // 从IP2登录（触发异地登录告警）
        loginWithIp("admin", "Password123!", "10.0.0.1");

        // 验证告警已发送
        // verify(notificationService).sendLoginAlert(any());
    }
}
```

---

## 性能测试

### 登录性能测试
```java
@SpringBootTest
class LoginPerformanceTest {

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("登录性能基准")
    void loginPerformance() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Password123!");

        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            authService.login(request);
        }
        long duration = System.nanoTime() - start;

        // 平均响应时间 < 100ms
        assertTrue(duration / 1000 < 100_000_000);
    }
}
```

### 性能指标

| 接口 | 并发数 | 平均响应时间 | 吞吐量 |
|------|--------|--------------|--------|
| POST /auth/login | 200 | < 100ms | > 1000/s |
| POST /auth/register | 50 | < 500ms | > 100/s |
| POST /auth/refresh | 200 | < 50ms | > 2000/s |
| POST /auth/logout | 200 | < 30ms | > 3000/s |

---

## 测试清单

- [ ] 单元测试 - TenantService
- [ ] 单元测试 - AuthService
- [ ] 单元测试 - TokenService
- [ ] 单元测试 - JwtUtil
- [ ] 单元测试 - UserProfileService
- [ ] 集成测试 - AuthController
- [ ] 集成测试 - TenantController
- [ ] 集成测试 - LogoutController
- [ ] 前端测试 - Login
- [ ] 前端测试 - Register
- [ ] 前端测试 - UserProfile
- [ ] 前端测试 - ChangePassword
- [ ] 安全测试 - 密码安全
- [ ] 安全测试 - JWT安全
- [ ] 安全测试 - 会话安全
- [ ] 性能测试 - 登录性能
