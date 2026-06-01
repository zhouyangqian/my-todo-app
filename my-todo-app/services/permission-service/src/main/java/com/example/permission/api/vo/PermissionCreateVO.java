package com.example.permission.api.vo;

import lombok.Data;

@Data
public class PermissionCreateVO {

    private String code;

    private String name;

    private Integer permissionType;

    private String description;

    private Long parentId;

    private Integer level;
}
