package com.example.user.api.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建用户请求VO
 */
@Data
public class UserCreateVO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度6-100")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    private String realName;

    private Long tenantId;
}
