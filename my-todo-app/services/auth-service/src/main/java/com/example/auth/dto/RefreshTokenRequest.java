package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新令牌请求 DTO
 * <p>
 * 使用刷新令牌换取新的访问令牌时提交的参数。
 * </p>
 */
@Data
public class RefreshTokenRequest {

    /** 刷新令牌（必填） */
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
