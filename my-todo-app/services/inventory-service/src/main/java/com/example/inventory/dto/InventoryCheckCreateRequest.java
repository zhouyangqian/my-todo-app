package com.example.inventory.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建盘点单请求DTO
 */
@Data
public class InventoryCheckCreateRequest {

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    private Integer checkType;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime checkDate;

    private String remark;

    /**
     * 商品信息列表（可选，由调用方提供商品编码和名称）
     */
    private List<ProductInfo> products;

    @Data
    public static class ProductInfo {
        private Long productId;
        private String productCode;
        private String productName;
    }
}
