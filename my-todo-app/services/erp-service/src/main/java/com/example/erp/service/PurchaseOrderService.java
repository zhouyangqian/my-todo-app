package com.example.erp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erp.api.dto.PurchaseOrderDTO;
import com.example.erp.api.vo.CreatePurchaseOrderVO;
import com.example.erp.api.vo.PurchaseInboundVO;
import com.example.erp.entity.PurchaseOrder;

import java.math.BigDecimal;

/**
 * 采购订单服务接口
 */
public interface PurchaseOrderService extends IService<PurchaseOrder> {

    Page<PurchaseOrderDTO> getOrderPage(Long tenantId, int page, int size,
                                         String orderNo, Long supplierId, Integer status);

    PurchaseOrderDTO getOrderDetail(Long orderId);

    PurchaseOrder createOrder(CreatePurchaseOrderVO request, Long tenantId, Long userId);

    PurchaseOrder updateOrder(Long orderId, CreatePurchaseOrderVO request, Long userId);

    void submitForApproval(Long orderId);

    void approveOrder(Long orderId, Long approverId);

    void cancelOrder(Long orderId, Long userId);

    void inbound(Long orderId, PurchaseInboundVO request, Long tenantId, Long userId);

    long countByStatus(Long tenantId, Integer status);

    BigDecimal sumTotalAmount(Long tenantId);
}
