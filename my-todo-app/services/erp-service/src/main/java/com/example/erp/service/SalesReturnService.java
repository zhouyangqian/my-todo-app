package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.exception.BusinessException;
import com.example.erp.entity.*;
import com.example.erp.mapper.SalesReturnItemMapper;
import com.example.erp.mapper.SalesReturnMapper;
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
 * 销售退货服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesReturnService extends ServiceImpl<SalesReturnMapper, SalesReturn> {

    private final SalesReturnItemMapper returnItemMapper;
    private final CustomerService customerService;
    private final WarehouseService warehouseService;
    private final InventoryService inventoryService;

    /**
     * 分页查询销售退货单
     */
    public Page<SalesReturn> getReturnPage(Long tenantId, int page, int size,
                                            Long customerId, Integer returnStatus) {
        LambdaQueryWrapper<SalesReturn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesReturn::getTenantId, tenantId);
        if (customerId != null) {
            wrapper.eq(SalesReturn::getCustomerId, customerId);
        }
        if (returnStatus != null) {
            wrapper.eq(SalesReturn::getReturnStatus, returnStatus);
        }
        wrapper.orderByDesc(SalesReturn::getCreatedAt);

        Page<SalesReturn> result = page(new Page<>(page, size), wrapper);
        fillRelatedData(result.getRecords());
        return result;
    }

    /**
     * 获取退货单详情
     */
    public SalesReturn getReturnDetail(Long returnId) {
        SalesReturn salesReturn = getById(returnId);
        if (salesReturn == null) {
            throw new BusinessException("退货单不存在");
        }
        fillRelatedData(Collections.singletonList(salesReturn));

        List<SalesReturnItem> items = returnItemMapper.selectList(
            new LambdaQueryWrapper<SalesReturnItem>()
                .eq(SalesReturnItem::getReturnId, returnId)
        );
        salesReturn.setItems(items);
        return salesReturn;
    }

    /**
     * 创建销售退货单
     */
    @Transactional
    public SalesReturn createReturn(SalesReturn salesReturn, Long tenantId, Long userId) {
        Customer customer = customerService.getById(salesReturn.getCustomerId());
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SalesReturnItem item : salesReturn.getItems()) {
            item.setAmount(item.getQuantity().multiply(item.getPrice()));
            totalAmount = totalAmount.add(item.getAmount());
        }

        salesReturn.setTenantId(tenantId);
        salesReturn.setReturnNo(generateReturnNo(tenantId));
        salesReturn.setTotalAmount(totalAmount);
        salesReturn.setReturnStatus(0);
        save(salesReturn);

        for (SalesReturnItem item : salesReturn.getItems()) {
            item.setTenantId(tenantId);
            item.setReturnId(salesReturn.getId());
            returnItemMapper.insert(item);
        }

        log.info("创建销售退货单: {}", salesReturn.getReturnNo());
        return salesReturn;
    }

    /**
     * 提交审核
     */
    @Transactional
    public void submitForApproval(Long returnId) {
        SalesReturn salesReturn = getById(returnId);
        if (salesReturn == null) {
            throw new BusinessException("退货单不存在");
        }
        if (salesReturn.getReturnStatus() != 0) {
            throw new BusinessException("只有草稿状态可以提交");
        }
        salesReturn.setReturnStatus(1);
        updateById(salesReturn);
    }

    /**
     * 审核退货单（审核后入库）
     */
    @Transactional
    public void approveReturn(Long returnId, Long userId) {
        SalesReturn salesReturn = getById(returnId);
        if (salesReturn == null) {
            throw new BusinessException("退货单不存在");
        }
        if (salesReturn.getReturnStatus() != 1) {
            throw new BusinessException("只有待审核状态可以审核");
        }

        // 退货入库（增加库存）
        List<SalesReturnItem> items = returnItemMapper.selectList(
            new LambdaQueryWrapper<SalesReturnItem>()
                .eq(SalesReturnItem::getReturnId, returnId)
        );
        for (SalesReturnItem item : items) {
            String bizNo = "SR-" + salesReturn.getReturnNo();
            inventoryService.inbound(
                salesReturn.getWarehouseId(), item.getProductId(), item.getQuantity(),
                item.getPrice(), null, 7, bizNo, returnId, salesReturn.getTenantId(), userId
            );
        }

        salesReturn.setReturnStatus(2);
        updateById(salesReturn);
        log.info("审核销售退货单: {}", salesReturn.getReturnNo());
    }

    /**
     * 取消退货单
     */
    @Transactional
    public void cancelReturn(Long returnId) {
        SalesReturn salesReturn = getById(returnId);
        if (salesReturn == null) {
            throw new BusinessException("退货单不存在");
        }
        if (salesReturn.getReturnStatus() == 2) {
            throw new BusinessException("已审核的退货单不能取消");
        }
        salesReturn.setReturnStatus(3);
        updateById(salesReturn);
    }

    private void fillRelatedData(List<SalesReturn> returns) {
        if (returns.isEmpty()) return;
        Set<Long> customerIds = returns.stream().map(SalesReturn::getCustomerId).collect(Collectors.toSet());
        Set<Long> warehouseIds = returns.stream().map(SalesReturn::getWarehouseId).collect(Collectors.toSet());

        Map<Long, Customer> customerMap = customerService.listByIds(customerIds).stream()
            .collect(Collectors.toMap(Customer::getId, c -> c));
        Map<Long, Warehouse> warehouseMap = warehouseService.listByIds(warehouseIds).stream()
            .collect(Collectors.toMap(Warehouse::getId, w -> w));

        returns.forEach(r -> {
            Customer c = customerMap.get(r.getCustomerId());
            if (c != null) r.setCustomerName(c.getCustomerName());
            Warehouse w = warehouseMap.get(r.getWarehouseId());
            if (w != null) r.setWarehouseName(w.getWarehouseName());
        });
    }

    private String generateReturnNo(Long tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "SR" + dateStr;
        Long count = lambdaQuery()
            .eq(SalesReturn::getTenantId, tenantId)
            .likeRight(SalesReturn::getReturnNo, prefix)
            .count();
        return prefix + String.format("%04d", count + 1);
    }
}
