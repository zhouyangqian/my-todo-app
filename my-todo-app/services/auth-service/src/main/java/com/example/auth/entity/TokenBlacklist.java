package com.example.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Token黑名单实体类
 * <p>
 * 对应 sys_token_blacklist 表，记录被拉黑的JWT令牌信息。
 * 配合Redis实现快速黑名单校验。
 * </p>
 */
@Data
@TableName("sys_token_blacklist")
public class TokenBlacklist implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Token前缀（前32字符） */
    private String tokenPrefix;

    /** 用户ID */
    private Long userId;

    /** 租户ID */
    private Long tenantId;

    /** 过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expiryTime;

    /** 拉黑原因：LOGOUT/REFRESH/FORCED */
    private String reason;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
