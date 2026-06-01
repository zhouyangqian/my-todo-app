package com.example.permission.api.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoleMenuAssignVO {

    private Long roleId;

    private List<Long> menuIds;
}
