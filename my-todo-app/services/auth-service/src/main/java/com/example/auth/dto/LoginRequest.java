package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求 DTO
 * <p>
 * 用户登录时提交的参数，包含用户名、密码、验证码、设备信息和租户ID。
 * </p>
 */
@Data
public class LoginRequest {

    /** 用户名（必填，3-50位） */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50位之间")
    private String username;

    /** 密码（必填） */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码缓存key（可选，开启验证码时使用） */
    private String captchaKey;

    /** 验证码值（可选，开启验证码时使用） */
    private String captchaValue;

    /** 设备类型：web、mobile、desktop，默认 web */
    private String deviceType = "web";

    /** 设备信息（User-Agent） */
    private String deviceInfo;

    /** 租户ID（多租户场景使用） */
    private Long tenantId;
}
