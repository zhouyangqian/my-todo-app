package com.example.erp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erp.api.dto.SalesOrderDTO;
import com.example.erp.api.vo.CreateSalesOrderVO;
import com.example.erp.entity.SalesOrder;

/**
 * 销售订单服务接口
 */
public interface SalesOrderService extends IService<SalesOrder> {

    Page<SalesOrderDTO> getOrderPage(Long tenantId, int page, int size,
                                      String orderNo, Long customerId, Integer status);

    SalesOrderDTO getOrderDetail(Long orderId);

    SalesOrder createOrder(CreateSalesOrderVO request, Long userId);

    SalesOrder updateOrder(Long orderId, CreateSalesOrderVO request, Long userId);

    void submitForApproval(Long orderId);

    void approveOrder(Long orderId, Long approverId);

    void cancelOrder(Long orderId, Long userId);
}
