package com.example.erp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erp.entity.Warehouse;

import java.util.List;

/**
 * 仓库服务接口
 */
public interface WarehouseService extends IService<Warehouse> {

    Page<Warehouse> getWarehousePage(Long tenantId, int page, int size,
                                      String warehouseName, Integer status);

    List<Warehouse> getAllWarehouses(Long tenantId);

    Warehouse getDefaultWarehouse(Long tenantId);

    Warehouse getByCode(String warehouseCode, Long tenantId);

    Warehouse createWarehouse(Warehouse warehouse);

    Warehouse updateWarehouse(Warehouse warehouse);

    void deleteWarehouse(Long id);
}
