package com.example.permission.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class PermissionTreeDTO {

    private Long id;
    private String code;
    private String name;
    private Long parentId;
    private Integer level;
    private String description;
    private List<PermissionTreeDTO> children;
}
