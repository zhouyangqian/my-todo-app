# Research: 用户认证与账号管理技术调研

**Feature**: 用户认证与账号管理 (003-user-auth)
**Date**: 2026-01-10
**Purpose**: 技术选型和研究决策记录

## 概述

本文档记录用户认证与账号管理模块的技术研究和选型决策。

## 技术选型

### 1. Spring Security + JWT 集成

**选择**: Spring Security 6.0 + JWT (io.jsonwebtoken:jjwt)

**理由**:
- Spring Security 是 Java 生态最成熟的安全框架
- JWT Token 无状态，适合分布式系统
- 支持自定义 Filter 实现 JWT 认证
- 与 Spring Boot 3.0 完美集成

**关键配置**:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

### 2. BCrypt 密码加密

**选择**: Spring Security BCryptPasswordEncoder

**理由**:
- BCrypt 是业界标准的密码哈希算法
- 自动加盐，防止彩虹表攻击
- 计算密集型，防止暴力破解
- Spring Security 内置支持

**实现**:

```java
@Component
public class PasswordEncoder {

    private static final org.springframework.security.crypto.password.PasswordEncoder
        encoder = new BCryptPasswordEncoder(12); // strength=12

    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
```

**密码策略验证**:

```java
public class PasswordValidator {

    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    public static void validate(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException(
                "密码必须至少8位，包含字母和数字"
            );
        }
    }
}
```

### 3. JWT Token 黑名单

**选择**: Redis + TTL 自动过期

**理由**:
- Redis 查询速度快 (<10ms)
- 支持自动 TTL 过期
- 黑名单记录与 Token 有效期同步
- 支持全端登出 (批量加入黑名单)

**实现**:

```java
@Service
public class TokenBlacklistService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "auth:blacklist:";
    private static final String USER_TOKENS_PREFIX = "auth:tokens:";

    /**
     * 将 Token 加入黑名单
     * @param token Token 字符串
     * @param expiryTime Token 过期时间戳
     */
    public void addToBlacklist(String token, long expiryTime) {
        long ttl = expiryTime - System.currentTimeMillis();
        if (ttl > 0) {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 检查 Token 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 全端登出：将用户的所有 Token 加入黑名单
     */
    public void blacklistAllUserTokens(Long userId) {
        String key = USER_TOKENS_PREFIX + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(key);
        if (tokens != null) {
            long now = System.currentTimeMillis() + 2 * 60 * 60 * 1000; // 2小时后
            tokens.forEach(token -> addToBlacklist(token, now));
            redisTemplate.delete(key);
        }
    }
}
```

### 4. 图形验证码

**选择**: EasyCaptcha (com.github.whvcse:easy-captcha)

**理由**:
- 轻量级，易于集成
- 支持多种验证码类型 (字母数字、算术、汉字)
- 配置简单，开箱即用
- 性能好，生成速度 <100ms

**实现**:

```java
@RestController
@RequestMapping("/api/v1/captcha")
public class CaptchaController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final long CAPTCHA_EXPIRY = 5 * 60 * 1000; // 5分钟

    /**
     * 生成验证码
     */
    @GetMapping
    public Result<CaptchaResponse> generate() {
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        captcha.setCharType(Captcha.TYPE_DEFAULT);

        String captchaId = UUID.randomUUID().toString();
        String code = captcha.text().toLowerCase();

        // 存储验证码哈希（SHA-256）
        String codeHash = DigestUtils.sha256Hex(code);
        String key = CAPTCHA_PREFIX + captchaId;

        redisTemplate.opsForValue().set(
            key,
            codeHash + ":" + System.currentTimeMillis(),
            CAPTCHA_EXPIRY,
            TimeUnit.MILLISECONDS
        );

        return Result.success(new CaptchaResponse(captchaId, captcha.toBase64()));
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verify")
    public Result<Void> verify(@RequestBody CaptchaRequest request) {
        String key = CAPTCHA_PREFIX + request.getCaptchaId();
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return Result.error("验证码已过期");
        }

        String[] parts = value.split(":");
        String codeHash = parts[0];
        String inputHash = DigestUtils.sha256Hex(
            request.getCode().toLowerCase()
        );

        if (!codeHash.equals(inputHash)) {
            return Result.error("验证码错误");
        }

        // 标记为已使用
        redisTemplate.delete(key);
        return Result.success();
    }
}
```

### 5. 登录失败限制

**选择**: Redis 计数器 + TTL

**理由**:
- Redis 原子递增，线程安全
- 自动 TTL 过期
- 性能优秀，适合高频查询
- 支持分布式环境

**实现**:

```java
@Component
public class LoginAttemptService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String ATTEMPT_PREFIX = "login:attempt:";
    private static final String LOCK_PREFIX = "login:lock:";
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION = 30 * 60 * 1000; // 30分钟

    /**
     * 记录登录失败
     */
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

    /**
     * 记录登录成功，清除失败计数
     */
    public void loginSucceeded(String username) {
        redisTemplate.delete(ATTEMPT_PREFIX + username);
    }

    /**
     * 检查是否被锁定
     */
    public boolean isLocked(String username) {
        String key = LOCK_PREFIX + username;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 锁定用户
     */
    private void lockUser(String username) {
        String key = LOCK_PREFIX + username;
        redisTemplate.opsForValue().set(
            key,
            "1",
            LOCK_DURATION,
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * 获取剩余失败次数
     */
    public int getRemainingAttempts(String username) {
        String key = ATTEMPT_PREFIX + username;
        String value = redisTemplate.opsForValue().get(key);
        int attempts = value != null ? Integer.parseInt(value) : 0;
        return Math.max(0, MAX_ATTEMPTS - attempts);
    }
}
```

### 6. WebSocket 挤号通知

**选择**: Spring WebSocket + STOMP

**理由**:
- Spring 官方支持
- 支持 STOMP 协议，消息格式标准化
- 与 Spring Security 集成良好
- 支持广播和点对点消息

**实现**:

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }
}

@Controller
public class SessionController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 挤号登录：通知旧设备下线
     */
    public void notifyForceLogout(Long userId, String sessionId) {
        messagingTemplate.convertAndSend(
            "/topic/session/" + userId,
            Map.of(
                "type", "FORCE_LOGOUT",
                "sessionId", sessionId,
                "message", "您的账号在其他设备登录，您已被强制下线",
                "timestamp", System.currentTimeMillis()
            )
        );
    }
}
```

**前端监听**:

```javascript
import Stomp from 'stompjs'
import SockJS from 'sockjs-client'

const connectWebSocket = (userId) => {
  const socket = new SockJS('/ws')
  const stomp = Stomp.over(socket)

  stomp.connect({}, () => {
    stomp.subscribe(`/topic/session/${userId}`, (message) => {
      const data = JSON.parse(message.body)
      if (data.type === 'FORCE_LOGOUT') {
        notification.warning({
          message: '强制下线',
          description: data.message
        })
        // 跳转到登录页
        router.push('/login')
      }
    })
  })
}
```

## 性能优化

### 1. Token 验证优化

- Token 不查询数据库，直接从 Redis 黑名单验证
- JWT 解析和验证在内存中完成
- 黑名单查询时间 <10ms

### 2. 验证码优化

- 验证码哈希存储，不存储明文
- Redis 缓存，5分钟自动过期
- 生成响应时间 <100ms

### 3. 登录失败限制优化

- Redis 原子递增，无竞态条件
- 分布式锁支持集群部署
- 锁定状态自动过期

### 4. 密码哈希优化

- BCrypt strength=12，平衡安全性和性能
- 哈希时间约 200-300ms，可接受
- 异步处理避免阻塞请求

## 安全考虑

### 1. Token 安全

- Access Token 有效期 2 小时
- Refresh Token 有效期 7 天
- Token 包含用户 ID、租户 ID、权限列表
- 登出后立即加入黑名单

### 2. 密码安全

- BCrypt 加密，strength=12
- 密码策略：最少8位，包含字母和数字
- 不记录明文密码到日志
- 修改密码需要验证旧密码

### 3. 验证码安全

- 验证码哈希存储 (SHA-256)
- 使用后立即删除，防重放
- 5 分钟自动过期
- 连续失败 3 次后必填

### 4. 登录防护

- 连续失败 5 次锁定 30 分钟
- 记录登录失败 IP 和原因
- 不泄露用户是否存在
- 错误提示统一为"用户名或密码错误"

## 依赖配置

```xml
<dependencies>
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

    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
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
