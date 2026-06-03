package com.example.auth.service;

import com.example.auth.api.dto.LoginDTO;
import com.example.auth.api.dto.UserProfileDTO;
import com.example.auth.api.vo.ChangePasswordVO;
import com.example.auth.api.vo.LoginVO;
import com.example.auth.api.vo.RefreshTokenVO;
import com.example.auth.api.vo.RegisterVO;
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
import java.util.List;

/**
 * 认证服务业务逻辑层实现
 * <p>
 * 处理用户登录、注册、令牌刷新、退出登录、修改密码等核心认证逻辑。
 * 包含登录失败次数限制、账号锁定、会话管理、令牌哈希存储等安全机制。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final LoginSessionMapper loginSessionMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final LoginLogMapper loginLogMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;
    private final TokenBlacklistService tokenBlacklistService;
    private final SseService sseService;

    /** 最大登录失败次数，超过后锁定账号 */
    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    /** 账号锁定时长（分钟） */
    private static final int LOCK_DURATION_MINUTES = 30;

    @Override
    @Transactional
    public LoginDTO login(LoginVO request, String ipAddress) {
        // 根据用户名和租户ID查询用户（提前查询，用于判断登录失败次数）
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUserName, request.getUserName())
                .eq(User::getDeleted, 0);
        if (request.getTenantId() != null) {
            queryWrapper.eq(User::getTenantId, request.getTenantId());
        }
        User userForCaptchaCheck = userMapper.selectOne(queryWrapper);

        // 验证码强制校验：登录失败次数 >= 3 时必须提供验证码
        if (userForCaptchaCheck != null) {
            int failCount = userForCaptchaCheck.getLoginFailCount() != null ? userForCaptchaCheck.getLoginFailCount() : 0;
            if (failCount >= 3 && (request.getCaptchaKey() == null || request.getCaptchaKey().isEmpty()
                    || request.getCaptchaValue() == null || request.getCaptchaValue().isEmpty())) {
                throw new BusinessException(2001, "登录失败次数过多，验证码必填");
            }
        }

        // 验证码校验（如果前端传了captchaKey则必须验证）
        if (request.getCaptchaKey() != null && !request.getCaptchaKey().isEmpty()) {
            if (!captchaService.validateCaptcha(request.getCaptchaKey(), request.getCaptchaValue())) {
                throw new BusinessException(2001, "验证码错误或已过期");
            }
        }

        // 使用已查询到的用户（captchaCheck 阶段已查询）
        User user = userForCaptchaCheck;

        // 初始化登录日志
        LoginLog loginLog = new LoginLog();
        loginLog.setTenantId(user != null ? user.getTenantId() : 1L);
        loginLog.setUserId(user != null ? user.getId() : null);
        loginLog.setUserName(request.getUserName());
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
        if (!passwordEncoder.matches(request.getPassword(), user.getPassWord())) {
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
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUserName(), user.getTenantId());
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

        // 单会话模式：踢出该用户的旧会话，通过 SSE 通知旧设备
        List<LoginSession> activeSessions = loginSessionMapper.selectList(
            new LambdaQueryWrapper<LoginSession>()
                .eq(LoginSession::getUserId, user.getId())
                .eq(LoginSession::getStatus, 1)
                .ne(LoginSession::getId, session.getId())
        );
        if (!activeSessions.isEmpty()) {
            // 通过 SSE 通知旧设备被踢出
            sseService.sendKickNotification(user.getId(), "您的账号在其他设备上登录，已被迫下线");
            // 将旧会话标记为已登出
            for (LoginSession oldSession : activeSessions) {
                loginSessionMapper.update(null,
                    new LambdaUpdateWrapper<LoginSession>()
                        .eq(LoginSession::getId, oldSession.getId())
                        .set(LoginSession::getStatus, 0)
                        .set(LoginSession::getLogoutTime, LocalDateTime.now())
                );
            }
        }

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
        return LoginDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtProperties().getAccessTokenExpiration() / 1000)
                .userInfo(LoginDTO.UserInfo.builder()
                        .userId(user.getId())
                        .userName(user.getUserName())
                        .email(user.getEmail())
                        .realName(user.getRealName())
                        .avatar(user.getAvatar())
                        .tenantId(user.getTenantId())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public LoginDTO refreshToken(RefreshTokenVO request) {
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
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUserName(), user.getTenantId());
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

        return LoginDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtProperties().getAccessTokenExpiration() / 1000)
                .userInfo(LoginDTO.UserInfo.builder()
                        .userId(user.getId())
                        .userName(user.getUserName())
                        .email(user.getEmail())
                        .realName(user.getRealName())
                        .avatar(user.getAvatar())
                        .tenantId(user.getTenantId())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public void logout(String token) {
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return;
        }

        String tokenId = jwtTokenProvider.getTokenId(token);
        Long userId = jwtTokenProvider.getUserId(token);

        // 将Token加入黑名单
        tokenBlacklistService.blacklistToken(token, userId, "LOGOUT");

        // 将会话标记为已登出
        loginSessionMapper.update(null,
            new LambdaUpdateWrapper<LoginSession>()
                .eq(LoginSession::getTokenId, tokenId)
                .set(LoginSession::getStatus, 0)           // 0-已登出
                .set(LoginSession::getLogoutTime, LocalDateTime.now())
        );
    }

    @Override
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

    @Override
    @Transactional
    public Long register(RegisterVO request) {
        // 校验两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }

        Long tenantId = request.getTenantId() != null ? request.getTenantId() : 1L;

        // 检查用户名是否已存在
        User existingUser = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUserName, request.getUserName())
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
        user.setUserName(request.getUserName());
        user.setPassWord(passwordEncoder.encode(request.getPassword()));  // BCrypt 加密存储
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

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordVO request) {
        // 校验新密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(3001, "用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassWord())) {
            throw new BusinessException(400, "旧密码不正确");
        }

        // 更新密码
        user.setPassWord(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 修改密码后强制所有设备重新登录
        logoutAll(userId);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(3001, "用户不存在");
        }

        // 更新密码
        user.setPassWord(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setLoginFailCount(0);      // 重置登录失败次数
        user.setLocked(0);              // 解除锁定
        user.setLockedUntil(null);
        userMapper.updateById(user);

        // 强制所有设备重新登录
        logoutAll(userId);

        log.info("管理员重置用户密码: userId={}", userId);
    }

    @Override
    public UserProfileDTO getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(3001, "用户不存在");
        }

        return UserProfileDTO.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .tenantId(user.getTenantId())
                .roles(List.of())  // 角色由 permission-service 管理，此处返回空列表
                .createdAt(user.getCreatedAt())
                .build();
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
