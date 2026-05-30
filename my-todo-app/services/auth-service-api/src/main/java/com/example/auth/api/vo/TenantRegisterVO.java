package com.example.auth.api.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 租户注册请求 VO
 * <p>
 * 租户自助注册时提交的参数，包含租户信息和管理员账户信息。
 * </p>
 */
@Data
public class TenantRegisterVO {

    /** 租户名称 */
    @NotBlank(message = "租户名称不能为空")
    @Size(min = 2, max = 128, message = "租户名称长度必须在2-128位之间")
    private String tenantName;

    /** 租户编码（唯一标识） */
    @NotBlank(message = "租户编码不能为空")
    @Size(min = 3, max = 64, message = "租户编码长度必须在3-64位之间")
    private String tenantCode;

    /** 联系人 */
    private String contactName;

    /** 联系邮箱 */
    @Email(message = "联系邮箱格式不正确")
    private String contactEmail;

    /** 联系电话 */
    private String contactPhone;

    /** 管理员用户名 */
    @NotBlank(message = "管理员用户名不能为空")
    @Size(min = 3, max = 50, message = "管理员用户名长度必须在3-50位之间")
    private String adminUsername;

    /** 管理员密码 */
    @NotBlank(message = "管理员密码不能为空")
    @Size(min = 8, max = 100, message = "管理员密码长度必须在8-100位之间")
    private String adminPassword;

    /** 管理员邮箱 */
    @Email(message = "管理员邮箱格式不正确")
    private String adminEmail;
}
