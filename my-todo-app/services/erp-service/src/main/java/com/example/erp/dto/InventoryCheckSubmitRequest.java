package com.example.erp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 提交盘点结果请求DTO
 */
@Data
public class InventoryCheckSubmitRequest {
    private List<CheckItemSubmit> items;

    @Data
    public static class CheckItemSubmit {
        private Long itemId;
        private BigDecimal actualQuantity;
        private String remark;
    }
}
