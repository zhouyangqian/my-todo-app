package com.example.permission.service;

import com.example.permission.entity.Session;

import java.util.List;

/**
 * 会话管理服务接口
 */
public interface SessionService {

    Session createSession(Long userId, Long tenantId, String deviceInfo, String ipAddress, String userAgent);

    void enforceSingleSession(Long userId, String currentSessionId);

    void kickSession(String sessionId);

    void kickAllUserSessions(Long userId);

    List<Session> getOnlineSessions(Long tenantId);

    void refreshSession(String sessionId);

    void cleanExpiredSessions();
}
