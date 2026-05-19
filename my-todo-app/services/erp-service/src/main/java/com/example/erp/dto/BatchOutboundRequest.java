package com.example.erp.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量出库请求DTO
 */
@Data
public class BatchOutboundRequest {
    private List<OutboundRequest> items;
}
