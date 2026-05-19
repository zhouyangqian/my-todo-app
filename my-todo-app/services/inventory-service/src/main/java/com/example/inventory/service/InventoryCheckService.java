package com.example.inventory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.exception.BusinessException;
import com.example.inventory.dto.InventoryCheckCreateRequest;
import com.example.inventory.dto.InventoryCheckSubmitRequest;
import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.InventoryCheck;
import com.example.inventory.entity.InventoryCheckItem;
import com.example.inventory.mapper.InventoryCheckItemMapper;
import com.example.inventory.mapper.InventoryCheckMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 库存盘点服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryCheckService extends ServiceImpl<InventoryCheckMapper, InventoryCheck> {

    private final InventoryCheckItemMapper checkItemMapper;
    private final InventoryService inventoryService;

    /**
     * 分页查询盘点单
     */
    public Page<InventoryCheck> getCheckPage(Long tenantId, int page, int size,
                                              Long warehouseId, Integer checkStatus) {
        LambdaQueryWrapper<InventoryCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryCheck::getTenantId, tenantId);
        if (warehouseId != null) {
            wrapper.eq(InventoryCheck::getWarehouseId, warehouseId);
        }
        if (checkStatus != null) {
            wrapper.eq(InventoryCheck::getCheckStatus, checkStatus);
        }
        wrapper.orderByDesc(InventoryCheck::getCreatedAt);

        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 获取盘点单详情
     */
    public InventoryCheck getCheckDetail(Long checkId) {
        InventoryCheck check = getById(checkId);
        if (check == null) {
            throw new BusinessException("盘点单不存在");
        }

        List<InventoryCheckItem> items = checkItemMapper.selectList(
            new LambdaQueryWrapper<InventoryCheckItem>()
                .eq(InventoryCheckItem::getCheckId, checkId)
        );
        check.setItems(items);
        return check;
    }

    /**
     * 创建盘点单（从库存中加载商品）
     */
    @Transactional
    public InventoryCheck createCheck(InventoryCheck check, InventoryCheckCreateRequest request,
                                       Long tenantId, Long userId) {
        check.setTenantId(tenantId);
        check.setCheckNo(generateCheckNo(tenantId));
        check.setCheckStatus(0);
        check.setTotalProfitQty(BigDecimal.ZERO);
        check.setTotalLossQty(BigDecimal.ZERO);
        save(check);

        // 从库存中加载该仓库的所有商品作为盘点明细
        List<Inventory> inventories = inventoryService.getInventoriesByWarehouse(check.getWarehouseId(), tenantId);

        // 构建商品信息Map（如果调用方提供了商品信息）
        Map<Long, InventoryCheckCreateRequest.ProductInfo> productInfoMap = new HashMap<>();
        if (request.getProducts() != null) {
            productInfoMap = request.getProducts().stream()
                .collect(Collectors.toMap(InventoryCheckCreateRequest.ProductInfo::getProductId, p -> p));
        }

        for (Inventory inv : inventories) {
            InventoryCheckItem item = new InventoryCheckItem();
            item.setTenantId(tenantId);
            item.setCheckId(check.getId());
            item.setProductId(inv.getProductId());

            // 优先使用调用方提供的商品信息
            InventoryCheckCreateRequest.ProductInfo productInfo = productInfoMap.get(inv.getProductId());
            if (productInfo != null) {
                item.setProductCode(productInfo.getProductCode());
                item.setProductName(productInfo.getProductName());
            } else {
                item.setProductCode("");
                item.setProductName("");
            }

            item.setSystemQuantity(inv.getQuantity());
            item.setDiffQuantity(BigDecimal.ZERO);
            checkItemMapper.insert(item);
        }

        log.info("创建盘点单: {}", check.getCheckNo());
        return check;
    }

    /**
     * 提交盘点结果
     */
    @Transactional
    public void submitCheckResult(Long checkId, List<InventoryCheckSubmitRequest.CheckItemSubmit> items, Long userId) {
        InventoryCheck check = getById(checkId);
        if (check == null) {
            throw new BusinessException("盘点单不存在");
        }
        if (check.getCheckStatus() != 0 && check.getCheckStatus() != 1) {
            throw new BusinessException("当前状态不允许提交盘点结果");
        }

        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;

        for (InventoryCheckSubmitRequest.CheckItemSubmit itemSubmit : items) {
            InventoryCheckItem existing = checkItemMapper.selectById(itemSubmit.getItemId());
            if (existing == null) continue;

            existing.setActualQuantity(itemSubmit.getActualQuantity());
            BigDecimal diff = itemSubmit.getActualQuantity().subtract(existing.getSystemQuantity());
            existing.setDiffQuantity(diff);
            existing.setRemark(itemSubmit.getRemark());
            checkItemMapper.updateById(existing);

            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                totalProfit = totalProfit.add(diff);
            } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                totalLoss = totalLoss.add(diff.abs());
            }
        }

        check.setCheckStatus(2); // 已完成
        check.setTotalProfitQty(totalProfit);
        check.setTotalLossQty(totalLoss);
        updateById(check);

        log.info("完成盘点单: {}, 盘盈={}, 盘亏={}", check.getCheckNo(), totalProfit, totalLoss);
    }

    /**
     * 取消盘点单
     */
    @Transactional
    public void cancelCheck(Long checkId) {
        InventoryCheck check = getById(checkId);
        if (check == null) {
            throw new BusinessException("盘点单不存在");
        }
        if (check.getCheckStatus() == 2) {
            throw new BusinessException("已完成的盘点单不能取消");
        }
        check.setCheckStatus(3);
        updateById(check);
        log.info("取消盘点单: {}", check.getCheckNo());
    }

    private String generateCheckNo(Long tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "CK" + dateStr;
        Long count = lambdaQuery()
            .eq(InventoryCheck::getTenantId, tenantId)
            .likeRight(InventoryCheck::getCheckNo, prefix)
            .count();
        return prefix + String.format("%04d", count + 1);
    }
}
