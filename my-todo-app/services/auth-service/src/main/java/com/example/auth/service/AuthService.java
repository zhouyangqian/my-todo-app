package com.example.auth.service;

import com.example.auth.api.dto.LoginDTO;
import com.example.auth.api.dto.UserProfileDTO;
import com.example.auth.api.vo.ChangePasswordVO;
import com.example.auth.api.vo.LoginVO;
import com.example.auth.api.vo.RefreshTokenVO;
import com.example.auth.api.vo.RegisterVO;

/**
 * 认证服务接口
 * <p>
 * 处理用户登录、注册、令牌刷新、退出登录、修改密码等核心认证逻辑。
 * </p>
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request   登录请求
     * @param ipAddress 客户端IP地址
     * @return 登录响应（令牌和用户信息）
     */
    LoginDTO login(LoginVO request, String ipAddress);

    /**
     * 刷新访问令牌
     *
     * @param request 刷新令牌请求
     * @return 新的登录响应
     */
    LoginDTO refreshToken(RefreshTokenVO request);

    /**
     * 退出登录（当前设备）
     *
     * @param token 访问令牌
     */
    void logout(String token);

    /**
     * 退出所有设备的登录
     *
     * @param userId 用户ID
     */
    void logoutAll(Long userId);

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 新创建的用户ID
     */
    Long register(RegisterVO request);

    /**
     * 修改密码
     *
     * @param userId  用户ID
     * @param request 修改密码请求
     */
    void changePassword(Long userId, ChangePasswordVO request);

    /**
     * 管理员重置用户密码
     *
     * @param userId      目标用户ID
     * @param newPassword 新密码（明文）
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 获取用户档案信息
     *
     * @param userId 用户ID
     * @return 用户档案 DTO
     */
    UserProfileDTO getUserProfile(Long userId);
}
