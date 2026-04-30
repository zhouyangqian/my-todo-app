package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.exception.BusinessException;
import com.example.erp.dto.SalesOrderCreateRequest;
import com.example.erp.dto.SalesOrderVO;
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
 * 销售订单服务类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供销售订单相关的核心业务逻辑：
 * - 销售订单分页查询（支持按订单号、客户、状态过滤）
 * - 销售订单创建（含明细）
 * - 销售订单更新（仅草稿状态可编辑）
 * - 销售订单审核
 * - 销售订单取消
 * - 销售订单详情查询（含明细）
 * - 订单编号生成
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderService extends ServiceImpl<SalesOrderMapper, SalesOrder> {

    private final SalesOrderItemMapper salesOrderItemMapper;
    private final CustomerService customerService;
    private final WarehouseService warehouseService;
    private final ProductService productService;

    /**
     * 分页查询销售订单列表
     *
     * @param tenantId  租户ID
     * @param page      当前页码
     * @param size      每页条数
     * @param orderNo   订单编号（可选，模糊搜索）
     * @param customerId 客户ID（可选）
     * @param status    订单状态（可选）
     * @return 销售订单分页数据
     */
    public Page<SalesOrderVO> getOrderPage(Long tenantId, int page, int size,
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

        // 转换为 VO
        Page<SalesOrderVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<SalesOrderVO> voList = result.getRecords().stream().map(order -> {
            SalesOrderVO vo = convertToVO(order);
            // 获取订单明细数量
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

    /**
     * 获取订单详情（含明细）
     *
     * @param orderId 订单ID
     * @return 订单VO对象
     */
    public SalesOrderVO getOrderDetail(Long orderId) {
        SalesOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        SalesOrderVO vo = convertToVO(order);

        // 获取订单明细
        List<SalesOrderItem> items = salesOrderItemMapper.selectList(
            new LambdaQueryWrapper<SalesOrderItem>()
                .eq(SalesOrderItem::getOrderId, orderId)
                .eq(SalesOrderItem::getDeleted, 0)
        );
        vo.setItems(items.stream().map(this::convertItemToVO).collect(Collectors.toList()));
        return vo;
    }

    /**
     * 创建销售订单（含明细）
     *
     * @param request 创建请求
     * @param userId  当前用户ID
     * @return 创建的订单
     */
    @Transactional
    public SalesOrder createOrder(SalesOrderCreateRequest request, Long userId) {
        // 校验客户
        Customer customer = customerService.getById(request.getCustomerId());
        if (customer == null || customer.getDeleted() == 1) {
            throw new BusinessException("客户不存在或已停用");
        }

        // 校验仓库
        Warehouse warehouse = warehouseService.getById(request.getWarehouseId());
        if (warehouse == null || warehouse.getStatus() == 0) {
            throw new BusinessException("仓库不存在或已停用");
        }

        // 校验商品并计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SalesOrderCreateRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productService.getById(itemReq.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new BusinessException("商品不存在或已停用: " + itemReq.getProductId());
            }
            BigDecimal lineAmount = itemReq.getQuantity().multiply(itemReq.getPrice())
                .subtract(itemReq.getDiscountAmount() != null ? itemReq.getDiscountAmount() : BigDecimal.ZERO);
            totalAmount = totalAmount.add(lineAmount);
        }

        // 创建订单
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
        order.setOrderStatus(0); // 草稿
        order.setSalesId(request.getSalesId());
        order.setRemark(request.getRemark());
        save(order);

        // 创建订单明细
        for (SalesOrderCreateRequest.OrderItemRequest itemReq : request.getItems()) {
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

    /**
     * 更新销售订单（仅草稿状态可编辑）
     *
     * @param orderId 订单ID
     * @param request 更新请求
     * @param userId  当前用户ID
     * @return 更新后的订单
     */
    @Transactional
    public SalesOrder updateOrder(Long orderId, SalesOrderCreateRequest request, Long userId) {
        SalesOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 0) {
            throw new BusinessException("只有草稿状态的订单可以编辑");
        }

        // 删除原明细
        salesOrderItemMapper.delete(
            new LambdaQueryWrapper<SalesOrderItem>()
                .eq(SalesOrderItem::getOrderId, orderId)
        );

        // 重新计算并创建明细
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SalesOrderCreateRequest.OrderItemRequest itemReq : request.getItems()) {
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

        // 更新订单
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

    /**
     * 提交审核
     *
     * @param orderId 订单ID
     */
    @Transactional
    public void submitForApproval(Long orderId) {
        SalesOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 0) {
            throw new BusinessException("只有草稿状态的订单可以提交审核");
        }
        order.setOrderStatus(1); // 待审核
        updateById(order);
        log.info("提交销售订单审核: {}", order.getOrderNo());
    }

    /**
     * 审核订单
     *
     * @param orderId    订单ID
     * @param approverId 审核人ID
     */
    @Transactional
    public void approveOrder(Long orderId, Long approverId) {
        SalesOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 1) {
            throw new BusinessException("只有待审核状态的订单可以审核");
        }
        order.setOrderStatus(2); // 已审核
        order.setApprovedBy(approverId);
        order.setApprovedAt(LocalDateTime.now());
        updateById(order);
        log.info("审核通过销售订单: {}", order.getOrderNo());
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param userId  当前用户ID
     */
    @Transactional
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
        order.setOrderStatus(5); // 已取消
        updateById(order);
        log.info("取消销售订单: {}", order.getOrderNo());
    }

    /**
     * 生成订单编号
     * 格式: SO + yyyyMMdd + 4位序号
     *
     * @param tenantId 租户ID
     * @return 订单编号
     */
    private String generateOrderNo(Long tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "SO" + dateStr;

        // 查询今天同前缀的订单数量
        Long count = lambdaQuery()
            .eq(SalesOrder::getTenantId, tenantId)
            .likeRight(SalesOrder::getOrderNo, prefix)
            .count();

        String seqNo = String.format("%04d", count + 1);
        return prefix + seqNo;
    }

    /**
     * 转换订单为VO
     */
    private SalesOrderVO convertToVO(SalesOrder order) {
        SalesOrderVO vo = new SalesOrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setOrderStatusText(getOrderStatusText(order.getOrderStatus()));

        // 获取客户名称
        Customer customer = customerService.getById(order.getCustomerId());
        if (customer != null) {
            vo.setCustomerName(customer.getCustomerName());
        }

        // 获取仓库名称
        Warehouse warehouse = warehouseService.getById(order.getWarehouseId());
        if (warehouse != null) {
            vo.setWarehouseName(warehouse.getWarehouseName());
        }

        return vo;
    }

    /**
     * 转换订单明细为VO
     */
    private SalesOrderVO.SalesOrderItemVO convertItemToVO(SalesOrderItem item) {
        SalesOrderVO.SalesOrderItemVO vo = new SalesOrderVO.SalesOrderItemVO();
        BeanUtils.copyProperties(item, vo);
        // 计算可发货数量 = 订单数量 - 已发货数量
        vo.setShippableQuantity(item.getQuantity().subtract(item.getDeliveredQuantity()));
        return vo;
    }

    /**
     * 获取订单状态文本
     */
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
