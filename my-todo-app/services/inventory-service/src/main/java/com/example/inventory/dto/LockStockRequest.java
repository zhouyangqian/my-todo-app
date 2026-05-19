package com.example.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 锁定库存请求DTO
 */
@Data
public class LockStockRequest {

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "锁定数量不能为空")
    @DecimalMin(value = "0.0001", message = "锁定数量必须大于0")
    private BigDecimal quantity;
}
