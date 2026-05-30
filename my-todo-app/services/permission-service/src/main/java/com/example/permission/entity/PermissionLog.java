package com.example.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.example.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 权限审计日志实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission_log")
public class PermissionLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 被操作的用户ID */
    private Long userId;

    /** 资源标识 */
    private String resource;

    /** 操作类型 */
    private String action;

    /** 权限编码 */
    private String permission;

    /** 结果: 0=拒绝, 1=通过 */
    private Integer result;

    /** 原因 */
    private String reason;

    /** IP地址 */
    private String ipAddress;
}
