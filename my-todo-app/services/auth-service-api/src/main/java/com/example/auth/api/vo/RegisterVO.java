package com.example.auth.api.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求 VO
 * <p>
 * 用户注册时提交的参数，包含用户名、密码、邮箱等信息。
 * 支持参数校验（用户名格式、密码长度、邮箱格式等）。
 * </p>
 */
@Data
public class RegisterVO {

    /** 用户名（必填，3-50位，字母开头，只允许字母数字下划线） */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50位之间")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "用户名必须以字母开头，只能包含字母、数字和下划线")
    private String userName;

    /** 密码（必填，8-100位） */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 100, message = "密码长度必须在8-100位之间")
    private String password;

    /** 确认密码（必填，需与密码一致） */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /** 邮箱（必填，需符合邮箱格式） */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 手机号（可选） */
    private String phone;

    /** 真实姓名（可选） */
    private String realName;

    /** 租户ID（多租户场景使用，可选） */
    private Long tenantId;
}
