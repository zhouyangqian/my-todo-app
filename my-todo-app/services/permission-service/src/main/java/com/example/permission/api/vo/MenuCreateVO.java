package com.example.permission.api.vo;

import lombok.Data;

@Data
public class MenuCreateVO {

    private Long parentId;

    private String menuName;

    private Integer menuType;

    private String path;

    private String component;

    private String permissionCode;

    private String icon;

    private Integer sortOrder;

    private Integer visible;
}
