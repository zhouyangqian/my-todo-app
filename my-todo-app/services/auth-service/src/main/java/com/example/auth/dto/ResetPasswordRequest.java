package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员重置密码请求 DTO
 */
@Data
public class ResetPasswordRequest {

    /** 用户ID（必填） */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 新密码（必填） */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
