package com.example.inventory.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 盘点单数据传输对象
 */
@Data
public class InventoryCheckDTO {
    private Long id;
    private String checkNo;
    private Long warehouseId;
    private String warehouseName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime checkDate;

    private Integer checkType;
    private String checkTypeText;
    private Integer checkStatus;
    private String checkStatusText;
    private BigDecimal totalProfitQty;
    private BigDecimal totalLossQty;
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createdAt;

    private List<InventoryCheckItemDTO> items;
}
