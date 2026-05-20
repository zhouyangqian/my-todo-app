package com.example.inventory.api.vo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 解锁库存请求VO
 */
@Data
public class UnlockStockVO {

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "解锁数量不能为空")
    @DecimalMin(value = "0.0001", message = "解锁数量必须大于0")
    private BigDecimal quantity;
}
