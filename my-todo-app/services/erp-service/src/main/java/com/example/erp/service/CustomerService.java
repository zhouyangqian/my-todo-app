package com.example.erp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erp.entity.Customer;

/**
 * 客户服务接口
 */
public interface CustomerService extends IService<Customer> {

    Page<Customer> getCustomerPage(Long tenantId, int page, int size,
                                    String customerName, Integer status);

    Customer getByCode(String customerCode, Long tenantId);

    Customer createCustomer(Customer customer);

    Customer updateCustomer(Customer customer);

    void deleteCustomer(Long id);
}
