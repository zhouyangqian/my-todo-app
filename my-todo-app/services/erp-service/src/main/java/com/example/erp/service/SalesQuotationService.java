package com.example.erp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erp.api.dto.SalesQuotationDTO;
import com.example.erp.api.vo.CreateSalesQuotationVO;
import com.example.erp.entity.SalesOrder;
import com.example.erp.entity.SalesQuotation;

/**
 * 销售报价单服务接口
 */
public interface SalesQuotationService extends IService<SalesQuotation> {

    Page<SalesQuotationDTO> getQuotationPage(Long tenantId, int page, int size,
                                              Long customerId, Integer status,
                                              String startDate, String endDate);

    SalesQuotationDTO getQuotationById(Long quotationId);

    SalesQuotation createQuotation(CreateSalesQuotationVO request, Long tenantId);

    SalesQuotation updateQuotation(Long quotationId, CreateSalesQuotationVO request);

    void sendQuotation(Long quotationId);

    void acceptQuotation(Long quotationId);

    void rejectQuotation(Long quotationId);

    SalesOrder convertToOrder(Long quotationId, Long warehouseId);
}
