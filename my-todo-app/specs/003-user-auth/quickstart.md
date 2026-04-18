# Quick Start: 用户认证与账号管理集成指南

**Feature**: 用户认证与账号管理 (003-user-auth)
**Date**: 2026-01-10
**Purpose**: 快速集成和使用用户认证功能

## 概述

本指南帮助开发者快速集成用户认证与账号管理模块到多租户 SaaS 系统中。

## 前置条件

- Java 17+
- Spring Boot 3.0+
- MySQL 8.0+
- Redis 7.0+
- Vue 3.0 + Ant Design 6.1.4

## 快速开始

### 1. 添加依赖

在 `auth-service/pom.xml` 中添加：

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>

    <!-- EasyCaptcha -->
    <dependency>
        <groupId>com.github.whvcse</groupId>
        <artifactId>easy-captcha</artifactId>
        <version>1.6.2</version>
    </dependency>

    <!-- WebSocket -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
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

    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
</dependencies>
```

### 2. 配置数据库

在 `application.yml` 中配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/saas_db?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 3000ms

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: your-256-bit-secret-key-at-least-32-bytes-long
  access-token-expiry: 7200000  # 2小时 (毫秒)
  refresh-token-expiry: 604800000  # 7天 (毫秒)
```

### 3. 配置 Spring Security

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/register",
                    "/api/v1/captcha/**",
                    "/ws/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

### 4. 创建 JWT Token Provider

```java
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(User user, List<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("tenantId", user.getTenantId());
        claims.put("username", user.getUsername());
        claims.put("permissions", permissions);

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(String.valueOf(user.getUserId()))
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact();
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(User user) {
        return Jwts.builder()
            .setSubject(String.valueOf(user.getUserId()))
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiry))
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact();
    }

    /**
     * 验证 Token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(secret)
            .build()
            .parseClaimsJws(token)
            .getBody();
        return Long.parseLong(claims.getSubject());
    }
}
```

### 5. 创建认证过滤器

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private TokenBlacklistService blacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if (token != null && tokenProvider.validateToken(token)) {
            // 检查 Token 是否在黑名单中
            if (!blacklistService.isBlacklisted(token)) {
                Long userId = tokenProvider.getUserIdFromToken(token);
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

### 6. 实现登录服务

```java
@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private SessionService sessionService;

    public LoginResponse login(String username, String password, String ipAddress) {
        // 检查是否被锁定
        if (loginAttemptService.isLocked(username)) {
            throw new AccountLockedException("账号已被锁定，请30分钟后再试");
        }

        // 查询用户
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
        );

        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            loginAttemptService.loginFailed(username);
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new AccountDisabledException("账号已被禁用");
        }

        // 清除失败计数
        loginAttemptService.loginSucceeded(username);

        // 获取用户权限
        List<String> permissions = getUserPermissions(user.getUserId());

        // 生成 Token
        String accessToken = tokenProvider.generateAccessToken(user, permissions);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        // 创建会话 (支持挤号登录)
        sessionService.createSession(user.getUserId(), accessToken, ipAddress);

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        userMapper.updateById(user);

        return new LoginResponse(accessToken, refreshToken, user, permissions);
    }

    public void logout(String token, Long userId, boolean logoutAll) {
        if (logoutAll) {
            // 全端登出
            blacklistService.blacklistAllUserTokens(userId);
        } else {
            // 当前设备登出
            long expiryTime = tokenProvider.getExpiryTime(token);
            blacklistService.addToBlacklist(token, expiryTime);
        }

        // 删除会话
        sessionService.deleteSession(token);
    }
}
```

### 7. 实现登录失败限制服务

```java
@Component
public class LoginAttemptService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String ATTEMPT_PREFIX = "login:attempt:";
    private static final String LOCK_PREFIX = "login:lock:";
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION = 30 * 60 * 1000; // 30分钟

    public void loginFailed(String username) {
        String key = ATTEMPT_PREFIX + username;
        Long attempts = redisTemplate.opsForValue().increment(key);

        if (attempts == 1) {
            redisTemplate.expire(key, 30, TimeUnit.MINUTES);
        }

        if (attempts >= MAX_ATTEMPTS) {
            lockUser(username);
        }
    }

    public void loginSucceeded(String username) {
        redisTemplate.delete(ATTEMPT_PREFIX + username);
    }

    public boolean isLocked(String username) {
        String key = LOCK_PREFIX + username;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public int getRemainingAttempts(String username) {
        String key = ATTEMPT_PREFIX + username;
        String value = redisTemplate.opsForValue().get(key);
        int attempts = value != null ? Integer.parseInt(value) : 0;
        return Math.max(0, MAX_ATTEMPTS - attempts);
    }
}
```

### 8. 控制器

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request,
                                      HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        LoginResponse response = authService.login(
            request.getUsername(),
            request.getPassword(),
            ipAddress
        );
        return Result.success(response);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody LogoutRequest request,
                               HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        Long userId = getCurrentUserId(token);
        authService.logout(token, userId, request.isLogoutAll());
        return Result.success();
    }
}
```

### 9. 前端 API 客户端

```javascript
// frontend/src/api/auth.js
import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/api/v1/auth/login',
    method: 'post',
    data
  })
}

export function logout(data) {
  return request({
    url: '/api/v1/auth/logout',
    method: 'post',
    data
  })
}

export function refreshToken(data) {
  return request({
    url: '/api/v1/auth/refresh',
    method: 'post',
    data
  })
}

export function getProfile() {
  return request({
    url: '/api/v1/auth/profile',
    method: 'get'
  })
}

export function updatePassword(data) {
  return request({
    url: '/api/v1/auth/profile/password',
    method: 'put',
    data
  })
}
```

### 10. 前端登录页面

```vue
<template>
  <a-card class="login-card" title="用户登录">
    <a-form @finish="handleLogin">
      <a-form-item
        name="username"
        :rules="[{ required: true, message: '请输入用户名' }]"
      >
        <a-input
          v-model:value="form.username"
          placeholder="用户名"
          size="large"
        />
      </a-form-item>

      <a-form-item
        name="password"
        :rules="[{ required: true, message: '请输入密码' }]"
      >
        <a-input-password
          v-model:value="form.password"
          placeholder="密码"
          size="large"
        />
      </a-form-item>

      <a-form-item v-if="requireCaptcha">
        <a-row :gutter="8">
          <a-col :span="14">
            <a-input
              v-model:value="form.captchaCode"
              placeholder="验证码"
              size="large"
            />
          </a-col>
          <a-col :span="10">
            <img
              :src="captchaImage"
              @click="refreshCaptcha"
              style="cursor: pointer; width: 100%; height: 38px"
            />
          </a-col>
        </a-row>
      </a-form-item>

      <a-form-item>
        <a-button
          type="primary"
          html-type="submit"
          size="large"
          block
          :loading="loading"
        >
          登录
        </a-button>
      </a-form-item>
    </a-form>
  </a-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { login } from '@/api/auth'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const requireCaptcha = ref(false)
const captchaImage = ref('')
const form = ref({
  username: '',
  password: '',
  captchaId: '',
  captchaCode: ''
})

const handleLogin = async () => {
  loading.value = true
  try {
    const { data } = await login(form.value)
    // 存储 Token
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    // 跳转到首页
    router.push('/')
  } catch (error) {
    if (error.data?.requireCaptcha) {
      requireCaptcha.value = true
      refreshCaptcha()
    }
  } finally {
    loading.value = false
  }
}
</script>
```

## 常见问题

### Q: 如何修改 Token 有效期？

A: 在 `application.yml` 中配置：
```yaml
jwt:
  access-token-expiry: 7200000  # 2小时
  refresh-token-expiry: 604800000  # 7天
```

### Q: 如何实现全端登出？

A: 登出时设置 `logoutAll: true`，系统会将用户的所有 Token 加入黑名单。

### Q: 验证码什么时候必填？

A: 连续登录失败 3 次后，第 4 次登录时验证码必填。

### Q: 如何处理 Token 过期？

A: 前端使用 Refresh Token 调用 `/api/v1/auth/refresh` 刷新 Token，如果 Refresh Token 也过期，引导用户重新登录。

## 下一步

- 查看完整 API 文档: `contracts/`
- 了解数据模型: `data-model.md`
- 查看技术选型: `research.md`
