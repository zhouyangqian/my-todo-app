package com.example.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 刷新令牌实体类
 * <p>
 * 对应 refresh_token 表，存储刷新令牌的哈希值。
 * 不存储明文令牌，仅存储 SHA-256 哈希值以提高安全性。
 * 支持令牌轮换机制和令牌吊销。
 * </p>
 */
@Data
@TableName("refresh_token")
public class RefreshToken implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 令牌记录ID */
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 用户ID */
    private Long userId;

    /** 刷新令牌的 SHA-256 哈希值（不存储明文） */
    private String tokenHash;

    /** 关联的登录会话ID */
    private Long sessionId;

    /** 令牌过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime expireTime;

    /** 是否已吊销：0-有效，1-已吊销 */
    private Integer revoked;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
