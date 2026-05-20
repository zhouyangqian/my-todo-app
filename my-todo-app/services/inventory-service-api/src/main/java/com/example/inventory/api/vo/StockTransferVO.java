package com.example.inventory.api.vo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存调拨请求VO
 */
@Data
public class StockTransferVO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "源仓库ID不能为空")
    private Long fromWarehouseId;

    @NotNull(message = "目标仓库ID不能为空")
    private Long toWarehouseId;

    @NotNull(message = "调拨数量不能为空")
    @DecimalMin(value = "0.0001", message = "调拨数量必须大于0")
    private BigDecimal quantity;

    @NotBlank(message = "调拨单号不能为空")
    private String transferNo;

    private String remark;
}
