package com.example.erp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erp.api.dto.SalesOrderItemDTO;
import com.example.erp.api.dto.SalesShipmentDTO;
import com.example.erp.api.vo.CreateShipmentVO;
import com.example.erp.entity.SalesShipment;

import java.util.List;

/**
 * 销售出库单服务接口
 */
public interface SalesShipmentService extends IService<SalesShipment> {

    Page<SalesShipmentDTO> getShipmentPage(Long tenantId, int page, int size,
                                            String shipmentNo, Long orderId, Integer status);

    SalesShipmentDTO getShipmentDetail(Long shipmentId);

    List<SalesOrderItemDTO> getShippableItems(Long orderId);

    SalesShipment createShipment(CreateShipmentVO request, Long userId);

    void approveShipment(Long shipmentId, Long approverId);

    void cancelShipment(Long shipmentId, Long userId);
}
