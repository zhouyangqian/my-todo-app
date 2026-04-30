package com.example.erp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 应收账款创建请求DTO
 * 用于调用finance-service时传递数据
 */
@Data
public class AccountReceivableCreateRequest {

    /** 租户ID */
    private Long tenantId;

    /** 业务单号 */
    private String bizNo;

    /** 客户ID */
    private Long customerId;

    /** 应收金额 */
    private BigDecimal amount;

    /** 已收金额 */
    private BigDecimal receivedAmount = BigDecimal.ZERO;

    /** 未收金额 */
    private BigDecimal unreceivedAmount;

    /** 业务日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate bizDate;

    /** 到期日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate dueDate;

    /** 币种 */
    private String currency = "CNY";

    /** 状态 */
    private Integer status = 0;

    /** 备注 */
    private String remark;
}
