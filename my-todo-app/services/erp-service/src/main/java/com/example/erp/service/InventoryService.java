package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erp.entity.Inventory;
import com.example.erp.entity.InventoryFlow;
import com.example.erp.mapper.InventoryFlowMapper;
import com.example.erp.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 库存服务类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供库存相关的核心业务逻辑：
 * - 库存查询（按仓库+商品维度）和带Redis缓存的库存数量查询
 * - 入库操作（支持批次管理、成本价更新、库存流水记录）
 * - 出库操作（库存不足校验、乐观锁防超卖、库存流水记录）
 * - 库存锁定/解锁（已下单未出库的库存锁定机制）
 * - 库存预警（低于下限或高于上限的商品预警）
 * - 库存缓存管理（Redis缓存，5分钟过期）
 * </p>
 * <p>
 * 库存操作采用乐观锁机制（version字段）防止并发场景下的数据不一致。
 * 每次入库/出库操作都会自动生成库存流水记录，确保库存变动的完整追溯。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService extends ServiceImpl<InventoryMapper, Inventory> {

    /** 库存流水数据访问层，用于记录每次出入库操作的水流日志 */
    private final InventoryFlowMapper inventoryFlowMapper;

    /** Redis模板，用于库存数量的缓存操作 */
    private final RedisTemplate<String, Object> redisTemplate;

    /** 库存缓存Key前缀，完整格式：inventory:stock:{tenantId}:{warehouseId}:{productId} */
    private static final String STOCK_CACHE_KEY = "inventory:stock:";

    /** 缓存过期时间（分钟），库存缓存5分钟后过期 */
    private static final long CACHE_EXPIRE_MINUTES = 5;

    /**
     * 根据仓库ID和商品ID查询库存记录
     *
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @return 库存记录对象，未找到则返回null
     */
    public Inventory getInventory(Long warehouseId, Long productId) {
        return getOne(
            new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getWarehouseId, warehouseId)
                .eq(Inventory::getProductId, productId)
        );
    }

    /**
     * 获取商品库存数量（带Redis缓存）
     * <p>
     * 先从Redis缓存中获取库存数量，缓存未命中则查询数据库并写入缓存。
     * 缓存有效期5分钟，库存变动时会自动清除对应缓存。
     * </p>
     *
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @param tenantId    租户ID
     * @return 库存数量，无库存记录则返回0
     */
    public BigDecimal getStockQuantity(Long warehouseId, Long productId, Long tenantId) {
        String cacheKey = STOCK_CACHE_KEY + tenantId + ":" + warehouseId + ":" + productId;

        // 先从Redis缓存获取库存数量
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return new BigDecimal(cached.toString());
        }

        // 缓存未命中，从数据库查询
        Inventory inventory = getInventory(warehouseId, productId);
        BigDecimal quantity = inventory != null ? inventory.getQuantity() : BigDecimal.ZERO;

        // 将查询结果写入Redis缓存
        redisTemplate.opsForValue().set(cacheKey, quantity, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return quantity;
    }

    /**
     * 入库操作
     * <p>
     * 处理流程：
     * 1. 查询是否已有该仓库+商品的库存记录
     * 2. 如果没有库存记录，创建新记录（初始库存=入库数量）
     * 3. 如果已有库存记录，累加库存数量和可用数量，更新成本价
     * 4. 记录库存流水日志
     * 5. 清除Redis缓存
     * </p>
     *
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @param quantity    入库数量
     * @param costPrice   成本价（可选，传入则更新）
     * @param batchNo     批次号（可选）
     * @param bizType     业务类型（1-采购入库, 3-调拨入库, 7-退货入库等）
     * @param bizNo       业务单号
     * @param bizId       业务ID（可选）
     * @param tenantId    租户ID
     * @param operatorId  操作人ID
     */
    @Transactional
    public void inbound(Long warehouseId, Long productId, BigDecimal quantity,
                       BigDecimal costPrice, String batchNo, Integer bizType,
                       String bizNo, Long bizId, Long tenantId, Long operatorId) {
        // 查询是否已有库存记录
        Inventory inventory = getInventory(warehouseId, productId);
        BigDecimal beforeQuantity = BigDecimal.ZERO;

        if (inventory == null) {
            // 首次入库：创建新的库存记录
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
            // 已有库存：累加库存数量和可用数量
            beforeQuantity = inventory.getQuantity();
            inventory.setQuantity(inventory.getQuantity().add(quantity));
            inventory.setAvailableQuantity(inventory.getAvailableQuantity().add(quantity));
            // 如果传入了有效的成本价，则更新
            if (costPrice != null && costPrice.compareTo(BigDecimal.ZERO) > 0) {
                inventory.setCostPrice(costPrice);
            }
            updateById(inventory);
        }

        // 记录入库流水日志
        recordFlow(warehouseId, productId, bizType, bizNo, bizId,
                   quantity, beforeQuantity, inventory.getQuantity(),
                   costPrice, batchNo, tenantId, operatorId);

        // 清除Redis中的库存缓存
        clearStockCache(warehouseId, productId, tenantId);

        log.info("入库成功: 仓库={}, 商品={}, 数量={}, 业务类型={}, 业务单号={}",
                warehouseId, productId, quantity, bizType, bizNo);
    }

    /**
     * 出库操作
     * <p>
     * 处理流程：
     * 1. 查询库存记录，校验库存是否存在
     * 2. 校验库存数量是否充足（当前库存 >= 需要出库的数量）
     * 3. 使用乐观锁扣减库存（防止并发超卖）
     * 4. 记录库存流水日志（出库数量为负数）
     * 5. 清除Redis缓存
     * </p>
     *
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @param quantity    出库数量
     * @param bizType     业务类型（2-销售出库, 4-调拨出库, 8-退货出库等）
     * @param bizNo       业务单号
     * @param bizId       业务ID（可选）
     * @param tenantId    租户ID
     * @param operatorId  操作人ID
     * @throws RuntimeException 库存不足或扣减失败时抛出
     */
    @Transactional
    public void outbound(Long warehouseId, Long productId, BigDecimal quantity,
                        Integer bizType, String bizNo, Long bizId,
                        Long tenantId, Long operatorId) {
        // 查询库存记录
        Inventory inventory = getInventory(warehouseId, productId);
        if (inventory == null) {
            throw new RuntimeException("库存不足: 商品ID=" + productId);
        }

        // 校验库存数量是否充足
        BigDecimal beforeQuantity = inventory.getQuantity();
        if (beforeQuantity.compareTo(quantity) < 0) {
            throw new RuntimeException("库存不足: 当前库存=" + beforeQuantity + ", 需要数量=" + quantity);
        }

        // 使用乐观锁扣减库存（通过version字段防止并发超卖）
        int rows = baseMapper.decreaseStock(inventory.getId(), quantity);
        if (rows == 0) {
            throw new RuntimeException("库存扣减失败，请重试");
        }

        BigDecimal afterQuantity = beforeQuantity.subtract(quantity);

        // 记录出库流水日志（出库数量记为负数）
        recordFlow(warehouseId, productId, bizType, bizNo, bizId,
                   quantity.negate(), beforeQuantity, afterQuantity,
                   inventory.getCostPrice(), inventory.getBatchNo(),
                   tenantId, operatorId);

        // 清除Redis中的库存缓存
        clearStockCache(warehouseId, productId, tenantId);

        log.info("出库成功: 仓库={}, 商品={}, 数量={}, 业务类型={}, 业务单号={}",
                warehouseId, productId, quantity, bizType, bizNo);
    }

    /**
     * 锁定库存
     * <p>
     * 将指定数量的可用库存转为锁定库存（已下单未出库状态）。
     * 可用数量 = 库存数量 - 锁定数量，锁定后可用数量减少。
     * 用于下单时预占库存，防止超卖。
     * </p>
     *
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @param quantity    锁定数量
     * @param tenantId    租户ID
     * @throws RuntimeException 库存不存在或可用库存不足时抛出
     */
    @Transactional
    public void lockStock(Long warehouseId, Long productId, BigDecimal quantity, Long tenantId) {
        Inventory inventory = getInventory(warehouseId, productId);
        if (inventory == null) {
            throw new RuntimeException("库存不存在");
        }

        // 校验可用库存是否充足
        if (inventory.getAvailableQuantity().compareTo(quantity) < 0) {
            throw new RuntimeException("可用库存不足");
        }

        // 增加锁定数量，减少可用数量
        inventory.setLockedQuantity(inventory.getLockedQuantity().add(quantity));
        inventory.setAvailableQuantity(inventory.getAvailableQuantity().subtract(quantity));
        updateById(inventory);

        // 清除缓存
        clearStockCache(warehouseId, productId, tenantId);
    }

    /**
     * 解锁库存
     * <p>
     * 将指定数量的锁定库存转回可用库存。
     * 用于订单取消或部分退款时释放预占的库存。
     * </p>
     *
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @param quantity    解锁数量
     * @param tenantId    租户ID
     */
    @Transactional
    public void unlockStock(Long warehouseId, Long productId, BigDecimal quantity, Long tenantId) {
        Inventory inventory = getInventory(warehouseId, productId);
        if (inventory == null) {
            return;
        }

        // 减少锁定数量，增加可用数量
        inventory.setLockedQuantity(inventory.getLockedQuantity().subtract(quantity));
        inventory.setAvailableQuantity(inventory.getAvailableQuantity().add(quantity));
        updateById(inventory);

        // 清除缓存
        clearStockCache(warehouseId, productId, tenantId);
    }

    /**
     * 记录库存流水日志
     * <p>
     * 每次库存变动时自动记录一条流水，包含变动前后的数量、
     * 变动数量（正数入库/负数出库）、关联的业务单号等信息，
     * 确保库存变动的完整追溯链。
     * </p>
     *
     * @param warehouseId    仓库ID
     * @param productId      商品ID
     * @param bizType        业务类型
     * @param bizNo          业务单号
     * @param bizId          业务ID
     * @param quantity       变动数量（正数入库，负数出库）
     * @param beforeQuantity 变动前数量
     * @param afterQuantity  变动后数量
     * @param costPrice      成本价
     * @param batchNo        批次号
     * @param tenantId       租户ID
     * @param operatorId     操作人ID
     */
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

    /**
     * 清除指定仓库+商品的Redis库存缓存
     * <p>
     * 在库存变动（入库/出库/锁定/解锁）后调用，确保下次查询获取最新数据
     * </p>
     *
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @param tenantId    租户ID
     */
    public void clearStockCache(Long warehouseId, Long productId, Long tenantId) {
        String cacheKey = STOCK_CACHE_KEY + tenantId + ":" + warehouseId + ":" + productId;
        redisTemplate.delete(cacheKey);
    }

    /**
     * 查询库存预警商品列表
     * <p>
     * 查询库存数量低于下限（stockMin）或高于上限（stockMax）的库存记录，
     * 可按仓库ID过滤。用于定时任务或手动触发库存预警检查。
     * </p>
     *
     * @param tenantId    租户ID
     * @param warehouseId 仓库ID（可选，传入则只查询指定仓库的预警）
     * @return 触发预警的库存记录列表
     */
    public List<Inventory> getAlertInventories(Long tenantId, Long warehouseId) {
        return list(
            new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getTenantId, tenantId)
                .eq(warehouseId != null, Inventory::getWarehouseId, warehouseId)
                // 预警条件：库存低于下限 或 高于上限
                .and(wrapper -> wrapper
                    .lt(Inventory::getQuantity, Inventory::getStockMin)
                    .or()
                    .gt(Inventory::getQuantity, Inventory::getStockMax)
                )
        );
    }
}
