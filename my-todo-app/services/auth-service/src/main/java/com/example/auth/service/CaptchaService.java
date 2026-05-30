package com.example.auth.service;

import com.example.auth.api.dto.CaptchaDTO;

/**
 * 验证码服务接口
 * <p>
 * 生成图形验证码，验证码值存储在Redis中。
 * </p>
 */
public interface CaptchaService {

    /**
     * 生成验证码
     *
     * @return 验证码DTO（包含captchaKey和base64图片）
     */
    CaptchaDTO generateCaptcha();

    /**
     * 校验验证码
     *
     * @param captchaKey   验证码Key
     * @param captchaValue 用户输入的验证码值
     * @return true-验证通过
     */
    boolean validateCaptcha(String captchaKey, String captchaValue);
}
