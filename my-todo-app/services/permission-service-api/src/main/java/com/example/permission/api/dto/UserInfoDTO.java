package com.example.permission.api.dto;

import lombok.Data;

/**
 * 用户基本信息DTO
 * <p>
 * 包含用户的基本信息，不包含敏感数据如密码
 * </p>
 */
@Data
public class UserInfoDTO {

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 真实姓名 */
    private String realName;

    /** 头像URL */
    private String avatar;

    /** 租户ID */
    private Long tenantId;
}
