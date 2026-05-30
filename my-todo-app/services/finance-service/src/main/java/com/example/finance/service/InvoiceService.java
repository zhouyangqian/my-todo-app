package com.example.finance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.finance.entity.Invoice;

import java.util.Map;

/**
 * 发票服务接口
 */
public interface InvoiceService extends IService<Invoice> {

    Page<Invoice> getPage(Long tenantId, int page, int size,
                          Integer invoiceType, Integer invoiceDirection, Integer status);

    Invoice getInvoiceById(Long id);

    Invoice create(Invoice invoice);

    Invoice update(Invoice invoice);

    void delete(Long id);

    void voidInvoice(Long id, Long userId);

    Map<String, Object> getStatistics(Long tenantId);
}
