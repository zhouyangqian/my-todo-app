package com.example.erp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erp.entity.Supplier;

/**
 * 供应商服务接口
 */
public interface SupplierService extends IService<Supplier> {

    Page<Supplier> getSupplierPage(Long tenantId, int page, int size,
                                    String supplierName, Integer status);

    Supplier getByCode(String supplierCode, Long tenantId);

    Supplier createSupplier(Supplier supplier);

    Supplier updateSupplier(Supplier supplier);

    void deleteSupplier(Long id);
}
