package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.exception.BusinessException;
import com.example.erp.entity.*;
import com.example.erp.mapper.PurchaseReturnItemMapper;
import com.example.erp.mapper.PurchaseReturnMapper;
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
 * 采购退货服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseReturnService extends ServiceImpl<PurchaseReturnMapper, PurchaseReturn> {

    private final PurchaseReturnItemMapper returnItemMapper;
    private final SupplierService supplierService;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final InventoryService inventoryService;

    /**
     * 分页查询采购退货单
     */
    public Page<PurchaseReturn> getReturnPage(Long tenantId, int page, int size,
                                               Long supplierId, Integer returnStatus) {
        LambdaQueryWrapper<PurchaseReturn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseReturn::getTenantId, tenantId);
        if (supplierId != null) {
            wrapper.eq(PurchaseReturn::getSupplierId, supplierId);
        }
        if (returnStatus != null) {
            wrapper.eq(PurchaseReturn::getReturnStatus, returnStatus);
        }
        wrapper.orderByDesc(PurchaseReturn::getCreatedAt);

        Page<PurchaseReturn> result = page(new Page<>(page, size), wrapper);
        fillRelatedData(result.getRecords());
        return result;
    }

    /**
     * 获取退货单详情
     */
    public PurchaseReturn getReturnDetail(Long returnId) {
        PurchaseReturn purchaseReturn = getById(returnId);
        if (purchaseReturn == null) {
            throw new BusinessException("退货单不存在");
        }
        fillRelatedData(Collections.singletonList(purchaseReturn));

        List<PurchaseReturnItem> items = returnItemMapper.selectList(
            new LambdaQueryWrapper<PurchaseReturnItem>()
                .eq(PurchaseReturnItem::getReturnId, returnId)
        );
        purchaseReturn.setItems(items);
        return purchaseReturn;
    }

    /**
     * 创建采购退货单
     */
    @Transactional
    public PurchaseReturn createReturn(PurchaseReturn purchaseReturn, Long tenantId, Long userId) {
        Supplier supplier = supplierService.getById(purchaseReturn.getSupplierId());
        if (supplier == null) {
            throw new BusinessException("供应商不存在");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseReturnItem item : purchaseReturn.getItems()) {
            item.setAmount(item.getQuantity().multiply(item.getPrice()));
            totalAmount = totalAmount.add(item.getAmount());
        }

        purchaseReturn.setTenantId(tenantId);
        purchaseReturn.setReturnNo(generateReturnNo(tenantId));
        purchaseReturn.setTotalAmount(totalAmount);
        purchaseReturn.setReturnStatus(0);
        save(purchaseReturn);

        for (PurchaseReturnItem item : purchaseReturn.getItems()) {
            item.setTenantId(tenantId);
            item.setReturnId(purchaseReturn.getId());
            returnItemMapper.insert(item);
        }

        log.info("创建采购退货单: {}", purchaseReturn.getReturnNo());
        return purchaseReturn;
    }

    /**
     * 提交审核
     */
    @Transactional
    public void submitForApproval(Long returnId) {
        PurchaseReturn purchaseReturn = getById(returnId);
        if (purchaseReturn == null) {
            throw new BusinessException("退货单不存在");
        }
        if (purchaseReturn.getReturnStatus() != 0) {
            throw new BusinessException("只有草稿状态可以提交");
        }
        purchaseReturn.setReturnStatus(1);
        updateById(purchaseReturn);
    }

    /**
     * 审核退货单（审核后入库）
     */
    @Transactional
    public void approveReturn(Long returnId, Long userId) {
        PurchaseReturn purchaseReturn = getById(returnId);
        if (purchaseReturn == null) {
            throw new BusinessException("退货单不存在");
        }
        if (purchaseReturn.getReturnStatus() != 1) {
            throw new BusinessException("只有待审核状态可以审核");
        }

        // 退货入库（增加库存）
        List<PurchaseReturnItem> items = returnItemMapper.selectList(
            new LambdaQueryWrapper<PurchaseReturnItem>()
                .eq(PurchaseReturnItem::getReturnId, returnId)
        );
        for (PurchaseReturnItem item : items) {
            String bizNo = "PR-" + purchaseReturn.getReturnNo();
            inventoryService.inbound(
                purchaseReturn.getWarehouseId(), item.getProductId(), item.getQuantity(),
                item.getPrice(), null, 7, bizNo, returnId, purchaseReturn.getTenantId(), userId
            );
        }

        purchaseReturn.setReturnStatus(2);
        updateById(purchaseReturn);
        log.info("审核采购退货单: {}", purchaseReturn.getReturnNo());
    }

    /**
     * 取消退货单
     */
    @Transactional
    public void cancelReturn(Long returnId) {
        PurchaseReturn purchaseReturn = getById(returnId);
        if (purchaseReturn == null) {
            throw new BusinessException("退货单不存在");
        }
        if (purchaseReturn.getReturnStatus() == 2) {
            throw new BusinessException("已审核的退货单不能取消");
        }
        purchaseReturn.setReturnStatus(3);
        updateById(purchaseReturn);
    }

    private void fillRelatedData(List<PurchaseReturn> returns) {
        if (returns.isEmpty()) return;
        Set<Long> supplierIds = returns.stream().map(PurchaseReturn::getSupplierId).collect(Collectors.toSet());
        Set<Long> warehouseIds = returns.stream().map(PurchaseReturn::getWarehouseId).collect(Collectors.toSet());

        Map<Long, Supplier> supplierMap = supplierService.listByIds(supplierIds).stream()
            .collect(Collectors.toMap(Supplier::getId, s -> s));
        Map<Long, Warehouse> warehouseMap = warehouseService.listByIds(warehouseIds).stream()
            .collect(Collectors.toMap(Warehouse::getId, w -> w));

        returns.forEach(r -> {
            Supplier s = supplierMap.get(r.getSupplierId());
            if (s != null) r.setSupplierName(s.getSupplierName());
            Warehouse w = warehouseMap.get(r.getWarehouseId());
            if (w != null) r.setWarehouseName(w.getWarehouseName());
        });
    }

    private String generateReturnNo(Long tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PR" + dateStr;
        Long count = lambdaQuery()
            .eq(PurchaseReturn::getTenantId, tenantId)
            .likeRight(PurchaseReturn::getReturnNo, prefix)
            .count();
        return prefix + String.format("%04d", count + 1);
    }
}
