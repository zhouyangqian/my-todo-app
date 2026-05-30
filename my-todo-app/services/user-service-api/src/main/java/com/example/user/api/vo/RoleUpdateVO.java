package com.example.user.api.vo;

import lombok.Data;

/**
 * 更新角色请求VO
 */
@Data
public class RoleUpdateVO {

    /** 角色名称 */
    private String roleName;

    /** 角色编码 */
    private String roleCode;

    /** 角色描述 */
    private String description;

    /** 状态：0-禁用，1-启用 */
    private Integer status;
}
