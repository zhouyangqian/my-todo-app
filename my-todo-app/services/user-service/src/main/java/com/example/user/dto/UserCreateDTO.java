package com.example.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建用户请求DTO
 */
@Data
public class UserCreateDTO {

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

    private Long departmentId;

    /** 分配的角色ID列表（可选，创建时同时分配角色） */
    private List<Long> roleIdList;
}
