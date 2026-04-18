package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erp.entity.Supplier;
import com.example.erp.mapper.SupplierMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 供应商服务类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供供应商相关的核心业务逻辑：
 * - 供应商分页查询（支持按名称模糊搜索、按状态过滤）
 * - 供应商编码唯一性校验
 * - 供应商的创建、更新、逻辑删除
 * - 根据供应商编码精确查询
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService extends ServiceImpl<SupplierMapper, Supplier> {

    /**
     * 分页查询供应商列表
     * <p>
     * 根据租户ID查询该租户下的供应商列表，支持按供应商名称模糊搜索和状态过滤，
     * 结果按创建时间降序排列（最新创建的排最前）
     * </p>
     *
     * @param tenantId     租户ID
     * @param page         当前页码
     * @param size         每页条数
     * @param supplierName 供应商名称（可选，模糊搜索）
     * @param status       状态（可选，0-停用，1-启用）
     * @return 供应商分页数据
     */
    public Page<Supplier> getSupplierPage(Long tenantId, int page, int size,
                                           String supplierName, Integer status) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        // 过滤条件：租户ID匹配 + 未删除
        wrapper.eq(Supplier::getTenantId, tenantId)
               .eq(Supplier::getDeleted, 0);
        // 可选条件：按供应商名称模糊搜索
        if (supplierName != null && !supplierName.isEmpty()) {
            wrapper.like(Supplier::getSupplierName, supplierName);
        }
        // 可选条件：按状态过滤
        if (status != null) {
            wrapper.eq(Supplier::getStatus, status);
        }
        // 按创建时间降序排列
        wrapper.orderByDesc(Supplier::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 根据供应商编码和租户ID精确查询供应商
     *
     * @param supplierCode 供应商编码
     * @param tenantId     租户ID
     * @return 供应商对象，未找到则返回null
     */
    public Supplier getByCode(String supplierCode, Long tenantId) {
        return getOne(
            new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getSupplierCode, supplierCode)
                .eq(Supplier::getTenantId, tenantId)
                .eq(Supplier::getDeleted, 0)
        );
    }

    /**
     * 创建供应商
     * <p>
     * 创建前校验供应商编码在同一租户下的唯一性，编码重复则抛出异常
     * </p>
     *
     * @param supplier 供应商实体对象
     * @return 创建成功的供应商对象
     * @throws IllegalArgumentException 供应商编码已存在时抛出
     */
    @Transactional
    public Supplier createSupplier(Supplier supplier) {
        // 校验供应商编码在同一租户下是否已存在
        Supplier existing = getByCode(supplier.getSupplierCode(), supplier.getTenantId());
        if (existing != null) {
            throw new IllegalArgumentException("供应商编码已存在: " + supplier.getSupplierCode());
        }
        save(supplier);
        log.info("创建供应商: {}", supplier.getSupplierCode());
        return supplier;
    }

    /**
     * 更新供应商信息
     * <p>
     * 更新前校验供应商编码在同一租户下的唯一性（排除自身），编码重复则抛出异常
     * </p>
     *
     * @param supplier 供应商实体对象（包含待更新字段和供应商ID）
     * @return 更新后的供应商对象
     * @throws IllegalArgumentException 供应商编码已被其他供应商使用时抛出
     */
    @Transactional
    public Supplier updateSupplier(Supplier supplier) {
        // 校验供应商编码在同一租户下是否已被其他供应商使用（排除自身）
        Supplier existing = getOne(
            new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getSupplierCode, supplier.getSupplierCode())
                .eq(Supplier::getTenantId, supplier.getTenantId())
                .ne(Supplier::getId, supplier.getId())
                .eq(Supplier::getDeleted, 0)
        );
        if (existing != null) {
            throw new IllegalArgumentException("供应商编码已存在: " + supplier.getSupplierCode());
        }
        updateById(supplier);
        log.info("更新供应商: {}", supplier.getSupplierCode());
        return supplier;
    }

    /**
     * 删除供应商（逻辑删除）
     * <p>
     * 将供应商的 deleted 字段设置为1，不进行物理删除
     * </p>
     *
     * @param id 待删除的供应商ID
     */
    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = getById(id);
        if (supplier != null) {
            // 逻辑删除：设置deleted=1
            supplier.setDeleted(1);
            updateById(supplier);
            log.info("删除供应商: {}", supplier.getSupplierCode());
        }
    }
}
