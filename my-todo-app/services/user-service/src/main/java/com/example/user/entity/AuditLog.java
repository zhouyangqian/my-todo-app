package com.example.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审计日志实体类
 * <p>
 * 对应数据库表 sys_audit_log，记录系统中的关键操作日志，
 * 包括操作人、操作类型、目标对象、操作结果、IP地址等。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_audit_log")
public class AuditLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 操作人ID */
    private Long operatorId;

    /** 操作类型（如 CREATE、UPDATE、DELETE、ASSIGN_ROLE 等） */
    private String operationType;

    /** 目标类型（如 USER、ROLE、PERMISSION 等） */
    private String targetType;

    /** 目标对象ID */
    private Long targetId;

    /** 操作结果：0-失败，1-成功 */
    private Integer result;

    /** 操作人IP地址 */
    private String ipAddress;

    /** 操作详情 */
    private String detail;
}
