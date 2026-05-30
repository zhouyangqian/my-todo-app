package com.example.user.api.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量操作请求VO
 */
@Data
public class BatchOperationVO {

    /** 用户ID列表 */
    @NotEmpty(message = "用户列表不能为空")
    @Size(max = 100, message = "单次操作不能超过100个用户")
    private List<Long> userIds;

    /** 角色ID列表（仅批量分配角色使用） */
    private List<Long> roleIds;
}
