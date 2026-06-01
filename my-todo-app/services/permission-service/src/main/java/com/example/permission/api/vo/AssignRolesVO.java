package com.example.permission.api.vo;

import lombok.Data;

import java.util.List;

@Data
public class AssignRolesVO {

    private Long userId;

    private List<Long> roleIds;
}
