package com.example.permission.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 权限树节点DTO
 * <p>
 * 用于返回树形结构的权限数据，包含子节点列表
 * </p>
 */
@Data
public class PermissionTreeDTO {

    /** 权限ID */
    private Long id;

    /** 权限编码 */
    private String code;

    /** 权限名称 */
    private String name;

    /** 权限描述 */
    private String description;

    /** 父权限ID */
    private Long parentId;

    /** 层级 */
    private Integer level;

    /** 子权限列表 */
    private List<PermissionTreeDTO> children;
}
