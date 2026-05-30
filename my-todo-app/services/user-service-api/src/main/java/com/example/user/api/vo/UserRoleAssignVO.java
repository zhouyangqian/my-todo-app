package com.example.user.api.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 用户角色分配请求VO
 */
@Data
public class UserRoleAssignVO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotEmpty(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}
