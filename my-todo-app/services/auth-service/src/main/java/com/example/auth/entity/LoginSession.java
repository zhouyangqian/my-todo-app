package com.example.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录会话实体类
 * <p>
 * 对应 login_session 表，记录每次成功登录后创建的会话。
 * 通过 tokenId 与 JWT 令牌关联，支持会话管理（单设备登录、踢人下线等）。
 * </p>
 */
@Data
@TableName("login_session")
public class LoginSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID */
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 用户ID */
    private Long userId;

    /** JWT 令牌唯一ID（jti），用于关联和吊销令牌 */
    private String tokenId;

    /** 设备类型：web、mobile、desktop */
    private String deviceType;

    /** 设备信息（User-Agent） */
    private String deviceInfo;

    /** 登录IP地址 */
    private String ipAddress;

    /** 登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime loginTime;

    /** 令牌过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime expireTime;

    /** 退出登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime logoutTime;

    /** 会话状态：0-已登出，1-在线 */
    private Integer status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
