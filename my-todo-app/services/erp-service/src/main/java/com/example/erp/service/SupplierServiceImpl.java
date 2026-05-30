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
 * 供应商服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {

    @Override
    public Page<Supplier> getSupplierPage(Long tenantId, int page, int size,
                                           String supplierName, Integer status) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Supplier::getTenantId, tenantId)
               .eq(Supplier::getDeleted, 0);
        if (supplierName != null && !supplierName.isEmpty()) {
            wrapper.like(Supplier::getSupplierName, supplierName);
        }
        if (status != null) {
            wrapper.eq(Supplier::getStatus, status);
        }
        wrapper.orderByDesc(Supplier::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Supplier getByCode(String supplierCode, Long tenantId) {
        return getOne(
            new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getSupplierCode, supplierCode)
                .eq(Supplier::getTenantId, tenantId)
                .eq(Supplier::getDeleted, 0)
        );
    }

    @Transactional
    @Override
    public Supplier createSupplier(Supplier supplier) {
        Supplier existing = getByCode(supplier.getSupplierCode(), supplier.getTenantId());
        if (existing != null) {
            throw new IllegalArgumentException("供应商编码已存在: " + supplier.getSupplierCode());
        }
        save(supplier);
        log.info("创建供应商: {}", supplier.getSupplierCode());
        return supplier;
    }

    @Transactional
    @Override
    public Supplier updateSupplier(Supplier supplier) {
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

    @Transactional
    @Override
    public void deleteSupplier(Long id) {
        Supplier supplier = getById(id);
        if (supplier != null) {
            supplier.setDeleted(1);
            updateById(supplier);
            log.info("删除供应商: {}", supplier.getSupplierCode());
        }
    }
}
