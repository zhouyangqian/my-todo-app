package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erp.entity.Customer;
import com.example.erp.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 客户服务类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供客户相关的核心业务逻辑：
 * - 客户分页查询（支持按名称模糊搜索、按状态过滤）
 * - 客户编码唯一性校验
 * - 客户的创建、更新、逻辑删除
 * - 根据客户编码精确查询
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService extends ServiceImpl<CustomerMapper, Customer> {

    /**
     * 分页查询客户列表
     * <p>
     * 根据租户ID查询该租户下的客户列表，支持按客户名称模糊搜索和状态过滤，
     * 结果按创建时间降序排列（最新创建的排最前）
     * </p>
     *
     * @param tenantId     租户ID
     * @param page         当前页码
     * @param size         每页条数
     * @param customerName 客户名称（可选，模糊搜索）
     * @param status       状态（可选，0-停用，1-启用）
     * @return 客户分页数据
     */
    public Page<Customer> getCustomerPage(Long tenantId, int page, int size,
                                           String customerName, Integer status) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        // 过滤条件：租户ID匹配 + 未删除
        wrapper.eq(Customer::getTenantId, tenantId)
               .eq(Customer::getDeleted, 0);
        // 可选条件：按客户名称模糊搜索
        if (customerName != null && !customerName.isEmpty()) {
            wrapper.like(Customer::getCustomerName, customerName);
        }
        // 可选条件：按状态过滤
        if (status != null) {
            wrapper.eq(Customer::getStatus, status);
        }
        // 按创建时间降序排列
        wrapper.orderByDesc(Customer::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 根据客户编码和租户ID精确查询客户
     *
     * @param customerCode 客户编码
     * @param tenantId     租户ID
     * @return 客户对象，未找到则返回null
     */
    public Customer getByCode(String customerCode, Long tenantId) {
        return getOne(
            new LambdaQueryWrapper<Customer>()
                .eq(Customer::getCustomerCode, customerCode)
                .eq(Customer::getTenantId, tenantId)
                .eq(Customer::getDeleted, 0)
        );
    }

    /**
     * 创建客户
     * <p>
     * 创建前校验客户编码在同一租户下的唯一性，编码重复则抛出异常
     * </p>
     *
     * @param customer 客户实体对象
     * @return 创建成功的客户对象
     * @throws IllegalArgumentException 客户编码已存在时抛出
     */
    @Transactional
    public Customer createCustomer(Customer customer) {
        // 校验客户编码在同一租户下是否已存在
        Customer existing = getByCode(customer.getCustomerCode(), customer.getTenantId());
        if (existing != null) {
            throw new IllegalArgumentException("客户编码已存在: " + customer.getCustomerCode());
        }
        save(customer);
        log.info("创建客户: {}", customer.getCustomerCode());
        return customer;
    }

    /**
     * 更新客户信息
     * <p>
     * 更新前校验客户编码在同一租户下的唯一性（排除自身），编码重复则抛出异常
     * </p>
     *
     * @param customer 客户实体对象（包含待更新字段和客户ID）
     * @return 更新后的客户对象
     * @throws IllegalArgumentException 客户编码已被其他客户使用时抛出
     */
    @Transactional
    public Customer updateCustomer(Customer customer) {
        // 校验客户编码在同一租户下是否已被其他客户使用（排除自身）
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

    /**
     * 删除客户（逻辑删除）
     * <p>
     * 将客户的 deleted 字段设置为1，不进行物理删除
     * </p>
     *
     * @param id 待删除的客户ID
     */
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = getById(id);
        if (customer != null) {
            // 逻辑删除：设置deleted=1
            customer.setDeleted(1);
            updateById(customer);
            log.info("删除客户: {}", customer.getCustomerCode());
        }
    }
}
