package com.example.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erp.entity.SupplierContact;

import java.util.List;

/**
 * 供应商联系人服务接口
 */
public interface SupplierContactService extends IService<SupplierContact> {

    List<SupplierContact> getContactsBySupplierId(Long supplierId);

    SupplierContact createContact(SupplierContact contact);

    SupplierContact updateContact(SupplierContact contact);

    void deleteContact(Long id);

    void setDefaultContact(Long id);
}
