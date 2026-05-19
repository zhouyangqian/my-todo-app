package com.example.erp.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量入库请求DTO
 */
@Data
public class BatchInboundRequest {
    private List<InboundRequest> items;
}
