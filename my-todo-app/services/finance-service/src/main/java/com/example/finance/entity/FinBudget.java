package com.example.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预算管理实体
 * <p>
 * 对应数据库表：fin_budget
 * 用于管理各部门/项目/总体的预算计划，支持多种控制级别。
 * </p>
 */
@Data
@TableName("fin_budget")
public class FinBudget implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 预算名称 */
    private String budgetName;

    /** 预算类型: DEPARTMENT/PROJECT/OVERALL */
    private String budgetType;

    /** 目标ID（部门ID或项目ID） */
    private Long targetId;

    /** 周期类型: MONTHLY/QUARTERLY/YEARLY */
    private String periodType;

    /** 预算期间开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate periodStart;

    /** 预算期间结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate periodEnd;

    /** 预算金额 */
    private BigDecimal budgetAmount;

    /** 已使用金额 */
    private BigDecimal usedAmount;

    /** 冻结金额 */
    private BigDecimal frozenAmount;

    /** 剩余金额 */
    private BigDecimal remainingAmount;

    /** 控制级别: FORCE/WARN/LOG */
    private String controlLevel;

    /** 警告阈值（百分比） */
    private BigDecimal warningThreshold;

    /** 状态: 0=草稿 1=已审批 2=执行中 3=已结束 */
    private Integer status;

    /** 软删除标记 */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /** 创建人ID */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /** 更新人ID */
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
