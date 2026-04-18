package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 * <p>
 * 登录成功后返回给前端的数据，包含访问令牌、刷新令牌和用户基本信息。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** 访问令牌（用于接口调用） */
    private String accessToken;

    /** 刷新令牌（用于刷新访问令牌） */
    private String refreshToken;

    /** 令牌类型，默认 Bearer */
    private String tokenType = "Bearer";

    /** 访问令牌有效期（秒） */
    private Long expiresIn;

    /** 用户基本信息 */
    private UserInfo userInfo;

    /**
     * 用户信息内嵌类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        /** 用户ID */
        private Long userId;
        /** 用户名 */
        private String username;
        /** 邮箱 */
        private String email;
        /** 真实姓名 */
        private String realName;
        /** 头像URL */
        private String avatar;
        /** 租户ID */
        private Long tenantId;
    }
}
