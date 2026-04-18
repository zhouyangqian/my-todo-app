package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
 * 仓库服务类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供仓库相关的核心业务逻辑：
 * - 仓库分页查询（支持按名称模糊搜索、按状态过滤）
 * - 查询租户所有启用的仓库（下拉选择用）
 * - 获取默认仓库
 * - 仓库编码唯一性校验
 * - 仓库的创建、更新、逻辑删除
 * - 默认仓库管理（同一租户只能有一个默认仓库）
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService extends ServiceImpl<WarehouseMapper, Warehouse> {

    /**
     * 分页查询仓库列表
     * <p>
     * 根据租户ID查询该租户下的仓库列表，支持按仓库名称模糊搜索和状态过滤，
     * 结果按排序字段升序排列
     * </p>
     *
     * @param tenantId      租户ID
     * @param page          当前页码
     * @param size          每页条数
     * @param warehouseName 仓库名称（可选，模糊搜索）
     * @param status        状态（可选，0-停用，1-启用）
     * @return 仓库分页数据
     */
    public Page<Warehouse> getWarehousePage(Long tenantId, int page, int size,
                                             String warehouseName, Integer status) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        // 过滤条件：租户ID匹配 + 未删除
        wrapper.eq(Warehouse::getTenantId, tenantId)
               .eq(Warehouse::getDeleted, 0);
        // 可选条件：按仓库名称模糊搜索
        if (warehouseName != null && !warehouseName.isEmpty()) {
            wrapper.like(Warehouse::getWarehouseName, warehouseName);
        }
        // 可选条件：按状态过滤
        if (status != null) {
            wrapper.eq(Warehouse::getStatus, status);
        }
        // 按排序字段升序排列
        wrapper.orderByAsc(Warehouse::getSort);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 获取租户所有启用的仓库列表
     * <p>
     * 用于下拉选择框等场景，只返回状态为启用的仓库，按排序字段升序排列
     * </p>
     *
     * @param tenantId 租户ID
     * @return 该租户下所有启用的仓库列表
     */
    public List<Warehouse> getAllWarehouses(Long tenantId) {
        return list(
            new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getTenantId, tenantId)
                .eq(Warehouse::getDeleted, 0)
                .eq(Warehouse::getStatus, 1)
                .orderByAsc(Warehouse::getSort)
        );
    }

    /**
     * 获取租户的默认仓库
     * <p>
     * 查询标记为默认且状态为启用的仓库，每个租户最多有一个默认仓库
     * </p>
     *
     * @param tenantId 租户ID
     * @return 默认仓库对象，未设置则返回null
     */
    public Warehouse getDefaultWarehouse(Long tenantId) {
        return getOne(
            new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getTenantId, tenantId)
                .eq(Warehouse::getIsDefault, 1)
                .eq(Warehouse::getStatus, 1)
                .eq(Warehouse::getDeleted, 0)
        );
    }

    /**
     * 根据仓库编码和租户ID精确查询仓库
     *
     * @param warehouseCode 仓库编码
     * @param tenantId      租户ID
     * @return 仓库对象，未找到则返回null
     */
    public Warehouse getByCode(String warehouseCode, Long tenantId) {
        return getOne(
            new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getWarehouseCode, warehouseCode)
                .eq(Warehouse::getTenantId, tenantId)
                .eq(Warehouse::getDeleted, 0)
        );
    }

    /**
     * 创建仓库
     * <p>
     * 创建前校验仓库编码在同一租户下的唯一性。
     * 如果新仓库被设置为默认仓库，会自动清除该租户下其他仓库的默认标记。
     * </p>
     *
     * @param warehouse 仓库实体对象
     * @return 创建成功的仓库对象
     * @throws IllegalArgumentException 仓库编码已存在时抛出
     */
    @Transactional
    public Warehouse createWarehouse(Warehouse warehouse) {
        // 校验仓库编码在同一租户下是否已存在
        Warehouse existing = getByCode(warehouse.getWarehouseCode(), warehouse.getTenantId());
        if (existing != null) {
            throw new IllegalArgumentException("仓库编码已存在: " + warehouse.getWarehouseCode());
        }
        // 如果设置为默认仓库，先清除该租户下其他仓库的默认标记
        if (warehouse.getIsDefault() != null && warehouse.getIsDefault() == 1) {
            clearDefaultWarehouse(warehouse.getTenantId());
        }
        save(warehouse);
        log.info("创建仓库: {}", warehouse.getWarehouseCode());
        return warehouse;
    }

    /**
     * 更新仓库信息
     * <p>
     * 更新前校验仓库编码在同一租户下的唯一性（排除自身）。
     * 如果更新后设为默认仓库，会自动清除该租户下其他仓库的默认标记。
     * </p>
     *
     * @param warehouse 仓库实体对象（包含待更新字段和仓库ID）
     * @return 更新后的仓库对象
     * @throws IllegalArgumentException 仓库编码已被其他仓库使用时抛出
     */
    @Transactional
    public Warehouse updateWarehouse(Warehouse warehouse) {
        // 校验仓库编码在同一租户下是否已被其他仓库使用（排除自身）
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
        // 如果设置为默认仓库，先清除该租户下其他仓库的默认标记
        if (warehouse.getIsDefault() != null && warehouse.getIsDefault() == 1) {
            clearDefaultWarehouse(warehouse.getTenantId());
        }
        updateById(warehouse);
        log.info("更新仓库: {}", warehouse.getWarehouseCode());
        return warehouse;
    }

    /**
     * 删除仓库（逻辑删除）
     * <p>
     * 将仓库的 deleted 字段设置为1，不进行物理删除
     * </p>
     *
     * @param id 待删除的仓库ID
     */
    @Transactional
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = getById(id);
        if (warehouse != null) {
            // 逻辑删除：设置deleted=1
            warehouse.setDeleted(1);
            updateById(warehouse);
            log.info("删除仓库: {}", warehouse.getWarehouseCode());
        }
    }

    /**
     * 清除指定租户下所有仓库的默认标记
     * <p>
     * 将该租户下所有标记为默认的仓库的 isDefault 字段设置为0，
     * 确保同一租户下只有一个默认仓库
     * </p>
     *
     * @param tenantId 租户ID
     */
    private void clearDefaultWarehouse(Long tenantId) {
        update(
            new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getTenantId, tenantId)
                .eq(Warehouse::getIsDefault, 1),
            wrapper -> wrapper.set(Warehouse::getIsDefault, 0)
        );
    }
}
