package com.example.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志实体类
 * <p>
 * 对应 login_log 表，记录每次登录尝试的详细信息，
 * 包括登录方式、是否成功、失败原因、IP地址和设备信息等。
 * </p>
 */
@Data
@TableName("login_log")
public class LoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日志ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 用户ID（登录失败时可能为空） */
    private Long userId;

    /** 登录用户名 */
    private String username;

    /** 登录方式：1-密码登录，2-短信验证码，3-第三方登录 */
    private Integer loginType;

    /** 登录状态：0-失败，1-成功 */
    private Integer loginStatus;

    /** 失败原因（仅登录失败时记录） */
    private String failReason;

    /** 登录IP地址 */
    private String ipAddress;

    /** 设备信息（User-Agent） */
    private String deviceInfo;

    /** 登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime loginTime;
}
