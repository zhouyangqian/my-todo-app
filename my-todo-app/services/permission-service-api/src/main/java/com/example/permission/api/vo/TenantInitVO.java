package com.example.permission.api.vo;

import lombok.Data;

/**
 * 租户权限初始化请求 VO
 * <p>
 * 租户注册成功后，auth-service 通过内部接口调用 permission-service，
 * 为新租户初始化默认角色和权限，并将管理员用户绑定到管理员角色。
 * </p>
 */
@Data
public class TenantInitVO {

    /** 新注册的租户ID */
    private Long tenantId;

    /** 管理员用户ID */
    private Long adminUserId;

    /** 管理员用户名 */
    private String adminUsername;
}
