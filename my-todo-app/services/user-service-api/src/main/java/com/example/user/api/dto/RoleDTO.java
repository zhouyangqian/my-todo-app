package com.example.user.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色信息DTO（响应输出）
 */
@Data
public class RoleDTO {

    private Long id;

    private Long tenantId;

    /** 角色名称 */
    private String roleName;

    /** 角色编码 */
    private String roleCode;

    /** 角色描述 */
    private String description;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 是否系统角色 */
    private Integer isSystem;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
