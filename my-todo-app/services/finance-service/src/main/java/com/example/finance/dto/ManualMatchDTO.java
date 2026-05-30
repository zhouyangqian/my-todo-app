package com.example.finance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 手动匹配请求DTO
 */
@Data
public class ManualMatchDTO {

    /** 银行交易记录ID */
    @NotNull(message = "银行记录ID不能为空")
    private Long bankRecordId;

    /** 系统记录ID */
    @NotNull(message = "系统记录ID不能为空")
    private Long systemRecordId;
}
