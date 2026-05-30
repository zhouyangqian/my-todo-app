package com.example.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 财务报表实体
 */
@Data
@TableName("fin_report")
public class FinReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 报表类型: 1-资产负债表, 2-利润表, 3-现金流量表, 4-毛利分析 */
    private Integer reportType;

    /** 报表名称 */
    private String reportName;

    /** 期间类型：MONTHLY-月度, QUARTERLY-季度, YEARLY-年度 */
    private String periodType;

    /** 币种编码 */
    private String currency;

    /** 报表期间 (如 2026-01) */
    private String reportPeriod;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate endDate;

    private String reportData;

    /** 状态: 0-草稿, 1-已生成, 2-已锁定 */
    private Integer status;

    /** 锁定操作人ID */
    private Long lockedBy;

    /** 锁定时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lockedAt;

    /** 版本号，乐观锁，默认1 */
    private Integer version;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
