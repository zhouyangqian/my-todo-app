package com.example.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erp.entity.CustomerContact;

import java.util.List;

/**
 * 客户联系人服务接口
 */
public interface CustomerContactService extends IService<CustomerContact> {

    List<CustomerContact> getContactsByCustomerId(Long customerId);

    CustomerContact createContact(CustomerContact contact);

    CustomerContact updateContact(CustomerContact contact);

    void deleteContact(Long id);

    void setDefaultContact(Long id);
}
