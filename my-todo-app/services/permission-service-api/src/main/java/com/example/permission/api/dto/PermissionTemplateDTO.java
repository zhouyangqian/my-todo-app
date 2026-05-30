package com.example.permission.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限模板响应DTO
 * <p>
 * 用于返回权限模板信息给前端，包含权限ID列表（已解析为List）
 * </p>
 */
@Data
public class PermissionTemplateDTO {

    /** 模板ID */
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 模板名称 */
    private String templateName;

    /** 模板编码 */
    private String templateCode;

    /** 模板描述 */
    private String description;

    /** 是否系统预设：0=否，1=是 */
    private Integer isSystem;

    /** 权限ID列表（已解析） */
    private List<Long> permissionIds;

    /** 状态：0=禁用，1=启用 */
    private Integer status;

    /** 创建人ID */
    private Long createdBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新人ID */
    private Long updatedBy;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
