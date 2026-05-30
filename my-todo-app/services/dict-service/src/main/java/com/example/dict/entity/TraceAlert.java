package com.example.dict.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 追踪告警实体
 */
@Data
@TableName("trace_alert")
public class TraceAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 告警名称 */
    private String alertName;

    /** 指标类型: RESPONSE_TIME/ERROR_RATE/THROUGHPUT */
    private String metricType;

    /** 阈值 */
    private BigDecimal threshold;

    /** 条件类型: GT/LT/EQ */
    private String conditionType;

    /** 服务名称 */
    private String serviceName;

    /** 是否启用 */
    private Integer enabled;

    /** 通知方式 */
    private String notifyType;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
