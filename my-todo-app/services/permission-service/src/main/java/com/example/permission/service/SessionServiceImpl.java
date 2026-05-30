package com.example.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.permission.entity.Session;
import com.example.permission.mapper.SessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 会话管理服务实现类
 * <p>
 * 提供会话的创建、踢出、查询、刷新和过期清理等功能。
 * 使用 Redis + 数据库双重存储：Redis 用于快速校验会话状态，数据库用于持久化。
 * </p>
 * <p>
 * Redis缓存策略：
 * - 会话缓存key格式：session:{sessionId}
 * - TTL：24小时
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionMapper sessionMapper;
    private final StringRedisTemplate redisTemplate;

    /** Redis会话key前缀 */
    private static final String SESSION_PREFIX = "session:";
    /** 会话TTL（小时） */
    private static final long SESSION_TTL_HOURS = 24;
    /** 会话活跃状态 */
    private static final int STATUS_ACTIVE = 1;
    /** 会话已踢出状态 */
    private static final int STATUS_KICKED = 0;

    /**
     * 创建会话（登录时调用）
     * <p>
     * 创建新的会话记录，同时写入Redis缓存。
     * </p>
     *
     * @param userId     用户ID
     * @param tenantId   租户ID
     * @param deviceInfo 设备信息
     * @param ipAddress  IP地址
     * @param userAgent  浏览器User-Agent
     * @return 创建的会话对象
     */
    @Override
    @Transactional
    public Session createSession(Long userId, Long tenantId, String deviceInfo, String ipAddress, String userAgent) {
        Session session = new Session();
        session.setUserId(userId);
        session.setTenantId(tenantId);
        session.setDeviceInfo(deviceInfo);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setStatus(STATUS_ACTIVE);
        session.setLoginTime(LocalDateTime.now());
        session.setLastActive(LocalDateTime.now());
        session.setCreatedAt(LocalDateTime.now());

        sessionMapper.insert(session);

        // 存入Redis（TTL 24小时）
        redisTemplate.opsForValue().set(SESSION_PREFIX + session.getId(), String.valueOf(userId),
                SESSION_TTL_HOURS, TimeUnit.HOURS);

        log.info("创建会话成功，用户ID: {}, 会话ID: {}", userId, session.getId());
        return session;
    }

    /**
     * 强制单会话：踢掉用户所有其他会话
     * <p>
     * 将指定用户除当前会话外的所有活跃会话标记为已踢出，
     * 并从Redis中删除对应的缓存记录。
     * </p>
     *
     * @param userId        用户ID
     * @param currentSessionId 当前会话ID（不会被踢出）
     */
    @Override
    @Transactional
    public void enforceSingleSession(Long userId, String currentSessionId) {
        List<Session> activeSessions = sessionMapper.selectList(
                new LambdaQueryWrapper<Session>()
                        .eq(Session::getUserId, userId)
                        .eq(Session::getStatus, STATUS_ACTIVE)
        );

        for (Session session : activeSessions) {
            if (!session.getId().equals(currentSessionId)) {
                // 标记为已踢出
                sessionMapper.update(null,
                        new LambdaUpdateWrapper<Session>()
                                .eq(Session::getId, session.getId())
                                .set(Session::getStatus, STATUS_KICKED)
                );
                // 从Redis删除
                redisTemplate.delete(SESSION_PREFIX + session.getId());
            }
        }
        log.info("强制单会话，踢出用户 {} 的旧会话，共 {} 个", userId, activeSessions.size() - 1);
    }

    /**
     * 踢出指定会话
     *
     * @param sessionId 会话ID
     */
    @Override
    @Transactional
    public void kickSession(String sessionId) {
        sessionMapper.update(null,
                new LambdaUpdateWrapper<Session>()
                        .eq(Session::getId, sessionId)
                        .set(Session::getStatus, STATUS_KICKED)
        );
        redisTemplate.delete(SESSION_PREFIX + sessionId);
        log.info("踢出会话: {}", sessionId);
    }

    /**
     * 踢出用户所有会话
     *
     * @param userId 用户ID
     */
    @Override
    @Transactional
    public void kickAllUserSessions(Long userId) {
        List<Session> sessions = sessionMapper.selectList(
                new LambdaQueryWrapper<Session>()
                        .eq(Session::getUserId, userId)
                        .eq(Session::getStatus, STATUS_ACTIVE)
        );

        // 批量标记为已踢出
        sessionMapper.update(null,
                new LambdaUpdateWrapper<Session>()
                        .eq(Session::getUserId, userId)
                        .eq(Session::getStatus, STATUS_ACTIVE)
                        .set(Session::getStatus, STATUS_KICKED)
        );

        // 批量清除Redis缓存
        for (Session session : sessions) {
            redisTemplate.delete(SESSION_PREFIX + session.getId());
        }
        log.info("踢出用户 {} 的所有会话，共 {} 个", userId, sessions.size());
    }

    /**
     * 获取在线用户列表
     *
     * @param tenantId 租户ID
     * @return 活跃会话列表
     */
    @Override
    public List<Session> getOnlineSessions(Long tenantId) {
        return sessionMapper.selectList(
                new LambdaQueryWrapper<Session>()
                        .eq(Session::getTenantId, tenantId)
                        .eq(Session::getStatus, STATUS_ACTIVE)
                        .orderByDesc(Session::getLastActive)
        );
    }

    /**
     * 刷新会话活跃时间
     * <p>
     * 更新数据库中的最后活跃时间，并重置Redis TTL
     * </p>
     *
     * @param sessionId 会话ID
     */
    @Override
    public void refreshSession(String sessionId) {
        sessionMapper.update(null,
                new LambdaUpdateWrapper<Session>()
                        .eq(Session::getId, sessionId)
                        .eq(Session::getStatus, STATUS_ACTIVE)
                        .set(Session::getLastActive, LocalDateTime.now())
        );
        // 重置Redis TTL
        redisTemplate.expire(SESSION_PREFIX + sessionId, SESSION_TTL_HOURS, TimeUnit.HOURS);
    }

    /**
     * 清理过期会话
     * <p>
     * 删除最后活跃时间超过24小时的会话记录
     * </p>
     */
    @Override
    @Transactional
    public void cleanExpiredSessions() {
        LocalDateTime expireTime = LocalDateTime.now().minusHours(SESSION_TTL_HOURS);

        // 先查询过期会话，用于清理Redis
        List<Session> expiredSessions = sessionMapper.selectList(
                new LambdaQueryWrapper<Session>()
                        .lt(Session::getLastActive, expireTime)
                        .eq(Session::getStatus, STATUS_ACTIVE)
        );

        // 清除Redis缓存
        for (Session session : expiredSessions) {
            redisTemplate.delete(SESSION_PREFIX + session.getId());
        }

        // 将过期会话标记为已踢出
        if (!expiredSessions.isEmpty()) {
            sessionMapper.update(null,
                    new LambdaUpdateWrapper<Session>()
                            .lt(Session::getLastActive, expireTime)
                            .eq(Session::getStatus, STATUS_ACTIVE)
                            .set(Session::getStatus, STATUS_KICKED)
            );
        }

        log.info("清理过期会话完成，共清理 {} 个", expiredSessions.size());
    }
}
