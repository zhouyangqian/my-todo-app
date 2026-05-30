package com.example.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户配额实体类
 * <p>
 * 对应 sys_tenant_quota 表，记录每个租户的资源配额限制。
 * </p>
 */
@Data
@TableName("sys_tenant_quota")
public class TenantQuota implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID（唯一） */
    private Long tenantId;

    /** 最大用户数 */
    private Integer maxUsers;

    /** 最大存储空间（MB） */
    private Integer maxStorageMb;

    /** 每日最大API调用次数 */
    private Integer maxApiCallsPerDay;

    /** 最大并发请求数 */
    private Integer maxConcurrentRequests;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
