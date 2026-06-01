package com.example.user.api.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户角色分配请求VO
 */
@Data
public class UserRoleAssignVO {

    private Long userId;
    private List<Long> roleIds;
}
