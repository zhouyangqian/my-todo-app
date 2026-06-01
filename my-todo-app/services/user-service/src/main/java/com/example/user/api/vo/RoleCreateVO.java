package com.example.user.api.vo;

import lombok.Data;

/**
 * 创建角色请求VO
 */
@Data
public class RoleCreateVO {

    private String roleName;
    private String roleCode;
    private String description;
}
