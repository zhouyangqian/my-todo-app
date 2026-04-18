package com.example.auth.service;

import com.example.auth.dto.*;
import com.example.auth.entity.LoginLog;
import com.example.auth.entity.LoginSession;
import com.example.auth.entity.RefreshToken;
import com.example.auth.entity.User;
import com.example.auth.mapper.LoginLogMapper;
import com.example.auth.mapper.LoginSessionMapper;
import com.example.auth.mapper.RefreshTokenMapper;
import com.example.auth.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.common.core.exception.BusinessException;
import com.example.common.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 认证服务业务逻辑层
 * <p>
 * 处理用户登录、注册、令牌刷新、退出登录、修改密码等核心认证逻辑。
 * 包含登录失败次数限制、账号锁定、会话管理、令牌哈希存储等安全机制。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final LoginSessionMapper loginSessionMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final LoginLogMapper loginLogMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /** 最大登录失败次数，超过后锁定账号 */
    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    /** 账号锁定时长（分钟） */
    private static final int LOCK_DURATION_MINUTES = 30;

    /**
     * 用户登录
     * <p>
     * 完整的登录流程：
     * 1. 根据用户名和租户ID查询用户
     * 2. 检查账号状态（禁用/锁定）
     * 3. 验证密码
     * 4. 处理登录失败计数和账号锁定
     * 5. 生成访问令牌和刷新令牌
     * 6. 创建登录会话和存储刷新令牌
     * 7. 记录登录日志
     * </p>
     *
     * @param request   登录请求
     * @param ipAddress 客户端IP地址
     * @return 登录响应（令牌和用户信息）
     */
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        // 根据用户名和租户ID查询用户
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .eq(User::getTenantId, request.getTenantId() != null ? request.getTenantId() : 1L)
        );

        // 初始化登录日志
        LoginLog loginLog = new LoginLog();
        loginLog.setTenantId(user != null ? user.getTenantId() : 1L);
        loginLog.setUserId(user != null ? user.getId() : null);
        loginLog.setUsername(request.getUsername());
        loginLog.setLoginType(1); // 1-密码登录
        loginLog.setIpAddress(ipAddress);
        loginLog.setDeviceInfo(request.getDeviceInfo());
        loginLog.setLoginTime(LocalDateTime.now());

        // 用户不存在
        if (user == null) {
            loginLog.setLoginStatus(0);
            loginLog.setFailReason("用户不存在");
            loginLogMapper.insert(loginLog);
            throw new BusinessException(2001, "用户名或密码错误");
        }

        // 检查账号是否被禁用
        if (user.getStatus() == 0) {
            loginLog.setLoginStatus(0);
            loginLog.setFailReason("账号已禁用");
            loginLogMapper.insert(loginLog);
            throw new BusinessException(2005, "账号已被禁用");
        }

        // 检查账号是否被锁定（锁定时间未过期）
        if (user.getLocked() == 1 && user.getLockedUntil() != null
                && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            loginLog.setLoginStatus(0);
            loginLog.setFailReason("账号已锁定");
            loginLogMapper.insert(loginLog);
            throw new BusinessException(2004, "账号已锁定，请稍后重试");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // 累加登录失败次数
            int failCount = (user.getLoginFailCount() != null ? user.getLoginFailCount() : 0) + 1;
            user.setLoginFailCount(failCount);

            // 超过最大失败次数，锁定账号
            if (failCount >= MAX_LOGIN_FAIL_COUNT) {
                user.setLocked(1);
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            }
            userMapper.updateById(user);

            loginLog.setLoginStatus(0);
            loginLog.setFailReason("密码错误");
            loginLogMapper.insert(loginLog);
            throw new BusinessException(2001, "用户名或密码错误");
        }

        // 登录成功，解锁账号（如果之前被锁定且已过锁定时间）
        if (user.getLocked() == 1) {
            user.setLocked(0);
            user.setLockedUntil(null);
        }
        user.setLoginFailCount(0);  // 重置失败次数
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        userMapper.updateById(user);

        // 生成访问令牌和刷新令牌
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getTenantId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getTenantId());

        // 创建登录会话记录
        LoginSession session = new LoginSession();
        session.setTenantId(user.getTenantId());
        session.setUserId(user.getId());
        session.setTokenId(jwtTokenProvider.getTokenId(accessToken));
        session.setDeviceType(request.getDeviceType());
        session.setDeviceInfo(request.getDeviceInfo());
        session.setIpAddress(ipAddress);
        session.setLoginTime(LocalDateTime.now());
        session.setExpireTime(LocalDateTime.now().plusSeconds(jwtTokenProvider.getJwtProperties().getAccessTokenExpiration() / 1000));
        session.setStatus(1);  // 1-在线
        loginSessionMapper.insert(session);

        // 存储刷新令牌的哈希值（不存储明文令牌）
        RefreshToken token = new RefreshToken();
        token.setTenantId(user.getTenantId());
        token.setUserId(user.getId());
        token.setTokenHash(hashToken(refreshToken));
        token.setSessionId(session.getId());
        token.setExpireTime(LocalDateTime.now().plusSeconds(jwtTokenProvider.getJwtProperties().getRefreshTokenExpiration() / 1000));
        token.setRevoked(0);  // 0-未吊销
        refreshTokenMapper.insert(token);

        // 记录登录成功日志
        loginLog.setLoginStatus(1);
        loginLogMapper.insert(loginLog);

        // 构造登录响应
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtProperties().getAccessTokenExpiration() / 1000)
                .userInfo(LoginResponse.UserInfo.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .realName(user.getRealName())
                        .avatar(user.getAvatar())
                        .tenantId(user.getTenantId())
                        .build())
                .build();
    }

    /**
     * 刷新访问令牌
     * <p>
     * 使用刷新令牌换取新的访问令牌和刷新令牌（令牌轮换机制）。
     * 旧刷新令牌会被吊销，防止重放攻击。
     * </p>
     *
     * @param request 刷新令牌请求
     * @return 新的登录响应
     */
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        // 验证刷新令牌格式
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new BusinessException(2009, "无效的刷新令牌");
        }

        // 确认是刷新令牌类型
        if (!jwtTokenProvider.isRefreshToken(request.getRefreshToken())) {
            throw new BusinessException(2009, "令牌类型错误，需要刷新令牌");
        }

        Long userId = jwtTokenProvider.getUserId(request.getRefreshToken());
        Long tenantId = jwtTokenProvider.getTenantId(request.getRefreshToken());
        String tokenHash = hashToken(request.getRefreshToken());

        // 查找令牌记录，确认未被吊销
        RefreshToken storedToken = refreshTokenMapper.selectOne(
            new LambdaQueryWrapper<RefreshToken>()
                .eq(RefreshToken::getTokenHash, tokenHash)
                .eq(RefreshToken::getRevoked, 0)
        );

        if (storedToken == null) {
            throw new BusinessException(2009, "刷新令牌不存在或已吊销");
        }

        // 检查令牌是否过期
        if (storedToken.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(2002, "刷新令牌已过期");
        }

        // 检查用户状态
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(2005, "用户不存在或已禁用");
        }

        // 生成新的令牌对
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getTenantId());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getTenantId());

        // 吊销旧刷新令牌（令牌轮换）
        storedToken.setRevoked(1);
        refreshTokenMapper.updateById(storedToken);

        // 存储新刷新令牌
        RefreshToken newToken = new RefreshToken();
        newToken.setTenantId(user.getTenantId());
        newToken.setUserId(user.getId());
        newToken.setTokenHash(hashToken(newRefreshToken));
        newToken.setExpireTime(LocalDateTime.now().plusSeconds(jwtTokenProvider.getJwtProperties().getRefreshTokenExpiration() / 1000));
        newToken.setRevoked(0);
        refreshTokenMapper.insert(newToken);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtProperties().getAccessTokenExpiration() / 1000)
                .userInfo(LoginResponse.UserInfo.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .realName(user.getRealName())
                        .avatar(user.getAvatar())
                        .tenantId(user.getTenantId())
                        .build())
                .build();
    }

    /**
     * 退出登录（当前设备）
     * <p>使当前令牌对应的会话失效</p>
     *
     * @param token 访问令牌
     */
    @Transactional
    public void logout(String token) {
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return;
        }

        String tokenId = jwtTokenProvider.getTokenId(token);

        // 将会话标记为已登出
        loginSessionMapper.update(null,
            new LambdaUpdateWrapper<LoginSession>()
                .eq(LoginSession::getTokenId, tokenId)
                .set(LoginSession::getStatus, 0)           // 0-已登出
                .set(LoginSession::getLogoutTime, LocalDateTime.now())
        );
    }

    /**
     * 退出所有设备的登录
     * <p>使该用户所有活跃会话和刷新令牌失效</p>
     *
     * @param userId 用户ID
     */
    @Transactional
    public void logoutAll(Long userId) {
        // 使所有活跃会话失效
        loginSessionMapper.update(null,
            new LambdaUpdateWrapper<LoginSession>()
                .eq(LoginSession::getUserId, userId)
                .eq(LoginSession::getStatus, 1)             // 1-在线
                .set(LoginSession::getStatus, 0)            // 设为已登出
                .set(LoginSession::getLogoutTime, LocalDateTime.now())
        );

        // 吊销所有未吊销的刷新令牌
        refreshTokenMapper.update(null,
            new LambdaUpdateWrapper<RefreshToken>()
                .eq(RefreshToken::getUserId, userId)
                .eq(RefreshToken::getRevoked, 0)            // 0-未吊销
                .set(RefreshToken::getRevoked, 1)           // 设为已吊销
        );
    }

    /**
     * 用户注册
     * <p>
     * 创建新用户，包括用户名和邮箱唯一性校验、密码加密存储。
     * </p>
     *
     * @param request 注册请求
     * @return 新创建的用户ID
     */
    @Transactional
    public Long register(RegisterRequest request) {
        // 校验两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }

        Long tenantId = request.getTenantId() != null ? request.getTenantId() : 1L;

        // 检查用户名是否已存在
        User existingUser = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .eq(User::getTenantId, tenantId)
        );
        if (existingUser != null) {
            throw new BusinessException(3002, "用户名已存在");
        }

        // 检查邮箱是否已存在
        existingUser = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail())
                .eq(User::getTenantId, tenantId)
        );
        if (existingUser != null) {
            throw new BusinessException(3003, "邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setTenantId(tenantId);
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));  // BCrypt 加密存储
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setStatus(1);             // 1-正常
        user.setLocked(0);             // 0-未锁定
        user.setLoginFailCount(0);     // 登录失败次数清零
        user.setPasswordChangedAt(LocalDateTime.now());
        userMapper.insert(user);

        return user.getId();
    }

    /**
     * 修改密码
     * <p>
     * 验证旧密码后更新新密码，修改成功后强制所有设备重新登录。
     * </p>
     *
     * @param userId  用户ID
     * @param request 修改密码请求
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // 校验新密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(3001, "用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "旧密码不正确");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 修改密码后强制所有设备重新登录
        logoutAll(userId);
    }

    /**
     * 使用 SHA-256 对令牌进行哈希
     * <p>存储令牌的哈希值而非明文，提高安全性</p>
     *
     * @param token 原始令牌
     * @return SHA-256 哈希后的十六进制字符串
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("令牌哈希计算失败", e);
        }
    }
}
