package com.example.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 预算创建/更新请求DTO
 */
@Data
public class BudgetDTO {

    /** 预算名称 */
    @NotBlank(message = "预算名称不能为空")
    private String budgetName;

    /** 预算类型: DEPARTMENT/PROJECT/OVERALL */
    @NotBlank(message = "预算类型不能为空")
    private String budgetType;

    /** 目标ID（部门ID或项目ID） */
    private Long targetId;

    /** 周期类型: MONTHLY/QUARTERLY/YEARLY */
    @NotBlank(message = "周期类型不能为空")
    private String periodType;

    /** 期间开始日期 */
    @NotNull(message = "开始日期不能为空")
    private LocalDate periodStart;

    /** 期间结束日期 */
    @NotNull(message = "结束日期不能为空")
    private LocalDate periodEnd;

    /** 预算金额 */
    @NotNull(message = "预算金额不能为空")
    private BigDecimal budgetAmount;

    /** 控制级别: FORCE/WARN/LOG */
    private String controlLevel;

    /** 警告阈值(百分比) */
    private BigDecimal warningThreshold;
}
