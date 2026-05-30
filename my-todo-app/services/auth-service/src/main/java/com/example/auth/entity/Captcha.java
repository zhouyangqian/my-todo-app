package com.example.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 验证码实体类
 * <p>
 * 对应 sys_captcha 表，存储验证码的SHA-256哈希值。
 * 实际验证码值存储在Redis中，数据库仅存哈希用于审计。
 * </p>
 */
@Data
@TableName("sys_captcha")
public class Captcha implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 验证码Key（UUID） */
    private String captchaKey;

    /** 验证码值的SHA-256哈希 */
    private String codeHash;

    /** 用户ID（可选，登录时关联） */
    private Long userId;

    /** 过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expiryTime;

    /** 是否已使用：0-未使用，1-已使用 */
    private Integer used;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
