package com.example.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 黑名单实体类
 * <p>
 * 对应数据库表 sys_blacklist，用于记录被拉黑的用户信息。
 * 支持多租户隔离，通过 tenantId 区分不同租户的黑名单数据。
 * 黑名单用户将被强制踢出所有会话。
 * </p>
 */
@Data
@TableName("sys_blacklist")
public class Blacklist implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID，自增长 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 被拉黑用户ID */
    private Long userId;

    /** 拉黑原因 */
    private String reason;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人类型（TENANT_ADMIN/SYSTEM_ADMIN） */
    private String operatorType;

    /** 状态：1=生效，0=已解除 */
    private Integer status;

    /** 加入黑名单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime addedAt;

    /** 解除黑名单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime removedAt;

    /** 解除操作人ID */
    private Long removedBy;

    /** 逻辑删除标志：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
