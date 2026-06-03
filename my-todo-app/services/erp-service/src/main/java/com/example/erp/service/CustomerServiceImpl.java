package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erp.entity.Customer;
import com.example.erp.mapper.CustomerMapper;
import com.example.common.core.util.CodeGenerateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 客户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    @Override
    public Page<Customer> getCustomerPage(Long tenantId, int page, int size,
                                           String customerName, Integer status) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getTenantId, tenantId)
               .eq(Customer::getDeleted, 0);
        if (customerName != null && !customerName.isEmpty()) {
            wrapper.like(Customer::getCustomerName, customerName);
        }
        if (status != null) {
            wrapper.eq(Customer::getStatus, status);
        }
        wrapper.orderByDesc(Customer::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Customer getByCode(String customerCode, Long tenantId) {
        return getOne(
            new LambdaQueryWrapper<Customer>()
                .eq(Customer::getCustomerCode, customerCode)
                .eq(Customer::getTenantId, tenantId)
                .eq(Customer::getDeleted, 0)
        );
    }

    @Transactional
    @Override
    public Customer createCustomer(Customer customer) {
        // 自动生成客户编码（格式：CUS-拼音首字母-时间戳）
        String generatedCode = CodeGenerateUtil.generate("CUS",
                customer.getCustomerName(),
                code -> getByCode(code, customer.getTenantId()) != null
        );
        customer.setCustomerCode(generatedCode);
        save(customer);
        log.info("创建客户: {}", customer.getCustomerCode());
        return customer;
    }

    @Transactional
    @Override
    public Customer updateCustomer(Customer customer) {
        Customer existing = getOne(
            new LambdaQueryWrapper<Customer>()
                .eq(Customer::getCustomerCode, customer.getCustomerCode())
                .eq(Customer::getTenantId, customer.getTenantId())
                .ne(Customer::getId, customer.getId())
                .eq(Customer::getDeleted, 0)
        );
        if (existing != null) {
            throw new IllegalArgumentException("客户编码已存在: " + customer.getCustomerCode());
        }
        updateById(customer);
        log.info("更新客户: {}", customer.getCustomerCode());
        return customer;
    }

    @Transactional
    @Override
    public void deleteCustomer(Long id) {
        Customer customer = getById(id);
        if (customer != null) {
            customer.setDeleted(1);
            updateById(customer);
            log.info("删除客户: {}", customer.getCustomerCode());
        }
    }
}
