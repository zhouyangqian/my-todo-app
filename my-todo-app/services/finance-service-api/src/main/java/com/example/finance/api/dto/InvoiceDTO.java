package com.example.finance.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发票DTO（服务间传输）
 */
@Data
public class InvoiceDTO {

    private Long id;

    private Long tenantId;

    private String invoiceNo;

    private Integer invoiceType;

    private Integer invoiceDirection;

    private Long bizId;

    private Long partnerId;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime invoiceDate;

    private BigDecimal amountWithoutTax;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;

    private BigDecimal taxRate;

    private Integer status;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
