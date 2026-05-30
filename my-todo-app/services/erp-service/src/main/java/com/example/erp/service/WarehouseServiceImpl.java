package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erp.entity.Warehouse;
import com.example.erp.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 仓库服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    @Override
    public Page<Warehouse> getWarehousePage(Long tenantId, int page, int size,
                                             String warehouseName, Integer status) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Warehouse::getTenantId, tenantId)
               .eq(Warehouse::getDeleted, 0);
        if (warehouseName != null && !warehouseName.isEmpty()) {
            wrapper.like(Warehouse::getWarehouseName, warehouseName);
        }
        if (status != null) {
            wrapper.eq(Warehouse::getStatus, status);
        }
        wrapper.orderByAsc(Warehouse::getWarehouseCode);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<Warehouse> getAllWarehouses(Long tenantId) {
        return list(
            new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getTenantId, tenantId)
                .eq(Warehouse::getDeleted, 0)
                .eq(Warehouse::getStatus, 1)
                .orderByAsc(Warehouse::getWarehouseCode)
        );
    }

    @Override
    public Warehouse getDefaultWarehouse(Long tenantId) {
        return getOne(
            new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getTenantId, tenantId)
                .eq(Warehouse::getIsDefault, 1)
                .eq(Warehouse::getStatus, 1)
                .eq(Warehouse::getDeleted, 0)
        );
    }

    @Override
    public Warehouse getByCode(String warehouseCode, Long tenantId) {
        return getOne(
            new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getWarehouseCode, warehouseCode)
                .eq(Warehouse::getTenantId, tenantId)
                .eq(Warehouse::getDeleted, 0)
        );
    }

    @Transactional
    @Override
    public Warehouse createWarehouse(Warehouse warehouse) {
        Warehouse existing = getByCode(warehouse.getWarehouseCode(), warehouse.getTenantId());
        if (existing != null) {
            throw new IllegalArgumentException("仓库编码已存在: " + warehouse.getWarehouseCode());
        }
        if (warehouse.getIsDefault() != null && warehouse.getIsDefault() == 1) {
            clearDefaultWarehouse(warehouse.getTenantId());
        }
        save(warehouse);
        log.info("创建仓库: {}", warehouse.getWarehouseCode());
        return warehouse;
    }

    @Transactional
    @Override
    public Warehouse updateWarehouse(Warehouse warehouse) {
        Warehouse existing = getOne(
            new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getWarehouseCode, warehouse.getWarehouseCode())
                .eq(Warehouse::getTenantId, warehouse.getTenantId())
                .ne(Warehouse::getId, warehouse.getId())
                .eq(Warehouse::getDeleted, 0)
        );
        if (existing != null) {
            throw new IllegalArgumentException("仓库编码已存在: " + warehouse.getWarehouseCode());
        }
        if (warehouse.getIsDefault() != null && warehouse.getIsDefault() == 1) {
            clearDefaultWarehouse(warehouse.getTenantId());
        }
        updateById(warehouse);
        log.info("更新仓库: {}", warehouse.getWarehouseCode());
        return warehouse;
    }

    @Transactional
    @Override
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = getById(id);
        if (warehouse != null) {
            warehouse.setDeleted(1);
            updateById(warehouse);
            log.info("删除仓库: {}", warehouse.getWarehouseCode());
        }
    }

    private void clearDefaultWarehouse(Long tenantId) {
        update(
            new LambdaUpdateWrapper<Warehouse>()
                .eq(Warehouse::getTenantId, tenantId)
                .eq(Warehouse::getIsDefault, 1)
                .set(Warehouse::getIsDefault, 0)
        );
    }
}
