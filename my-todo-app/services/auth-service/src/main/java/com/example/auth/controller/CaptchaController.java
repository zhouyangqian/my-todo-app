package com.example.auth.controller;

import com.example.auth.api.dto.CaptchaDTO;
import com.example.auth.service.CaptchaService;
import com.example.common.core.result.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码控制器
 * <p>
 * 提供验证码生成接口，登录页面加载时调用。
 * </p>
 */
@Tag(name = "验证码", description = "验证码生成接口")
@RestController
@RequestMapping("/api/auth/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    /**
     * 获取验证码
     * <p>返回验证码Key和Base64编码的验证码图片</p>
     *
     * @return 验证码DTO
     */
    @Operation(summary = "获取验证码")
    @GetMapping
    public ApiResponse<CaptchaDTO> getCaptcha() {
        CaptchaDTO captchaDTO = captchaService.generateCaptcha();
        return ApiResponse.success(captchaDTO);
    }
}
