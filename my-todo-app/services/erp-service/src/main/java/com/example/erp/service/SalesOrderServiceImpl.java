package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.exception.BusinessException;
import com.example.erp.api.vo.CreateSalesOrderVO;
import com.example.erp.api.dto.SalesOrderDTO;
import com.example.erp.api.dto.SalesOrderItemDTO;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Product;
import com.example.erp.entity.SalesOrder;
import com.example.erp.entity.SalesOrderItem;
import com.example.erp.entity.Warehouse;
import com.example.erp.mapper.SalesOrderItemMapper;
import com.example.erp.mapper.SalesOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 销售订单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderServiceImpl extends ServiceImpl<SalesOrderMapper, SalesOrder> implements SalesOrderService {

    private final SalesOrderItemMapper salesOrderItemMapper;
    private final CustomerService customerService;
    private final WarehouseService warehouseService;
    private final ProductService productService;

    @Override
    public Page<SalesOrderDTO> getOrderPage(Long tenantId, int page, int size,
                                            String orderNo, Long customerId, Integer status) {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrder::getTenantId, tenantId)
               .eq(SalesOrder::getDeleted, 0);
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(SalesOrder::getOrderNo, orderNo);
        }
        if (customerId != null) {
            wrapper.eq(SalesOrder::getCustomerId, customerId);
        }
        if (status != null) {
            wrapper.eq(SalesOrder::getOrderStatus, status);
        }
        wrapper.orderByDesc(SalesOrder::getCreatedAt);

        Page<SalesOrder> result = page(new Page<>(page, size), wrapper);

        Page<SalesOrderDTO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<SalesOrderDTO> voList = result.getRecords().stream().map(order -> {
            SalesOrderDTO vo = convertToVO(order);
            List<SalesOrderItem> items = salesOrderItemMapper.selectList(
                new LambdaQueryWrapper<SalesOrderItem>()
                    .eq(SalesOrderItem::getOrderId, order.getId())
                    .eq(SalesOrderItem::getDeleted, 0)
            );
            vo.setItems(items.stream().map(this::convertItemToVO).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public SalesOrderDTO getOrderDetail(Long orderId) {
        SalesOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        SalesOrderDTO vo = convertToVO(order);

        List<SalesOrderItem> items = salesOrderItemMapper.selectList(
            new LambdaQueryWrapper<SalesOrderItem>()
                .eq(SalesOrderItem::getOrderId, orderId)
                .eq(SalesOrderItem::getDeleted, 0)
        );
        vo.setItems(items.stream().map(this::convertItemToVO).collect(Collectors.toList()));
        return vo;
    }

    @Transactional
    @Override
    public SalesOrder createOrder(CreateSalesOrderVO request, Long userId) {
        Customer customer = customerService.getById(request.getCustomerId());
        if (customer == null || customer.getDeleted() == 1) {
            throw new BusinessException("客户不存在或已停用");
        }

        Warehouse warehouse = warehouseService.getById(request.getWarehouseId());
        if (warehouse == null || warehouse.getStatus() == 0) {
            throw new BusinessException("仓库不存在或已停用");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateSalesOrderVO.OrderItemRequest itemReq : request.getItems()) {
            Product product = productService.getById(itemReq.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new BusinessException("商品不存在或已停用: " + itemReq.getProductId());
            }
            BigDecimal lineAmount = itemReq.getQuantity().multiply(itemReq.getPrice())
                .subtract(itemReq.getDiscountAmount() != null ? itemReq.getDiscountAmount() : BigDecimal.ZERO);
            totalAmount = totalAmount.add(lineAmount);
        }

        SalesOrder order = new SalesOrder();
        order.setTenantId(customer.getTenantId());
        order.setOrderNo(generateOrderNo(customer.getTenantId()));
        order.setCustomerId(request.getCustomerId());
        order.setWarehouseId(request.getWarehouseId());
        order.setOrderDate(LocalDateTime.now());
        order.setExpectedDate(request.getExpectedDate());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setReceivedAmount(totalAmount);
        order.setDeliveredAmount(BigDecimal.ZERO);
        order.setDeliveredQuantity(BigDecimal.ZERO);
        order.setOrderStatus(0);
        order.setSalesId(request.getSalesId());
        order.setRemark(request.getRemark());
        save(order);

        for (CreateSalesOrderVO.OrderItemRequest itemReq : request.getItems()) {
            Product product = productService.getById(itemReq.getProductId());
            SalesOrderItem item = new SalesOrderItem();
            item.setTenantId(customer.getTenantId());
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setProductCode(product.getProductCode());
            item.setProductName(product.getProductName());
            item.setSpecification(product.getSpecification());
            item.setUnit(product.getUnit());
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(itemReq.getPrice());
            item.setDiscountAmount(itemReq.getDiscountAmount() != null ? itemReq.getDiscountAmount() : BigDecimal.ZERO);
            item.setAmount(itemReq.getQuantity().multiply(itemReq.getPrice())
                .subtract(itemReq.getDiscountAmount() != null ? itemReq.getDiscountAmount() : BigDecimal.ZERO));
            item.setDeliveredQuantity(BigDecimal.ZERO);
            item.setDeliveredAmount(BigDecimal.ZERO);
            item.setRemark(itemReq.getRemark());
            salesOrderItemMapper.insert(item);
        }

        log.info("创建销售订单: {}", order.getOrderNo());
        return order;
    }

    @Transactional
    @Override
    public SalesOrder updateOrder(Long orderId, CreateSalesOrderVO request, Long userId) {
        SalesOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 0) {
            throw new BusinessException("只有草稿状态的订单可以编辑");
        }

        salesOrderItemMapper.delete(
            new LambdaQueryWrapper<SalesOrderItem>()
                .eq(SalesOrderItem::getOrderId, orderId)
        );

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateSalesOrderVO.OrderItemRequest itemReq : request.getItems()) {
            Product product = productService.getById(itemReq.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new BusinessException("商品不存在或已停用: " + itemReq.getProductId());
            }
            BigDecimal lineAmount = itemReq.getQuantity().multiply(itemReq.getPrice())
                .subtract(itemReq.getDiscountAmount() != null ? itemReq.getDiscountAmount() : BigDecimal.ZERO);
            totalAmount = totalAmount.add(lineAmount);

            SalesOrderItem item = new SalesOrderItem();
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
            item.setAmount(lineAmount);
            item.setDeliveredQuantity(BigDecimal.ZERO);
            item.setDeliveredAmount(BigDecimal.ZERO);
            item.setRemark(itemReq.getRemark());
            salesOrderItemMapper.insert(item);
        }

        order.setCustomerId(request.getCustomerId());
        order.setWarehouseId(request.getWarehouseId());
        order.setExpectedDate(request.getExpectedDate());
        order.setSalesId(request.getSalesId());
        order.setRemark(request.getRemark());
        order.setTotalAmount(totalAmount);
        order.setReceivedAmount(totalAmount);
        updateById(order);

        log.info("更新销售订单: {}", order.getOrderNo());
        return order;
    }

    @Transactional
    @Override
    public void submitForApproval(Long orderId) {
        SalesOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 0) {
            throw new BusinessException("只有草稿状态的订单可以提交审核");
        }
        order.setOrderStatus(1);
        updateById(order);
        log.info("提交销售订单审核: {}", order.getOrderNo());
    }

    @Transactional
    @Override
    public void approveOrder(Long orderId, Long approverId) {
        SalesOrder order = getById(orderId);
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
        log.info("审核通过销售订单: {}", order.getOrderNo());
    }

    @Transactional
    @Override
    public void cancelOrder(Long orderId, Long userId) {
        SalesOrder order = getById(orderId);
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
        log.info("取消销售订单: {}", order.getOrderNo());
    }

    @Override
    public long countByStatus(Long tenantId, Integer status) {
        return lambdaQuery()
            .eq(SalesOrder::getTenantId, tenantId)
            .eq(SalesOrder::getOrderStatus, status)
            .eq(SalesOrder::getDeleted, 0)
            .count();
    }

    @Override
    public BigDecimal sumTotalAmount(Long tenantId) {
        List<SalesOrder> list = lambdaQuery()
            .eq(SalesOrder::getTenantId, tenantId)
            .ne(SalesOrder::getOrderStatus, 5) // 排除已取消
            .eq(SalesOrder::getDeleted, 0)
            .list();
        return list.stream()
            .map(SalesOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateOrderNo(Long tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "SO" + dateStr;

        Long count = lambdaQuery()
            .eq(SalesOrder::getTenantId, tenantId)
            .likeRight(SalesOrder::getOrderNo, prefix)
            .count();

        String seqNo = String.format("%04d", count + 1);
        return prefix + seqNo;
    }

    private SalesOrderDTO convertToVO(SalesOrder order) {
        SalesOrderDTO vo = new SalesOrderDTO();
        BeanUtils.copyProperties(order, vo);
        vo.setOrderStatusText(getOrderStatusText(order.getOrderStatus()));

        Customer customer = customerService.getById(order.getCustomerId());
        if (customer != null) {
            vo.setCustomerName(customer.getCustomerName());
        }

        Warehouse warehouse = warehouseService.getById(order.getWarehouseId());
        if (warehouse != null) {
            vo.setWarehouseName(warehouse.getWarehouseName());
        }

        return vo;
    }

    private SalesOrderItemDTO convertItemToVO(SalesOrderItem item) {
        SalesOrderItemDTO vo = new SalesOrderItemDTO();
        BeanUtils.copyProperties(item, vo);
        vo.setShippableQuantity(item.getQuantity().subtract(item.getDeliveredQuantity()));
        return vo;
    }

    private String getOrderStatusText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "待审核";
            case 2 -> "已审核";
            case 3 -> "已出库";
            case 4 -> "已完成";
            case 5 -> "已取消";
            default -> "未知";
        };
    }
}
