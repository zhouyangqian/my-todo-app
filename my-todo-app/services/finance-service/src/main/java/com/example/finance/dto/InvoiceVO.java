package com.example.finance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发票响应VO
 */
@Data
public class InvoiceVO {

    private Long id;

    private String invoiceNo;

    private String invoiceCode;

    /** 发票类型：1-增值税专用发票, 2-增值税普通发票, 3-电子发票 */
    private Integer invoiceType;

    /** 发票方向：1-开票, 2-收票 */
    private Integer invoiceDirection;

    private Long bizId;

    private Long partnerId;

    private Integer partnerType;

    private String partnerName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime invoiceDate;

    private String currency;

    private BigDecimal amountWithoutTax;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;

    private BigDecimal taxRate;

    /** 状态：0-待开票, 1-已开票, 2-已作废 */
    private Integer status;

    private String voidReason;

    private Long voidBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime voidAt;

    private String remark;

    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    private Long updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
