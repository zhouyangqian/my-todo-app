package com.example.finance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收支记录创建请求DTO
 */
@Data
public class TransactionCreateDTO {

    /** 收支类型：1-收入, 2-支出 */
    @NotNull(message = "收支类型不能为空")
    private Integer recordType;

    /** 业务类型：1-销售收款, 2-采购付款, 3-退款, 4-其他收入, 5-其他支出 */
    private Integer bizType;

    /** 关联业务ID */
    private Long bizId;

    /** 客户或供应商ID */
    private Long partnerId;

    /** 收支金额 */
    @NotNull(message = "收支金额不能为空")
    private BigDecimal amount;

    /** 币种编码 */
    private String currency;

    /** 支付方式：1-现金, 2-银行转账, 3-支付宝, 4-微信, 5-支票 */
    private Integer paymentMethod;

    /** 银行账户ID */
    @NotNull(message = "银行账户ID不能为空")
    private Long bankAccountId;

    /** 交易日期 */
    private LocalDateTime transactionDate;

    /** 汇率 */
    private BigDecimal exchangeRate;

    /** 交易类型：INCOME/EXPENSE/TRANSFER */
    private String transType;

    /** 来源类型：BILL/MANUAL/ADJUSTMENT */
    private String sourceType;

    /** 科目编码 */
    private String categoryCode;

    /** 科目名称 */
    private String categoryName;

    /** 备注 */
    private String remark;
}
