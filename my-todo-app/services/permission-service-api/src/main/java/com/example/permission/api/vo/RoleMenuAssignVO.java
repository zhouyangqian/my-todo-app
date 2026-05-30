package com.example.permission.api.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 分配菜单给角色请求 VO
 */
@Data
public class RoleMenuAssignVO {

    /** 角色ID */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /** 菜单ID列表 */
    private List<Long> menuIds;
}
