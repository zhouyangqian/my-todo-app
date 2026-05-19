package com.example.erp.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.erp.entity.Inventory;
import com.example.erp.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 库存预警定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryAlertTask {

    private final InventoryMapper inventoryMapper;

    /**
     * 每小时检查一次库存预警
     * 查询库存数量低于下限或高于上限的记录
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkInventoryAlert() {
        // 查询低于库存下限的商品
        List<Inventory> lowStockList = inventoryMapper.selectList(
            new LambdaQueryWrapper<Inventory>()
                .apply("quantity < stock_min AND stock_min > 0")
        );
        if (!lowStockList.isEmpty()) {
            log.warn("库存预警：{}个商品库存低于下限", lowStockList.size());
            for (Inventory inv : lowStockList) {
                log.warn("  仓库={}, 商品={}, 当前库存={}, 下限={}",
                    inv.getWarehouseId(), inv.getProductId(), inv.getQuantity(), inv.getStockMin());
            }
        }

        // 查询超过库存上限的商品
        List<Inventory> highStockList = inventoryMapper.selectList(
            new LambdaQueryWrapper<Inventory>()
                .apply("quantity > stock_max AND stock_max < 99999999")
        );
        if (!highStockList.isEmpty()) {
            log.warn("库存预警：{}个商品库存超过上限", highStockList.size());
            for (Inventory inv : highStockList) {
                log.warn("  仓库={}, 商品={}, 当前库存={}, 上限={}",
                    inv.getWarehouseId(), inv.getProductId(), inv.getQuantity(), inv.getStockMax());
            }
        }

        if (lowStockList.isEmpty() && highStockList.isEmpty()) {
            log.info("库存预警检查完成，无异常");
        }
    }
}
