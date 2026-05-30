package com.example.permission.api.dto;

import lombok.Data;

/**
 * 角色继承关系DTO
 * <p>
 * 用于返回角色继承关系数据，包含父角色的基本信息
 * </p>
 */
@Data
public class RoleInheritanceDTO {

    /** 子角色ID */
    private Long childRoleId;

    /** 父角色ID */
    private Long parentRoleId;

    /** 父角色名称 */
    private String parentRoleName;
}
