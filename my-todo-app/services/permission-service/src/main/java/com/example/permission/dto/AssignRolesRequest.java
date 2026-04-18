package com.example.permission.dto;

import lombok.Data;

import java.util.List;

/**
 * 分配角色请求DTO
 * <p>
 * 用于给用户分配角色时接收前端传递的参数，
 * 包含目标用户ID和待分配的角色ID列表
 * </p>
 */
@Data
public class AssignRolesRequest {

    /** 待分配角色的目标用户ID */
    private Long userId;

    /** 待分配的角色ID列表 */
    private List<Long> roleIds;
}
