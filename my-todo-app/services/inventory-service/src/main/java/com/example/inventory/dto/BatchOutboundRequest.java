package com.example.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量出库请求DTO
 */
@Data
public class BatchOutboundRequest {

    @NotEmpty(message = "出库明细不能为空")
    @Valid
    private List<OutboundRequest> items;
}
