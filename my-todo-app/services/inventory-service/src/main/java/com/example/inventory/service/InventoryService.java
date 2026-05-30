package com.example.inventory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.inventory.api.dto.InventoryDTO;
import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.InventoryFlow;

import java.math.BigDecimal;
import java.util.List;

/**
 * 库存服务接口
 */
public interface InventoryService {

    Inventory getInventory(Long warehouseId, Long productId);

    List<Inventory> getInventoriesByWarehouse(Long warehouseId, Long tenantId);

    Page<InventoryDTO> getInventoryPage(Long tenantId, int page, int size, Long warehouseId, String productName);

    BigDecimal getStockQuantity(Long warehouseId, Long productId, Long tenantId);

    void inbound(Long warehouseId, Long productId, BigDecimal quantity,
                 BigDecimal costPrice, String batchNo, Integer bizType,
                 String bizNo, Long bizId, Long tenantId, Long operatorId);

    void outbound(Long warehouseId, Long productId, BigDecimal quantity,
                  Integer bizType, String bizNo, Long bizId,
                  Long tenantId, Long operatorId);

    void lockStock(Long warehouseId, Long productId, BigDecimal quantity, Long tenantId);

    void unlockStock(Long warehouseId, Long productId, BigDecimal quantity, Long tenantId);

    void clearStockCache(Long warehouseId, Long productId, Long tenantId);

    Page<InventoryFlow> getFlowPage(Long tenantId, int page, int size,
                                    Long warehouseId, Long productId, Integer bizType);

    void transfer(Long fromWarehouseId, Long toWarehouseId, Long productId,
                  BigDecimal quantity, String transferNo, Long tenantId, Long operatorId);

    List<Inventory> getAlertInventories(Long tenantId, Long warehouseId);
}
