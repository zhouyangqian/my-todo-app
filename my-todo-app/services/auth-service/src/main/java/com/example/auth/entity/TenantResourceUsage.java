package com.example.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租户资源使用量实体类
 * <p>
 * 对应 sys_tenant_resource_usage 表，记录每个租户每日的资源使用情况。
 * </p>
 */
@Data
@TableName("sys_tenant_resource_usage")
public class TenantResourceUsage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 用户数量 */
    private Integer userCount;

    /** 已使用存储空间（MB） */
    private BigDecimal storageUsedMb;

    /** 今日API调用次数 */
    private Integer apiCallsToday;

    /** 当前并发请求数 */
    private Integer concurrentRequests;

    /** 记录日期 */
    private LocalDate recordDate;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
