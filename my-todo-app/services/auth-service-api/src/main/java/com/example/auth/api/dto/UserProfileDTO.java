package com.example.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户档案信息 DTO
 * <p>
 * 用户档案接口返回的详细信息，包含基本信息、角色列表等。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String userName;

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

    /** 角色编码列表 */
    private List<String> roles;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
