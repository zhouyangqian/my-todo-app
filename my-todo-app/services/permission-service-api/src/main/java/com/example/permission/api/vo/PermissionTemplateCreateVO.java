package com.example.permission.api.vo;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 创建/更新权限模板请求VO
 * <p>
 * 用于接收前端传递的权限模板参数
 * </p>
 */
@Data
public class PermissionTemplateCreateVO {

    /** 模板名称 */
    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    /** 模板编码 */
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    /** 模板描述 */
    private String description;

    /** 权限ID列表 */
    private List<Long> permissionIds;

    /** 状态：0=禁用，1=启用 */
    private Integer status;
}
