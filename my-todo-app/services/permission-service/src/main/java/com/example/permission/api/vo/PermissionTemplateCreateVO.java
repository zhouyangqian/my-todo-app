package com.example.permission.api.vo;

import lombok.Data;

import java.util.List;

@Data
public class PermissionTemplateCreateVO {

    private String templateName;

    private String templateCode;

    private String description;

    private List<Long> permissionIds;

    private Integer status;
}
