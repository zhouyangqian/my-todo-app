package com.example.erp.feign;

import com.example.common.core.result.ApiResponse;
import com.example.erp.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 库存服务 Feign 客户端
 * <p>
 * 用于 erp-service 调用 inventory-service 的接口，
 * 主要用于采购入库、销售出库、退货等场景的库存操作。
 * </p>
 */
@FeignClient(name = "inventory-service", path = "/api/inventory")
public interface InventoryServiceClient {

    @PostMapping("/inbound")
    ApiResponse<Void> inbound(@RequestBody InboundRequest request,
                               @RequestHeader("X-Tenant-Id") Long tenantId,
                               @RequestHeader("X-User-Id") Long userId);

    @PostMapping("/outbound")
    ApiResponse<Void> outbound(@RequestBody OutboundRequest request,
                                @RequestHeader("X-Tenant-Id") Long tenantId,
                                @RequestHeader("X-User-Id") Long userId);

    @PostMapping("/lock-stock")
    ApiResponse<Void> lockStock(@RequestBody LockStockRequest request,
                                 @RequestHeader("X-Tenant-Id") Long tenantId);

    @PostMapping("/unlock-stock")
    ApiResponse<Void> unlockStock(@RequestBody UnlockStockRequest request,
                                   @RequestHeader("X-Tenant-Id") Long tenantId);

    @GetMapping("/get-stock-quantity")
    ApiResponse<BigDecimal> getStockQuantity(
            @RequestParam Long warehouseId,
            @RequestParam Long productId,
            @RequestHeader("X-Tenant-Id") Long tenantId);

    @PostMapping("/batch-inbound")
    ApiResponse<Void> batchInbound(@RequestBody BatchInboundRequest request,
                                    @RequestHeader("X-Tenant-Id") Long tenantId,
                                    @RequestHeader("X-User-Id") Long userId);

    @PostMapping("/batch-outbound")
    ApiResponse<Void> batchOutbound(@RequestBody BatchOutboundRequest request,
                                     @RequestHeader("X-Tenant-Id") Long tenantId,
                                     @RequestHeader("X-User-Id") Long userId);
}
