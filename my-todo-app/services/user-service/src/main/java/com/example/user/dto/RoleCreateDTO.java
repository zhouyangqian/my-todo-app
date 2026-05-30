package com.example.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建角色请求DTO
 */
@Data
public class RoleCreateDTO {

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    /** 角色描述 */
    private String description;
}
