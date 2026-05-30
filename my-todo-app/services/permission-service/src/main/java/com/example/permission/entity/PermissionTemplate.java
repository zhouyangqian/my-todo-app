package com.example.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限模板实体类
 * <p>
 * 对应数据库表 sys_permission_template，用于存储权限模板信息。
 * 支持系统预设模板和租户自定义模板，可以将模板权限快速应用到角色。
 * permissionIds 字段使用 JSON 数组格式存储权限ID列表，如 [1,2,3]。
 * </p>
 */
@Data
@TableName("sys_permission_template")
public class PermissionTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID，自增长 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID，0表示系统预设 */
    private Long tenantId;

    /** 模板名称 */
    private String templateName;

    /** 模板编码，租户内唯一 */
    private String templateCode;

    /** 模板描述 */
    private String description;

    /** 是否系统预设：0=否，1=是 */
    private Integer isSystem;

    /** 权限ID列表（JSON数组格式，如 [1,2,3]） */
    private String permissionIds;

    /** 状态：0=禁用，1=启用 */
    private Integer status;

    /** 逻辑删除标志：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建人ID */
    private Long createdBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /** 更新人ID */
    private Long updatedBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
