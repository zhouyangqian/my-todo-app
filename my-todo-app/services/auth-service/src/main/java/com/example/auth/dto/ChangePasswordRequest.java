package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求 DTO
 * <p>
 * 用户修改密码时提交的参数，需提供旧密码、新密码和确认密码。
 * </p>
 */
@Data
public class ChangePasswordRequest {

    /** 旧密码（必填） */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /** 新密码（必填） */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    /** 确认新密码（必填，需与新密码一致） */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
