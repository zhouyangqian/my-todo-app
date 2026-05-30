package com.example.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 银行对账单实体
 * <p>
 * 对应数据库表：fin_bank_reconciliation
 * 用于管理银行对账周期、匹配状态等信息。
 * </p>
 */
@Data
@TableName("fin_bank_reconciliation")
public class FinBankReconciliation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 银行账户ID */
    private Long bankAccountId;

    /** 对账期间开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate periodStart;

    /** 对账期间结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate periodEnd;

    /** 对账状态: 0=进行中 1=已完成 */
    private Integer status;

    /** 银行方总额 */
    private BigDecimal totalBankAmount;

    /** 系统方总额 */
    private BigDecimal totalSystemAmount;

    /** 已匹配数量 */
    private Integer matchedCount;

    /** 未匹配数量 */
    private Integer unmatchedCount;

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

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
