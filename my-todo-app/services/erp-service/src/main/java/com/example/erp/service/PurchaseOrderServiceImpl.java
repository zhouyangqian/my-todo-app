package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.exception.BusinessException;
import com.example.inventory.api.vo.InboundVO;
import com.example.erp.api.vo.PurchaseInboundVO;
import com.example.erp.api.vo.CreatePurchaseOrderVO;
import com.example.erp.api.dto.PurchaseOrderDTO;
import com.example.erp.api.dto.PurchaseOrderItemDTO;
import com.example.erp.api.feign.InventoryFeignClient;
import com.example.erp.entity.*;
import com.example.erp.mapper.PurchaseOrderItemMapper;
import com.example.erp.mapper.PurchaseOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 采购订单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> implements PurchaseOrderService {

    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final SupplierService supplierService;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final InventoryFeignClient inventoryServiceClient;

    @Override
    public Page<PurchaseOrderDTO> getOrderPage(Long tenantId, int page, int size,
                                               String orderNo, Long supplierId, Integer status) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrder::getTenantId, tenantId);
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(PurchaseOrder::getOrderNo, orderNo);
        }
        if (supplierId != null) {
            wrapper.eq(PurchaseOrder::getSupplierId, supplierId);
        }
        if (status != null) {
            wrapper.eq(PurchaseOrder::getOrderStatus, status);
        }
        wrapper.orderByDesc(PurchaseOrder::getCreatedAt);

        Page<PurchaseOrder> result = page(new Page<>(page, size), wrapper);
        List<PurchaseOrder> orders = result.getRecords();
        if (orders.isEmpty()) {
            Page<PurchaseOrderDTO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }

        Set<Long> orderIds = orders.stream().map(PurchaseOrder::getId).collect(Collectors.toSet());
        Set<Long> supplierIds = orders.stream().map(PurchaseOrder::getSupplierId).collect(Collectors.toSet());
        Set<Long> warehouseIds = orders.stream().map(PurchaseOrder::getWarehouseId).collect(Collectors.toSet());

        Map<Long, List<PurchaseOrderItem>> itemsMap = purchaseOrderItemMapper.selectList(
            new LambdaQueryWrapper<PurchaseOrderItem>()
                .in(PurchaseOrderItem::getOrderId, orderIds)
        ).stream().collect(Collectors.groupingBy(PurchaseOrderItem::getOrderId));

        Map<Long, Supplier> supplierMap = supplierService.listByIds(supplierIds).stream()
            .collect(Collectors.toMap(Supplier::getId, s -> s));
        Map<Long, Warehouse> warehouseMap = warehouseService.listByIds(warehouseIds).stream()
            .collect(Collectors.toMap(Warehouse::getId, w -> w));

        List<PurchaseOrderDTO> voList = orders.stream().map(order -> {
            PurchaseOrderDTO vo = convertToVO(order, supplierMap, warehouseMap);
            List<PurchaseOrderItem> items = itemsMap.getOrDefault(order.getId(), Collections.emptyList());
            vo.setItems(items.stream().map(this::convertItemToVO).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());

        Page<PurchaseOrderDTO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public PurchaseOrderDTO getOrderDetail(Long orderId) {
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        Map<Long, Supplier> supplierMap = new HashMap<>();
        Map<Long, Warehouse> warehouseMap = new HashMap<>();
        Supplier supplier = supplierService.getById(order.getSupplierId());
        if (supplier != null) supplierMap.put(supplier.getId(), supplier);
        Warehouse warehouse = warehouseService.getById(order.getWarehouseId());
        if (warehouse != null) warehouseMap.put(warehouse.getId(), warehouse);

        PurchaseOrderDTO vo = convertToVO(order, supplierMap, warehouseMap);

        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(
            new LambdaQueryWrapper<PurchaseOrderItem>()
                .eq(PurchaseOrderItem::getOrderId, orderId)
        );
        vo.setItems(items.stream().map(this::convertItemToVO).collect(Collectors.toList()));
        return vo;
    }

    @Transactional
    @Override
    public PurchaseOrder createOrder(CreatePurchaseOrderVO request, Long tenantId, Long userId) {
        Supplier supplier = supplierService.getById(request.getSupplierId());
        if (supplier == null) {
            throw new BusinessException("供应商不存在或已停用");
        }
        if (!supplier.getTenantId().equals(tenantId)) {
            throw new BusinessException("供应商不属于当前租户");
        }

        Warehouse warehouse = warehouseService.getById(request.getWarehouseId());
        if (warehouse == null || warehouse.getStatus() == 0) {
            throw new BusinessException("仓库不存在或已停用");
        }

        Map<Long, Product> productCache = new HashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreatePurchaseOrderVO.OrderItemRequest itemReq : request.getItems()) {
            Product product = productService.getById(itemReq.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new BusinessException("商品不存在或已停用: " + itemReq.getProductId());
            }
            productCache.put(product.getId(), product);
            BigDecimal lineAmount = calculateLineAmount(itemReq);
            totalAmount = totalAmount.add(lineAmount);
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setTenantId(tenantId);
        order.setOrderNo(generateOrderNo(tenantId));
        order.setSupplierId(request.getSupplierId());
        order.setWarehouseId(request.getWarehouseId());
        order.setOrderDate(LocalDateTime.now());
        order.setExpectedDate(request.getExpectedDate());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setOrderStatus(0);
        order.setHandlerId(request.getHandlerId());
        order.setRemark(request.getRemark());
        save(order);

        for (CreatePurchaseOrderVO.OrderItemRequest itemReq : request.getItems()) {
            Product product = productCache.get(itemReq.getProductId());
            PurchaseOrderItem item = buildOrderItem(order, product, itemReq);
            purchaseOrderItemMapper.insert(item);
        }

        log.info("创建采购订单: {}", order.getOrderNo());
        return order;
    }

    @Transactional
    @Override
    public PurchaseOrder updateOrder(Long orderId, CreatePurchaseOrderVO request, Long userId) {
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 0) {
            throw new BusinessException("只有草稿状态的订单可以编辑");
        }

        purchaseOrderItemMapper.delete(
            new LambdaQueryWrapper<PurchaseOrderItem>()
                .eq(PurchaseOrderItem::getOrderId, orderId)
        );

        Map<Long, Product> productCache = new HashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreatePurchaseOrderVO.OrderItemRequest itemReq : request.getItems()) {
            Product product = productService.getById(itemReq.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new BusinessException("商品不存在或已停用: " + itemReq.getProductId());
            }
            productCache.put(product.getId(), product);
            BigDecimal lineAmount = calculateLineAmount(itemReq);
            totalAmount = totalAmount.add(lineAmount);

            PurchaseOrderItem item = buildOrderItem(order, product, itemReq);
            purchaseOrderItemMapper.insert(item);
        }

        order.setSupplierId(request.getSupplierId());
        order.setWarehouseId(request.getWarehouseId());
        order.setExpectedDate(request.getExpectedDate());
        order.setHandlerId(request.getHandlerId());
        order.setRemark(request.getRemark());
        order.setTotalAmount(totalAmount);
        updateById(order);

        log.info("更新采购订单: {}", order.getOrderNo());
        return order;
    }

    @Transactional
    @Override
    public void submitForApproval(Long orderId) {
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 0) {
            throw new BusinessException("只有草稿状态的订单可以提交审核");
        }
        order.setOrderStatus(1);
        updateById(order);
        log.info("提交采购订单审核: {}", order.getOrderNo());
    }

    @Transactional
    @Override
    public void approveOrder(Long orderId, Long approverId) {
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 1) {
            throw new BusinessException("只有待审核状态的订单可以审核");
        }
        order.setOrderStatus(2);
        order.setApprovedBy(approverId);
        order.setApprovedAt(LocalDateTime.now());
        updateById(order);
        log.info("审核通过采购订单: {}", order.getOrderNo());
    }

    @Transactional
    @Override
    public void cancelOrder(Long orderId, Long userId) {
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() == 4) {
            throw new BusinessException("已完成的订单不能取消");
        }
        if (order.getOrderStatus() == 5) {
            throw new BusinessException("订单已取消");
        }
        order.setOrderStatus(5);
        updateById(order);
        log.info("取消采购订单: {}", order.getOrderNo());
    }

    @Transactional
    @Override
    public void inbound(Long orderId, PurchaseInboundVO request, Long tenantId, Long userId) {
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 2) {
            throw new BusinessException("只有已审核的订单可以入库");
        }

        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(
            new LambdaQueryWrapper<PurchaseOrderItem>()
                .eq(PurchaseOrderItem::getOrderId, orderId)
        );
        Map<Long, PurchaseOrderItem> itemMap = items.stream()
            .collect(Collectors.toMap(PurchaseOrderItem::getId, i -> i));

        boolean allReceived = true;

        for (PurchaseInboundVO.InboundItemRequest itemReq : request.getItems()) {
            PurchaseOrderItem item = itemMap.get(itemReq.getItemId());
            if (item == null) {
                throw new BusinessException("订单明细不存在: " + itemReq.getItemId());
            }

            BigDecimal receivable = item.getQuantity().subtract(item.getReceivedQuantity());
            if (itemReq.getQuantity().compareTo(receivable) > 0) {
                throw new BusinessException("入库数量超过可入库数量，商品: " + item.getProductName());
            }

            item.setReceivedQuantity(item.getReceivedQuantity().add(itemReq.getQuantity()));
            item.setReceivedAmount(item.getReceivedAmount().add(
                itemReq.getQuantity().multiply(item.getPrice())
            ));
            purchaseOrderItemMapper.updateById(item);

            String bizNo = "PO-IN-" + order.getOrderNo();
            InboundVO inboundReq = new InboundVO();
            inboundReq.setWarehouseId(order.getWarehouseId());
            inboundReq.setProductId(item.getProductId());
            inboundReq.setQuantity(itemReq.getQuantity());
            inboundReq.setCostPrice(item.getPrice());
            inboundReq.setBizType(1);
            inboundReq.setBizNo(bizNo);
            inboundReq.setBizId(orderId);
            inventoryServiceClient.inbound(inboundReq, tenantId, userId);

            if (item.getReceivedQuantity().compareTo(item.getQuantity()) < 0) {
                allReceived = false;
            }
        }

        if (allReceived) {
            order.setOrderStatus(4);
        } else {
            order.setOrderStatus(3);
        }
        updateById(order);

        log.info("采购入库成功: 订单号={}, 全部入库={}", order.getOrderNo(), allReceived);
    }

    private String generateOrderNo(Long tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PO" + dateStr;

        Long count = lambdaQuery()
            .eq(PurchaseOrder::getTenantId, tenantId)
            .likeRight(PurchaseOrder::getOrderNo, prefix)
            .count();

        String seqNo = String.format("%04d", count + 1);
        return prefix + seqNo;
    }

    private BigDecimal calculateLineAmount(CreatePurchaseOrderVO.OrderItemRequest itemReq) {
        BigDecimal discount = itemReq.getDiscountAmount() != null ? itemReq.getDiscountAmount() : BigDecimal.ZERO;
        return itemReq.getQuantity().multiply(itemReq.getPrice()).subtract(discount);
    }

    private PurchaseOrderItem buildOrderItem(PurchaseOrder order, Product product,
                                              CreatePurchaseOrderVO.OrderItemRequest itemReq) {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setTenantId(order.getTenantId());
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setProductCode(product.getProductCode());
        item.setProductName(product.getProductName());
        item.setSpecification(product.getSpecification());
        item.setUnit(product.getUnit());
        item.setQuantity(itemReq.getQuantity());
        item.setPrice(itemReq.getPrice());
        item.setDiscountAmount(itemReq.getDiscountAmount() != null ? itemReq.getDiscountAmount() : BigDecimal.ZERO);
        item.setAmount(calculateLineAmount(itemReq));
        item.setReceivedQuantity(BigDecimal.ZERO);
        item.setReceivedAmount(BigDecimal.ZERO);
        item.setRemark(itemReq.getRemark());
        return item;
    }

    private PurchaseOrderDTO convertToVO(PurchaseOrder order,
                                         Map<Long, Supplier> supplierMap,
                                         Map<Long, Warehouse> warehouseMap) {
        PurchaseOrderDTO vo = new PurchaseOrderDTO();
        BeanUtils.copyProperties(order, vo);
        vo.setOrderStatusText(getOrderStatusText(order.getOrderStatus()));

        Supplier supplier = supplierMap.get(order.getSupplierId());
        if (supplier != null) {
            vo.setSupplierName(supplier.getSupplierName());
        }

        Warehouse warehouse = warehouseMap.get(order.getWarehouseId());
        if (warehouse != null) {
            vo.setWarehouseName(warehouse.getWarehouseName());
        }

        return vo;
    }

    private PurchaseOrderItemDTO convertItemToVO(PurchaseOrderItem item) {
        PurchaseOrderItemDTO vo = new PurchaseOrderItemDTO();
        BeanUtils.copyProperties(item, vo);
        vo.setReceivableQuantity(item.getQuantity().subtract(item.getReceivedQuantity()));
        return vo;
    }

    private String getOrderStatusText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "待审核";
            case 2 -> "已审核";
            case 3 -> "已入库";
            case 4 -> "已完成";
            case 5 -> "已取消";
            default -> "未知";
        };
    }
}
