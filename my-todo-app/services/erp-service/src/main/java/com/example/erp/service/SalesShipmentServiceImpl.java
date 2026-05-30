package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.exception.BusinessException;
import com.example.erp.api.vo.CreateReceivableVO;
import com.example.erp.api.dto.SalesOrderDTO;
import com.example.erp.api.dto.SalesOrderItemDTO;
import com.example.erp.api.dto.SalesShipmentItemDTO;
import com.example.erp.api.vo.CreateShipmentVO;
import com.example.erp.api.dto.SalesShipmentDTO;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Product;
import com.example.erp.entity.SalesOrder;
import com.example.erp.entity.SalesOrderItem;
import com.example.erp.entity.SalesShipment;
import com.example.erp.entity.SalesShipmentItem;
import com.example.erp.entity.Warehouse;
import com.example.inventory.api.vo.OutboundVO;
import com.example.erp.api.feign.FinanceFeignClient;
import com.example.erp.api.feign.InventoryFeignClient;
import com.example.erp.mapper.SalesOrderItemMapper;
import com.example.erp.mapper.SalesOrderMapper;
import com.example.erp.mapper.SalesShipmentItemMapper;
import com.example.erp.mapper.SalesShipmentMapper;
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
 * 销售出库单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesShipmentServiceImpl extends ServiceImpl<SalesShipmentMapper, SalesShipment> implements SalesShipmentService {

    private final SalesShipmentItemMapper salesShipmentItemMapper;
    private final SalesOrderMapper salesOrderMapper;
    private final SalesOrderItemMapper salesOrderItemMapper;
    private final CustomerService customerService;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final InventoryFeignClient inventoryServiceClient;
    private final SalesOrderService salesOrderService;
    private final FinanceFeignClient financeServiceClient;

    @Override
    public Page<SalesShipmentDTO> getShipmentPage(Long tenantId, int page, int size,
                                                  String shipmentNo, Long orderId, Integer status) {
        LambdaQueryWrapper<SalesShipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesShipment::getTenantId, tenantId)
               .eq(SalesShipment::getDeleted, 0);
        if (shipmentNo != null && !shipmentNo.isEmpty()) {
            wrapper.like(SalesShipment::getShipmentNo, shipmentNo);
        }
        if (orderId != null) {
            wrapper.eq(SalesShipment::getOrderId, orderId);
        }
        if (status != null) {
            wrapper.eq(SalesShipment::getShipmentStatus, status);
        }
        wrapper.orderByDesc(SalesShipment::getCreatedAt);

        Page<SalesShipment> result = page(new Page<>(page, size), wrapper);

        Page<SalesShipmentDTO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<SalesShipmentDTO> voList = result.getRecords().stream().map(shipment -> {
            SalesShipmentDTO vo = convertToVO(shipment);
            List<SalesShipmentItem> items = salesShipmentItemMapper.selectList(
                new LambdaQueryWrapper<SalesShipmentItem>()
                    .eq(SalesShipmentItem::getShipmentId, shipment.getId())
                    .eq(SalesShipmentItem::getDeleted, 0)
            );
            vo.setItems(items.stream().map(this::convertItemToVO).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public SalesShipmentDTO getShipmentDetail(Long shipmentId) {
        SalesShipment shipment = getById(shipmentId);
        if (shipment == null) {
            throw new BusinessException("出库单不存在");
        }
        SalesShipmentDTO vo = convertToVO(shipment);

        List<SalesShipmentItem> items = salesShipmentItemMapper.selectList(
            new LambdaQueryWrapper<SalesShipmentItem>()
                .eq(SalesShipmentItem::getShipmentId, shipmentId)
                .eq(SalesShipmentItem::getDeleted, 0)
        );
        vo.setItems(items.stream().map(this::convertItemToVO).collect(Collectors.toList()));
        return vo;
    }

    @Override
    public List<SalesOrderItemDTO> getShippableItems(Long orderId) {
        SalesOrder order = salesOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() < 2) {
            throw new BusinessException("订单未审核，无法发货");
        }
        if (order.getOrderStatus() >= 4) {
            throw new BusinessException("订单已完成，无需发货");
        }

        List<SalesOrderItem> items = salesOrderItemMapper.selectList(
            new LambdaQueryWrapper<SalesOrderItem>()
                .eq(SalesOrderItem::getOrderId, orderId)
                .eq(SalesOrderItem::getDeleted, 0)
        );

        return items.stream()
            .filter(item -> item.getQuantity().compareTo(item.getDeliveredQuantity()) > 0)
            .map(item -> {
                SalesOrderItemDTO vo = new SalesOrderItemDTO();
                BeanUtils.copyProperties(item, vo);
                vo.setShippableQuantity(item.getQuantity().subtract(item.getDeliveredQuantity()));
                return vo;
            })
            .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public SalesShipment createShipment(CreateShipmentVO request, Long userId) {
        SalesOrder order = salesOrderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() < 2) {
            throw new BusinessException("订单未审核，无法发货");
        }
        if (order.getOrderStatus() >= 4) {
            throw new BusinessException("订单已完成，无需发货");
        }

        Customer customer = customerService.getById(order.getCustomerId());
        Warehouse warehouse = warehouseService.getById(order.getWarehouseId());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateShipmentVO.ShipmentItemRequest itemReq : request.getItems()) {
            SalesOrderItem orderItem = salesOrderItemMapper.selectById(itemReq.getOrderItemId());
            if (orderItem == null || !orderItem.getOrderId().equals(order.getId())) {
                throw new BusinessException("订单明细不存在或不属于该订单");
            }
            BigDecimal shippableQty = orderItem.getQuantity().subtract(orderItem.getDeliveredQuantity());
            if (itemReq.getQuantity().compareTo(shippableQty) > 0) {
                throw new BusinessException("出库数量超过可发货数量");
            }

            BigDecimal lineAmount = itemReq.getQuantity().multiply(orderItem.getPrice());
            totalAmount = totalAmount.add(lineAmount);
        }

        SalesShipment shipment = new SalesShipment();
        shipment.setTenantId(order.getTenantId());
        shipment.setShipmentNo(generateShipmentNo(order.getTenantId()));
        shipment.setOrderId(order.getId());
        shipment.setOrderNo(order.getOrderNo());
        shipment.setCustomerId(order.getCustomerId());
        shipment.setWarehouseId(order.getWarehouseId());
        shipment.setShipmentDate(request.getShipmentDate() != null ? request.getShipmentDate() : LocalDateTime.now());
        shipment.setTotalAmount(totalAmount);
        shipment.setDiscountAmount(BigDecimal.ZERO);
        shipment.setReceivedAmount(totalAmount);
        shipment.setShipmentStatus(0);
        shipment.setRemark(request.getRemark());
        save(shipment);

        for (CreateShipmentVO.ShipmentItemRequest itemReq : request.getItems()) {
            SalesOrderItem orderItem = salesOrderItemMapper.selectById(itemReq.getOrderItemId());
            Product product = productService.getById(orderItem.getProductId());

            SalesShipmentItem item = new SalesShipmentItem();
            item.setTenantId(order.getTenantId());
            item.setShipmentId(shipment.getId());
            item.setOrderItemId(orderItem.getId());
            item.setProductId(product.getId());
            item.setProductCode(product.getProductCode());
            item.setProductName(product.getProductName());
            item.setSpecification(product.getSpecification());
            item.setUnit(product.getUnit());
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(orderItem.getPrice());
            item.setDiscountAmount(BigDecimal.ZERO);
            item.setAmount(itemReq.getQuantity().multiply(orderItem.getPrice()));
            item.setRemark(itemReq.getRemark());
            salesShipmentItemMapper.insert(item);
        }

        log.info("创建销售出库单: {}", shipment.getShipmentNo());
        return shipment;
    }

    @Transactional
    @Override
    public void approveShipment(Long shipmentId, Long approverId) {
        SalesShipment shipment = getById(shipmentId);
        if (shipment == null) {
            throw new BusinessException("出库单不存在");
        }
        if (shipment.getShipmentStatus() != 0 && shipment.getShipmentStatus() != 1) {
            throw new BusinessException("只有草稿或待审核状态的出库单可以审核");
        }

        SalesOrder order = salesOrderMapper.selectById(shipment.getOrderId());
        if (order.getOrderStatus() < 2) {
            throw new BusinessException("订单未审核");
        }

        Customer customer = customerService.getById(shipment.getCustomerId());

        List<SalesShipmentItem> shipmentItems = salesShipmentItemMapper.selectList(
            new LambdaQueryWrapper<SalesShipmentItem>()
                .eq(SalesShipmentItem::getShipmentId, shipmentId)
                .eq(SalesShipmentItem::getDeleted, 0)
        );

        for (SalesShipmentItem item : shipmentItems) {
            OutboundVO outboundReq = new OutboundVO();
            outboundReq.setWarehouseId(shipment.getWarehouseId());
            outboundReq.setProductId(item.getProductId());
            outboundReq.setQuantity(item.getQuantity());
            outboundReq.setBizType(2);
            outboundReq.setBizNo(shipment.getShipmentNo());
            outboundReq.setBizId(shipment.getId());
            inventoryServiceClient.outbound(outboundReq, shipment.getTenantId(), approverId);
        }

        CreateReceivableVO receivableRequest = new CreateReceivableVO();
        receivableRequest.setTenantId(shipment.getTenantId());
        receivableRequest.setBizNo(shipment.getShipmentNo());
        receivableRequest.setCustomerId(shipment.getCustomerId());
        receivableRequest.setAmount(shipment.getReceivedAmount());
        receivableRequest.setUnreceivedAmount(shipment.getReceivedAmount());
        receivableRequest.setBizDate(shipment.getShipmentDate().toLocalDate());
        LocalDateTime dueDate = shipment.getShipmentDate().plusDays(customer.getCreditDays() != null ? customer.getCreditDays() : 0);
        receivableRequest.setDueDate(dueDate.toLocalDate());
        financeServiceClient.createReceivable(receivableRequest);

        BigDecimal totalDeliveredQty = BigDecimal.ZERO;
        BigDecimal totalDeliveredAmount = BigDecimal.ZERO;

        for (SalesShipmentItem shipmentItem : shipmentItems) {
            SalesOrderItem orderItem = salesOrderItemMapper.selectById(shipmentItem.getOrderItemId());
            if (orderItem != null) {
                BigDecimal newDeliveredQty = orderItem.getDeliveredQuantity().add(shipmentItem.getQuantity());
                BigDecimal newDeliveredAmount = orderItem.getDeliveredAmount().add(shipmentItem.getAmount());
                orderItem.setDeliveredQuantity(newDeliveredQty);
                orderItem.setDeliveredAmount(newDeliveredAmount);
                salesOrderItemMapper.updateById(orderItem);

                totalDeliveredQty = totalDeliveredQty.add(shipmentItem.getQuantity());
                totalDeliveredAmount = totalDeliveredAmount.add(shipmentItem.getAmount());
            }
        }

        order.setDeliveredQuantity(order.getDeliveredQuantity().add(totalDeliveredQty));
        order.setDeliveredAmount(order.getDeliveredAmount().add(totalDeliveredAmount));

        boolean fullyDelivered = true;
        List<SalesOrderItem> allOrderItems = salesOrderItemMapper.selectList(
            new LambdaQueryWrapper<SalesOrderItem>()
                .eq(SalesOrderItem::getOrderId, order.getId())
                .eq(SalesOrderItem::getDeleted, 0)
        );
        for (SalesOrderItem orderItem : allOrderItems) {
            if (orderItem.getQuantity().compareTo(orderItem.getDeliveredQuantity()) > 0) {
                fullyDelivered = false;
                break;
            }
        }

        if (fullyDelivered) {
            order.setOrderStatus(4);
        } else {
            order.setOrderStatus(3);
        }
        salesOrderMapper.updateById(order);

        shipment.setShipmentStatus(2);
        updateById(shipment);

        log.info("审核销售出库单: {}", shipment.getShipmentNo());
    }

    @Transactional
    @Override
    public void cancelShipment(Long shipmentId, Long userId) {
        SalesShipment shipment = getById(shipmentId);
        if (shipment == null) {
            throw new BusinessException("出库单不存在");
        }
        if (shipment.getShipmentStatus() == 2) {
            throw new BusinessException("已出库的出库单不能取消，请先冲销");
        }
        if (shipment.getShipmentStatus() == 3) {
            throw new BusinessException("出库单已取消");
        }
        shipment.setShipmentStatus(3);
        updateById(shipment);
        log.info("取消销售出库单: {}", shipment.getShipmentNo());
    }

    private String generateShipmentNo(Long tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "SS" + dateStr;

        Long count = lambdaQuery()
            .eq(SalesShipment::getTenantId, tenantId)
            .likeRight(SalesShipment::getShipmentNo, prefix)
            .count();

        String seqNo = String.format("%04d", count + 1);
        return prefix + seqNo;
    }

    private SalesShipmentDTO convertToVO(SalesShipment shipment) {
        SalesShipmentDTO vo = new SalesShipmentDTO();
        BeanUtils.copyProperties(shipment, vo);
        vo.setShipmentStatusText(getShipmentStatusText(shipment.getShipmentStatus()));

        Customer customer = customerService.getById(shipment.getCustomerId());
        if (customer != null) {
            vo.setCustomerName(customer.getCustomerName());
        }

        Warehouse warehouse = warehouseService.getById(shipment.getWarehouseId());
        if (warehouse != null) {
            vo.setWarehouseName(warehouse.getWarehouseName());
        }

        return vo;
    }

    private SalesShipmentItemDTO convertItemToVO(SalesShipmentItem item) {
        SalesShipmentItemDTO vo = new SalesShipmentItemDTO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }

    private String getShipmentStatusText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "待审核";
            case 2 -> "已出库";
            case 3 -> "已取消";
            default -> "未知";
        };
    }
}
