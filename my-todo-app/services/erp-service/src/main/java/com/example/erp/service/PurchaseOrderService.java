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
 * 采购订单服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderService extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> {

    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final SupplierService supplierService;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final InventoryFeignClient inventoryServiceClient;

    /**
     * 分页查询采购订单
     */
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

        // 批量查询关联数据，避免 N+1
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

    /**
     * 获取订单详情（含明细）
     */
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

    /**
     * 创建采购订单（含明细）
     */
    @Transactional
    public PurchaseOrder createOrder(CreatePurchaseOrderVO request, Long tenantId, Long userId) {
        // 校验供应商
        Supplier supplier = supplierService.getById(request.getSupplierId());
        if (supplier == null) {
            throw new BusinessException("供应商不存在或已停用");
        }
        if (!supplier.getTenantId().equals(tenantId)) {
            throw new BusinessException("供应商不属于当前租户");
        }

        // 校验仓库
        Warehouse warehouse = warehouseService.getById(request.getWarehouseId());
        if (warehouse == null || warehouse.getStatus() == 0) {
            throw new BusinessException("仓库不存在或已停用");
        }

        // 校验商品并缓存，避免重复查询
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

        // 创建订单
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

        // 创建订单明细（使用缓存的商品数据）
        for (CreatePurchaseOrderVO.OrderItemRequest itemReq : request.getItems()) {
            Product product = productCache.get(itemReq.getProductId());
            PurchaseOrderItem item = buildOrderItem(order, product, itemReq);
            purchaseOrderItemMapper.insert(item);
        }

        log.info("创建采购订单: {}", order.getOrderNo());
        return order;
    }

    /**
     * 更新采购订单（仅草稿状态可编辑）
     */
    @Transactional
    public PurchaseOrder updateOrder(Long orderId, CreatePurchaseOrderVO request, Long userId) {
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 0) {
            throw new BusinessException("只有草稿状态的订单可以编辑");
        }

        // 删除原明细
        purchaseOrderItemMapper.delete(
            new LambdaQueryWrapper<PurchaseOrderItem>()
                .eq(PurchaseOrderItem::getOrderId, orderId)
        );

        // 校验商品并缓存
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

        // 更新订单
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

    /**
     * 提交审核
     */
    @Transactional
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

    /**
     * 审核订单
     */
    @Transactional
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

    /**
     * 取消订单
     */
    @Transactional
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

    /**
     * 采购入库
     * <p>
     * 处理流程：
     * 1. 校验订单状态必须为"已审核"(status=2)
     * 2. 遍历入库明细，更新订单明细的已入库数量
     * 3. 调用库存服务增加库存
     * 4. 如果所有商品全部入库，更新订单状态为"已入库"(status=3)
     * </p>
     *
     * @param orderId   订单ID
     * @param request   入库请求（包含各商品的实收数量）
     * @param tenantId  租户ID
     * @param userId    操作人ID
     */
    @Transactional
    public void inbound(Long orderId, PurchaseInboundVO request, Long tenantId, Long userId) {
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 2) {
            throw new BusinessException("只有已审核的订单可以入库");
        }

        // 查询订单明细
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

            // 校验入库数量不超过可入库数量
            BigDecimal receivable = item.getQuantity().subtract(item.getReceivedQuantity());
            if (itemReq.getQuantity().compareTo(receivable) > 0) {
                throw new BusinessException("入库数量超过可入库数量，商品: " + item.getProductName());
            }

            // 更新已入库数量
            item.setReceivedQuantity(item.getReceivedQuantity().add(itemReq.getQuantity()));
            item.setReceivedAmount(item.getReceivedAmount().add(
                itemReq.getQuantity().multiply(item.getPrice())
            ));
            purchaseOrderItemMapper.updateById(item);

            // 调用库存服务入库
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

            // 检查是否全部入库
            if (item.getReceivedQuantity().compareTo(item.getQuantity()) < 0) {
                allReceived = false;
            }
        }

        // 更新订单状态
        if (allReceived) {
            order.setOrderStatus(4); // 已完成
        } else {
            order.setOrderStatus(3); // 部分入库
        }
        updateById(order);

        log.info("采购入库成功: 订单号={}, 全部入库={}", order.getOrderNo(), allReceived);
    }

    /**
     * 生成订单编号 格式: PO + yyyyMMdd + 4位序号
     */
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

    /**
     * 计算行金额 = 数量 * 单价 - 折扣
     */
    private BigDecimal calculateLineAmount(CreatePurchaseOrderVO.OrderItemRequest itemReq) {
        BigDecimal discount = itemReq.getDiscountAmount() != null ? itemReq.getDiscountAmount() : BigDecimal.ZERO;
        return itemReq.getQuantity().multiply(itemReq.getPrice()).subtract(discount);
    }

    /**
     * 构建订单明细实体
     */
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

    /**
     * 转换订单为VO（使用预加载的关联数据）
     */
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
