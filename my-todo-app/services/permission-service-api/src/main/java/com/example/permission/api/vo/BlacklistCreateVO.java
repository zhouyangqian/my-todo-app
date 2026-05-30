package com.example.permission.api.vo;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 加入黑名单请求VO
 * <p>
 * 用于接收前端传递的拉黑用户参数
 * </p>
 */
@Data
public class BlacklistCreateVO {

    /** 被拉黑用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 拉黑原因 */
    private String reason;

    /** 操作人类型（TENANT_ADMIN/SYSTEM_ADMIN），默认TENANT_ADMIN */
    private String operatorType;
}
