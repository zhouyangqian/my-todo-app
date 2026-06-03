package com.example.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 租户注册结果 DTO
 * <p>
 * 租户注册成功后返回给前端，包含新创建的租户ID和管理员账户信息，
 * 前端用此信息提示用户保存管理员凭据。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRegisterResultDTO {

    /** 新创建的租户ID */
    private Long tenantId;

    /** 自动生成的租户编码 */
    private String tenantCode;

    /** 管理员用户名 */
    private String adminUsername;

    /** 管理员密码（注册成功后展示给用户，建议立即修改） */
    private String adminPassword;
}
