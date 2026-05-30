package com.example.permission.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 黑名单响应DTO
 * <p>
 * 用于返回黑名单记录信息给前端
 * </p>
 */
@Data
public class BlacklistDTO {

    /** 记录ID */
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 被拉黑用户ID */
    private Long userId;

    /** 拉黑原因 */
    private String reason;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人类型 */
    private String operatorType;

    /** 状态：1=生效，0=已解除 */
    private Integer status;

    /** 加入黑名单时间 */
    private LocalDateTime addedAt;

    /** 解除黑名单时间 */
    private LocalDateTime removedAt;

    /** 解除操作人ID */
    private Long removedBy;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
