package com.example.inventory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.exception.BusinessException;
import com.example.inventory.api.dto.InventoryDTO;
import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.InventoryFlow;
import com.example.inventory.mapper.InventoryFlowMapper;
import com.example.inventory.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 库存服务类
 * <p>
 * 提供库存相关的核心业务逻辑：
 * - 库存查询（按仓库+商品维度）和带Redis缓存的库存数量查询
 * - 入库操作（支持批次管理、成本价更新、库存流水记录）
 * - 出库操作（库存不足校验、乐观锁防超卖、库存流水记录）
 * - 库存锁定/解锁（已下单未出库的库存锁定机制）
 * - 库存预警（低于下限或高于上限的商品预警）
 * - 库存缓存管理（Redis缓存，5分钟过期）
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService extends ServiceImpl<InventoryMapper, Inventory> {

    private final InventoryFlowMapper inventoryFlowMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String STOCK_CACHE_KEY = "inventory:stock:";
    private static final long CACHE_EXPIRE_MINUTES = 5;

    public Inventory getInventory(Long warehouseId, Long productId) {
        return getOne(
            new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getWarehouseId, warehouseId)
                .eq(Inventory::getProductId, productId)
        );
    }

    public List<Inventory> getInventoriesByWarehouse(Long warehouseId, Long tenantId) {
        return list(
            new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getWarehouseId, warehouseId)
                .eq(Inventory::getTenantId, tenantId)
        );
    }

    public Page<InventoryDTO> getInventoryPage(Long tenantId, int page, int size, Long warehouseId, String productName) {
        Page<Inventory> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getTenantId, tenantId)
               .eq(warehouseId != null, Inventory::getWarehouseId, warehouseId)
               .orderByDesc(Inventory::getUpdatedAt);

        Page<Inventory> result = page(pageParam, wrapper);

        Page<InventoryDTO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<InventoryDTO> voList = result.getRecords().stream().map(inventory -> {
            InventoryDTO vo = new InventoryDTO();
            vo.setId(inventory.getId());
            vo.setWarehouseId(inventory.getWarehouseId());
            vo.setProductId(inventory.getProductId());
            vo.setBatchNo(inventory.getBatchNo());
            vo.setQuantity(inventory.getQuantity());
            vo.setLockedQuantity(inventory.getLockedQuantity());
            vo.setAvailableQuantity(inventory.getAvailableQuantity());
            vo.setStockMin(inventory.getStockMin());
            vo.setStockMax(inventory.getStockMax());
            vo.setCostPrice(inventory.getCostPrice());
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    public BigDecimal getStockQuantity(Long warehouseId, Long productId, Long tenantId) {
        String cacheKey = STOCK_CACHE_KEY + tenantId + ":" + warehouseId + ":" + productId;

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return new BigDecimal(cached.toString());
        }

        Inventory inventory = getInventory(warehouseId, productId);
        BigDecimal quantity = inventory != null ? inventory.getQuantity() : BigDecimal.ZERO;

        redisTemplate.opsForValue().set(cacheKey, quantity, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return quantity;
    }

    @Transactional
    public void inbound(Long warehouseId, Long productId, BigDecimal quantity,
                       BigDecimal costPrice, String batchNo, Integer bizType,
                       String bizNo, Long bizId, Long tenantId, Long operatorId) {
        Inventory inventory = getInventory(warehouseId, productId);
        BigDecimal beforeQuantity = BigDecimal.ZERO;

        if (inventory == null) {
            inventory = new Inventory();
            inventory.setTenantId(tenantId);
            inventory.setWarehouseId(warehouseId);
            inventory.setProductId(productId);
            inventory.setBatchNo(batchNo);
            inventory.setQuantity(quantity);
            inventory.setLockedQuantity(BigDecimal.ZERO);
            inventory.setAvailableQuantity(quantity);
            inventory.setCostPrice(costPrice);
            inventory.setStatus(1);
            inventory.setVersion(0);
            save(inventory);
            beforeQuantity = BigDecimal.ZERO;
        } else {
            beforeQuantity = inventory.getQuantity();
            inventory.setQuantity(inventory.getQuantity().add(quantity));
            inventory.setAvailableQuantity(inventory.getAvailableQuantity().add(quantity));
            if (costPrice != null && costPrice.compareTo(BigDecimal.ZERO) > 0) {
                inventory.setCostPrice(costPrice);
            }
            updateById(inventory);
        }

        recordFlow(warehouseId, productId, bizType, bizNo, bizId,
                   quantity, beforeQuantity, inventory.getQuantity(),
                   costPrice, batchNo, tenantId, operatorId);

        clearStockCache(warehouseId, productId, tenantId);

        log.info("入库成功: 仓库={}, 商品={}, 数量={}, 业务类型={}, 业务单号={}",
                warehouseId, productId, quantity, bizType, bizNo);
    }

    @Transactional
    public void outbound(Long warehouseId, Long productId, BigDecimal quantity,
                        Integer bizType, String bizNo, Long bizId,
                        Long tenantId, Long operatorId) {
        Inventory inventory = getInventory(warehouseId, productId);
        if (inventory == null) {
            throw new BusinessException("库存不足: 商品ID=" + productId);
        }

        BigDecimal beforeQuantity = inventory.getQuantity();
        if (beforeQuantity.compareTo(quantity) < 0) {
            throw new BusinessException("库存不足: 当前库存=" + beforeQuantity + ", 需要数量=" + quantity);
        }

        int rows = baseMapper.decreaseStock(inventory.getId(), quantity);
        if (rows == 0) {
            throw new BusinessException("库存扣减失败，请重试");
        }

        BigDecimal afterQuantity = beforeQuantity.subtract(quantity);

        recordFlow(warehouseId, productId, bizType, bizNo, bizId,
                   quantity.negate(), beforeQuantity, afterQuantity,
                   inventory.getCostPrice(), inventory.getBatchNo(),
                   tenantId, operatorId);

        clearStockCache(warehouseId, productId, tenantId);

        log.info("出库成功: 仓库={}, 商品={}, 数量={}, 业务类型={}, 业务单号={}",
                warehouseId, productId, quantity, bizType, bizNo);
    }

    @Transactional
    public void lockStock(Long warehouseId, Long productId, BigDecimal quantity, Long tenantId) {
        Inventory inventory = getInventory(warehouseId, productId);
        if (inventory == null) {
            throw new BusinessException("库存不存在");
        }

        if (inventory.getAvailableQuantity().compareTo(quantity) < 0) {
            throw new BusinessException("可用库存不足");
        }

        inventory.setLockedQuantity(inventory.getLockedQuantity().add(quantity));
        inventory.setAvailableQuantity(inventory.getAvailableQuantity().subtract(quantity));
        updateById(inventory);

        clearStockCache(warehouseId, productId, tenantId);
    }

    @Transactional
    public void unlockStock(Long warehouseId, Long productId, BigDecimal quantity, Long tenantId) {
        Inventory inventory = getInventory(warehouseId, productId);
        if (inventory == null) {
            return;
        }

        inventory.setLockedQuantity(inventory.getLockedQuantity().subtract(quantity));
        inventory.setAvailableQuantity(inventory.getAvailableQuantity().add(quantity));
        updateById(inventory);

        clearStockCache(warehouseId, productId, tenantId);
    }

    private void recordFlow(Long warehouseId, Long productId, Integer bizType,
                           String bizNo, Long bizId, BigDecimal quantity,
                           BigDecimal beforeQuantity, BigDecimal afterQuantity,
                           BigDecimal costPrice, String batchNo,
                           Long tenantId, Long operatorId) {
        InventoryFlow flow = new InventoryFlow();
        flow.setTenantId(tenantId);
        flow.setWarehouseId(warehouseId);
        flow.setProductId(productId);
        flow.setBizType(bizType);
        flow.setBizNo(bizNo);
        flow.setBizId(bizId);
        flow.setQuantity(quantity);
        flow.setBeforeQuantity(beforeQuantity);
        flow.setAfterQuantity(afterQuantity);
        flow.setCostPrice(costPrice);
        flow.setBatchNo(batchNo);
        flow.setOperatorId(operatorId);
        flow.setCreatedAt(LocalDateTime.now());
        inventoryFlowMapper.insert(flow);
    }

    public void clearStockCache(Long warehouseId, Long productId, Long tenantId) {
        String cacheKey = STOCK_CACHE_KEY + tenantId + ":" + warehouseId + ":" + productId;
        redisTemplate.delete(cacheKey);
    }

    public Page<InventoryFlow> getFlowPage(Long tenantId, int page, int size,
                                            Long warehouseId, Long productId, Integer bizType) {
        LambdaQueryWrapper<InventoryFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryFlow::getTenantId, tenantId)
               .eq(warehouseId != null, InventoryFlow::getWarehouseId, warehouseId)
               .eq(productId != null, InventoryFlow::getProductId, productId)
               .eq(bizType != null, InventoryFlow::getBizType, bizType)
               .orderByDesc(InventoryFlow::getCreatedAt);
        return inventoryFlowMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public List<Inventory> getAlertInventories(Long tenantId, Long warehouseId) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getTenantId, tenantId)
               .eq(warehouseId != null, Inventory::getWarehouseId, warehouseId)
               .and(w -> w.apply("quantity < stock_min OR quantity > stock_max"));
        return list(wrapper);
    }
}
