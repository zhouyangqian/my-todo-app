package com.example.user.api.vo;

import lombok.Data;

/**
 * 更新角色请求VO
 */
@Data
public class RoleUpdateVO {

    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;
}
