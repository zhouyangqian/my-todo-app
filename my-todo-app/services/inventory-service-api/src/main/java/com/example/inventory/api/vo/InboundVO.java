package com.example.inventory.api.vo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 入库请求VO
 */
@Data
public class InboundVO {

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "入库数量不能为空")
    @DecimalMin(value = "0.0001", message = "入库数量必须大于0")
    private BigDecimal quantity;

    private BigDecimal costPrice;

    private String batchNo;

    @NotNull(message = "业务类型不能为空")
    private Integer bizType;

    @NotBlank(message = "业务单号不能为空")
    private String bizNo;

    private Long bizId;
}
