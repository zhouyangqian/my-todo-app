package com.example.dict.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * API使用记录实体
 */
@Data
@TableName("api_usage_record")
public class ApiUsageRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** API定义ID */
    private Long apiId;

    /** 订阅ID */
    private Long subscriptionId;

    /** 请求时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime requestTime;

    /** 响应状态码 */
    private Integer responseStatus;

    /** 响应时间(ms) */
    private Integer responseTimeMs;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
