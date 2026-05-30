package com.example.user.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志DTO（响应输出）
 */
@Data
public class AuditLogDTO {

    private Long id;

    /** 操作人ID */
    private Long operatorId;

    /** 操作类型 */
    private String operationType;

    /** 目标类型 */
    private String targetType;

    /** 目标ID */
    private Long targetId;

    /** 操作结果：0-失败，1-成功 */
    private Integer result;

    /** IP地址 */
    private String ipAddress;

    /** 操作详情 */
    private String detail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
