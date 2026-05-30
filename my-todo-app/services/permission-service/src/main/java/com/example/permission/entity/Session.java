package com.example.permission.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话实体类
 * <p>
 * 对应数据库表 sys_session，用于记录用户登录会话信息。
 * 支持会话管理功能，包括在线用户查看、会话踢出、强制单会话等。
 * </p>
 */
@Data
@TableName("sys_session")
public class Session implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话主键ID，UUID自动生成 */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 用户ID */
    private Long userId;

    /** 租户ID */
    private Long tenantId;

    /** 设备信息 */
    private String deviceInfo;

    /** IP地址 */
    private String ipAddress;

    /** 浏览器User-Agent */
    private String userAgent;

    /** 会话状态：1=活跃，0=已踢出 */
    private Integer status;

    /** 登录时间 */
    private LocalDateTime loginTime;

    /** 最后活跃时间 */
    private LocalDateTime lastActive;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
