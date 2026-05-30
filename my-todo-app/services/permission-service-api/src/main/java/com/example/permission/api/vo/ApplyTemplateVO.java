package com.example.permission.api.vo;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 应用模板到角色请求VO
 * <p>
 * 用于将权限模板的权限列表应用到指定角色
 * </p>
 */
@Data
public class ApplyTemplateVO {

    /** 模板ID */
    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    /** 角色ID */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}
