package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.exception.BusinessException;
import com.example.erp.dto.AccountReceivableCreateRequest;
import com.example.erp.dto.SalesOrderVO;
import com.example.erp.dto.SalesShipmentCreateRequest;
import com.example.erp.dto.SalesShipmentVO;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Product;
import com.example.erp.entity.SalesOrder;
import com.example.erp.entity.SalesOrderItem;
import com.example.erp.entity.SalesShipment;
import com.example.erp.entity.SalesShipmentItem;
import com.example.erp.entity.Warehouse;
import com.example.erp.dto.OutboundRequest;
import com.example.erp.feign.FinanceServiceClient;
import com.example.erp.feign.InventoryServiceClient;
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
 * 销售出库单服务类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供销售出库单相关的核心业务逻辑：
 * - 销售出库单分页查询
 * - 获取订单的可发货商品列表
 * - 销售出库单创建（从订单选择未发货商品）
 * - 销售出库单审核（触发库存扣减和应收生成）
 * - 销售出库单取消
 * - 出库单详情查询
 * - 出库单号生成
 * </p>
 * <p>
 * 关键业务逻辑：
 * - 审核出库单时调用 InventoryService.outbound() 扣减库存
 * - 审核出库单时调用 FinanceServiceClient.createReceivable() 生成应收账款
 * - 更新销售订单明细的已发货数量和金额
 * - 更新销售订单的已发货总数和金额
 * - 全部发货完成后更新订单状态为"已出库"
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesShipmentService extends ServiceImpl<SalesShipmentMapper, SalesShipment> {

    private final SalesShipmentItemMapper salesShipmentItemMapper;
    private final SalesOrderMapper salesOrderMapper;
    private final SalesOrderItemMapper salesOrderItemMapper;
    private final CustomerService customerService;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final InventoryServiceClient inventoryServiceClient;
    private final SalesOrderService salesOrderService;
    private final FinanceServiceClient financeServiceClient;

    /**
     * 分页查询出库单列表
     *
     * @param tenantId   租户ID
     * @param page       当前页码
     * @param size       每页条数
     * @param shipmentNo 出库单号（可选，模糊搜索）
     * @param orderId    订单ID（可选）
     * @param status     出库状态（可选）
     * @return 出库单分页数据
     */
    public Page<SalesShipmentVO> getShipmentPage(Long tenantId, int page, int size,
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

        // 转换为 VO
        Page<SalesShipmentVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<SalesShipmentVO> voList = result.getRecords().stream().map(shipment -> {
            SalesShipmentVO vo = convertToVO(shipment);
            // 获取出库明细
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

    /**
     * 获取出库单详情（含明细）
     *
     * @param shipmentId 出库单ID
     * @return 出库单VO对象
     */
    public SalesShipmentVO getShipmentDetail(Long shipmentId) {
        SalesShipment shipment = getById(shipmentId);
        if (shipment == null) {
            throw new BusinessException("出库单不存在");
        }
        SalesShipmentVO vo = convertToVO(shipment);

        // 获取出库明细
        List<SalesShipmentItem> items = salesShipmentItemMapper.selectList(
            new LambdaQueryWrapper<SalesShipmentItem>()
                .eq(SalesShipmentItem::getShipmentId, shipmentId)
                .eq(SalesShipmentItem::getDeleted, 0)
        );
        vo.setItems(items.stream().map(this::convertItemToVO).collect(Collectors.toList()));
        return vo;
    }

    /**
     * 获取订单的可发货商品列表
     *
     * @param orderId 订单ID
     * @return 可发货的订单明细列表
     */
    public List<SalesOrderVO.SalesOrderItemVO> getShippableItems(Long orderId) {
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
                SalesOrderVO.SalesOrderItemVO vo = new SalesOrderVO.SalesOrderItemVO();
                BeanUtils.copyProperties(item, vo);
                vo.setShippableQuantity(item.getQuantity().subtract(item.getDeliveredQuantity()));
                return vo;
            })
            .collect(Collectors.toList());
    }

    /**
     * 创建出库单（从订单选择未发货商品）
     *
     * @param request 创建请求
     * @param userId  当前用户ID
     * @return 创建的出库单
     */
    @Transactional
    public SalesShipment createShipment(SalesShipmentCreateRequest request, Long userId) {
        // 校验订单
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

        // 校验客户和仓库
        Customer customer = customerService.getById(order.getCustomerId());
        Warehouse warehouse = warehouseService.getById(order.getWarehouseId());

        // 计算出库总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SalesShipmentCreateRequest.ShipmentItemRequest itemReq : request.getItems()) {
            SalesOrderItem orderItem = salesOrderItemMapper.selectById(itemReq.getOrderItemId());
            if (orderItem == null || !orderItem.getOrderId().equals(order.getId())) {
                throw new BusinessException("订单明细不存在或不属于该订单");
            }
            BigDecimal shippableQty = orderItem.getQuantity().subtract(orderItem.getDeliveredQuantity());
            if (itemReq.getQuantity().compareTo(shippableQty) > 0) {
                throw new BusinessException("出库数量超过可发货数量");
            }

            // 使用订单明细的单价
            BigDecimal lineAmount = itemReq.getQuantity().multiply(orderItem.getPrice());
            totalAmount = totalAmount.add(lineAmount);
        }

        // 创建出库单
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
        shipment.setShipmentStatus(0); // 草稿
        shipment.setRemark(request.getRemark());
        save(shipment);

        // 创建出库明细
        for (SalesShipmentCreateRequest.ShipmentItemRequest itemReq : request.getItems()) {
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

    /**
     * 审核出库单（触发库存扣减和应收生成）
     * <p>
     * 事务边界：
     * 1. 校验出库单和订单状态
     * 2. 对每个明细调用 InventoryService.outbound() 扣减库存
     * 3. 创建 AccountReceivable 生成应收账款
     * 4. 更新 SalesOrderItem 的已发货数量和金额
     * 5. 更新 SalesOrder 的已发货总数和金额
     * 6. 更新订单状态（如全部发货）
     * 7. 更新出库单状态
     * </p>
     *
     * @param shipmentId 出库单ID
     * @param approverId 审核人ID
     */
    @Transactional
    public void approveShipment(Long shipmentId, Long approverId) {
        SalesShipment shipment = getById(shipmentId);
        if (shipment == null) {
            throw new BusinessException("出库单不存在");
        }
        if (shipment.getShipmentStatus() != 0 && shipment.getShipmentStatus() != 1) {
            throw new BusinessException("只有草稿或待审核状态的出库单可以审核");
        }

        // 校验订单状态
        SalesOrder order = salesOrderMapper.selectById(shipment.getOrderId());
        if (order.getOrderStatus() < 2) {
            throw new BusinessException("订单未审核");
        }

        // 获取客户信息（用于计算应收账款到期日）
        Customer customer = customerService.getById(shipment.getCustomerId());

        // 获取出库明细
        List<SalesShipmentItem> shipmentItems = salesShipmentItemMapper.selectList(
            new LambdaQueryWrapper<SalesShipmentItem>()
                .eq(SalesShipmentItem::getShipmentId, shipmentId)
                .eq(SalesShipmentItem::getDeleted, 0)
        );

        // 1. 扣减库存
        for (SalesShipmentItem item : shipmentItems) {
            OutboundRequest outboundReq = new OutboundRequest();
            outboundReq.setWarehouseId(shipment.getWarehouseId());
            outboundReq.setProductId(item.getProductId());
            outboundReq.setQuantity(item.getQuantity());
            outboundReq.setBizType(2);
            outboundReq.setBizNo(shipment.getShipmentNo());
            outboundReq.setBizId(shipment.getId());
            inventoryServiceClient.outbound(outboundReq, shipment.getTenantId(), approverId);
        }

        // 2. 创建应收账款
        AccountReceivableCreateRequest receivableRequest = new AccountReceivableCreateRequest();
        receivableRequest.setTenantId(shipment.getTenantId());
        receivableRequest.setBizNo(shipment.getShipmentNo());
        receivableRequest.setCustomerId(shipment.getCustomerId());
        receivableRequest.setAmount(shipment.getReceivedAmount());
        receivableRequest.setUnreceivedAmount(shipment.getReceivedAmount());
        receivableRequest.setBizDate(shipment.getShipmentDate().toLocalDate());
        // 计算到期日：发货日期 + 客户账期天数
        LocalDateTime dueDate = shipment.getShipmentDate().plusDays(customer.getCreditDays() != null ? customer.getCreditDays() : 0);
        receivableRequest.setDueDate(dueDate.toLocalDate());
        financeServiceClient.createReceivable(receivableRequest);

        // 3-5. 更新订单明细和订单的已发货信息
        BigDecimal totalDeliveredQty = BigDecimal.ZERO;
        BigDecimal totalDeliveredAmount = BigDecimal.ZERO;

        for (SalesShipmentItem shipmentItem : shipmentItems) {
            SalesOrderItem orderItem = salesOrderItemMapper.selectById(shipmentItem.getOrderItemId());
            if (orderItem != null) {
                // 更新订单明细的已发货数量和金额
                BigDecimal newDeliveredQty = orderItem.getDeliveredQuantity().add(shipmentItem.getQuantity());
                BigDecimal newDeliveredAmount = orderItem.getDeliveredAmount().add(shipmentItem.getAmount());
                orderItem.setDeliveredQuantity(newDeliveredQty);
                orderItem.setDeliveredAmount(newDeliveredAmount);
                salesOrderItemMapper.updateById(orderItem);

                totalDeliveredQty = totalDeliveredQty.add(shipmentItem.getQuantity());
                totalDeliveredAmount = totalDeliveredAmount.add(shipmentItem.getAmount());
            }
        }

        // 更新订单的已发货总数和金额
        order.setDeliveredQuantity(order.getDeliveredQuantity().add(totalDeliveredQty));
        order.setDeliveredAmount(order.getDeliveredAmount().add(totalDeliveredAmount));

        // 6. 检查是否全部发货完成
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

        // 更新订单状态
        if (fullyDelivered) {
            order.setOrderStatus(4); // 已完成
        } else {
            order.setOrderStatus(3); // 已出库（部分发货）
        }
        salesOrderMapper.updateById(order);

        // 7. 更新出库单状态
        shipment.setShipmentStatus(2); // 已出库
        updateById(shipment);

        log.info("审核销售出库单: {}", shipment.getShipmentNo());
    }

    /**
     * 取消出库单
     *
     * @param shipmentId 出库单ID
     * @param userId     当前用户ID
     */
    @Transactional
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
        shipment.setShipmentStatus(3); // 已取消
        updateById(shipment);
        log.info("取消销售出库单: {}", shipment.getShipmentNo());
    }

    /**
     * 生成出库单号
     * 格式: SS + yyyyMMdd + 4位序号
     *
     * @param tenantId 租户ID
     * @return 出库单号
     */
    private String generateShipmentNo(Long tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "SS" + dateStr;

        // 查询今天同前缀的出库单数量
        Long count = lambdaQuery()
            .eq(SalesShipment::getTenantId, tenantId)
            .likeRight(SalesShipment::getShipmentNo, prefix)
            .count();

        String seqNo = String.format("%04d", count + 1);
        return prefix + seqNo;
    }

    /**
     * 转换出库单为VO
     */
    private SalesShipmentVO convertToVO(SalesShipment shipment) {
        SalesShipmentVO vo = new SalesShipmentVO();
        BeanUtils.copyProperties(shipment, vo);
        vo.setShipmentStatusText(getShipmentStatusText(shipment.getShipmentStatus()));

        // 获取客户名称
        Customer customer = customerService.getById(shipment.getCustomerId());
        if (customer != null) {
            vo.setCustomerName(customer.getCustomerName());
        }

        // 获取仓库名称
        Warehouse warehouse = warehouseService.getById(shipment.getWarehouseId());
        if (warehouse != null) {
            vo.setWarehouseName(warehouse.getWarehouseName());
        }

        return vo;
    }

    /**
     * 转换出库明细为VO
     */
    private SalesShipmentVO.SalesShipmentItemVO convertItemToVO(SalesShipmentItem item) {
        SalesShipmentVO.SalesShipmentItemVO vo = new SalesShipmentVO.SalesShipmentItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }

    /**
     * 获取出库状态文本
     */
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
