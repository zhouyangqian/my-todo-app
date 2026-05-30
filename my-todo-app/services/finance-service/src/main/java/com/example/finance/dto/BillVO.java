package com.example.finance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单响应VO
 */
@Data
public class BillVO {

    private Long id;

    private String billNo;

    /** 账单类型：1=应收, 2=应付 */
    private Integer billType;

    /** 方向：1=正数, -1=红字 */
    private Integer direction;

    /** 往来单位类型：1=客户, 2=供应商 */
    private Integer partnerType;

    private Long partnerId;

    private String partnerName;

    private Long accountId;

    private String currency;

    private BigDecimal exchangeRate;

    private BigDecimal amount;

    private BigDecimal baseAmount;

    private BigDecimal paidAmount;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate billDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate dueDate;

    /** 状态：0=草稿, 1=待审核, 2=已审核, 3=部分收付, 4=已完成, 5=已取消 */
    private Integer status;

    private String sourceType;

    private Long sourceId;

    private String sourceNo;

    /** 审核状态：0=无, 1=待审核, 2=已通过, 3=已驳回 */
    private Integer auditStatus;

    private Long auditBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime auditAt;

    private String auditRemark;

    private String remark;

    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    private Long updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
