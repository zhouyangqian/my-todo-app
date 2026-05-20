package com.example.erp.api.feign;

import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.inventory.api.dto.InventoryDTO;
import com.example.inventory.api.vo.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 库存服务 Feign 客户端
 * <p>
 * 用于 erp-service 调用 inventory-service 的接口，
 * 主要用于采购入库、销售出库、退货等场景的库存操作。
 * </p>
 */
@FeignClient(name = "inventory-service", path = "/api/inventory")
public interface InventoryFeignClient {

    @PostMapping("/inbound")
    ApiResponse<Void> inbound(@RequestBody InboundVO request,
                               @RequestHeader("X-Tenant-Id") Long tenantId,
                               @RequestHeader("X-User-Id") Long userId);

    @PostMapping("/outbound")
    ApiResponse<Void> outbound(@RequestBody OutboundVO request,
                                @RequestHeader("X-Tenant-Id") Long tenantId,
                                @RequestHeader("X-User-Id") Long userId);

    @PostMapping("/lock-stock")
    ApiResponse<Void> lockStock(@RequestBody LockStockVO request,
                                 @RequestHeader("X-Tenant-Id") Long tenantId);

    @PostMapping("/unlock-stock")
    ApiResponse<Void> unlockStock(@RequestBody UnlockStockVO request,
                                   @RequestHeader("X-Tenant-Id") Long tenantId);

    @GetMapping("/get-stock-quantity")
    ApiResponse<BigDecimal> getStockQuantity(
            @RequestParam Long warehouseId,
            @RequestParam Long productId,
            @RequestHeader("X-Tenant-Id") Long tenantId);

    @PostMapping("/batch-inbound")
    ApiResponse<Void> batchInbound(@RequestBody BatchInboundVO request,
                                    @RequestHeader("X-Tenant-Id") Long tenantId,
                                    @RequestHeader("X-User-Id") Long userId);

    @PostMapping("/batch-outbound")
    ApiResponse<Void> batchOutbound(@RequestBody BatchOutboundVO request,
                                     @RequestHeader("X-Tenant-Id") Long tenantId,
                                     @RequestHeader("X-User-Id") Long userId);

    @GetMapping("/get-inventory-page")
    ApiResponse<PageResult<InventoryDTO>> getInventoryPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "1000") int size,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String productName);

    @GetMapping("/get-alert-inventories")
    ApiResponse<List<InventoryDTO>> getAlertInventories(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(required = false) Long warehouseId);
}
