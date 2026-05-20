package com.example.finance.api.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建发票请求VO
 */
@Data
public class InvoiceCreateVO {

    @NotNull(message = "发票类型不能为空")
    private Integer invoiceType;

    @NotNull(message = "发票方向不能为空")
    private Integer invoiceDirection;

    private Long bizId;

    private Long partnerId;

    @NotNull(message = "开票日期不能为空")
    private LocalDateTime invoiceDate;

    @NotNull(message = "不含税金额不能为空")
    private BigDecimal amountWithoutTax;

    private BigDecimal taxAmount;

    @NotNull(message = "价税合计不能为空")
    private BigDecimal totalAmount;

    private BigDecimal taxRate;

    private String remark;

    private String invoiceNo;
}
