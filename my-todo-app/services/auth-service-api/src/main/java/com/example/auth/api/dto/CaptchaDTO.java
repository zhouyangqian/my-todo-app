package com.example.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应 DTO
 * <p>
 * 返回给前端的验证码信息，包含验证码Key和Base64编码的图片。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaDTO {

    /** 验证码Key（UUID），登录时需要传回 */
    private String captchaKey;

    /** 验证码图片（Base64编码） */
    private String captchaImage;
}
