package com.example.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 租户信息响应 DTO
 * <p>
 * 返回给前端的租户基本信息。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDTO {

    /** 租户ID */
    private Long id;

    /** 租户名称 */
    private String tenantName;

    /** 租户编码 */
    private String tenantCode;

    /** 状态：0-禁用，1-正常，2-过期 */
    private Integer status;

    /** 用户数量限制 */
    private Integer userLimit;

    /** 联系人 */
    private String contactName;

    /** 联系邮箱 */
    private String contactEmail;

    /** 联系电话 */
    private String contactPhone;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
