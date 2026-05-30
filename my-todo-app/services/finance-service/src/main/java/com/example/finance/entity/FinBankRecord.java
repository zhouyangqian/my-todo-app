package com.example.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 银行交易记录实体
 * <p>
 * 对应数据库表：fin_bank_record
 * 用于存储从银行对账单导入的交易明细。
 * </p>
 */
@Data
@TableName("fin_bank_record")
public class FinBankRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 银行账户ID */
    private Long bankAccountId;

    /** 交易日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate transactionDate;

    /** 交易金额 */
    private BigDecimal amount;

    /** 交易描述 */
    private String description;

    /** 交易参考号 */
    private String referenceNo;

    /** 交易类型: DEBIT/CREDIT */
    private String transactionType;

    /** 匹配状态: 0=未匹配 1=自动匹配 2=手动匹配 3=异常 */
    private Integer matchStatus;

    /** 匹配的系统记录ID */
    private Long matchedRecordId;

    /** 导入批次号 */
    private String importBatch;

    /** 软删除标记 */
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
