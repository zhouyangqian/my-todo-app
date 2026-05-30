package com.example.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.entity.Tenant;
import com.example.auth.entity.TenantQuota;
import com.example.auth.entity.TenantResourceUsage;

/**
 * 租户管理服务接口
 * <p>
 * 处理租户配额管理、资源使用量统计等业务逻辑。
 * </p>
 */
public interface TenantManagementService {

    /**
     * 初始化租户配额
     *
     * @param tenantId 租户ID
     */
    void initTenantQuota(Long tenantId);

    /**
     * 查询租户配额
     *
     * @param tenantId 租户ID
     * @return 租户配额
     */
    TenantQuota getTenantQuota(Long tenantId);

    /**
     * 更新租户配额
     *
     * @param tenantId 租户ID
     * @param quota    配额信息
     * @return 更新后的配额
     */
    TenantQuota updateTenantQuota(Long tenantId, TenantQuota quota);

    /**
     * 检查用户数量限制并递增用户计数
     *
     * @param tenantId 租户ID
     */
    void checkAndIncrementUserCount(Long tenantId);

    /**
     * 减少用户计数
     *
     * @param tenantId 租户ID
     */
    void decrementUserCount(Long tenantId);

    /**
     * 查询租户资源使用量
     *
     * @param tenantId 租户ID
     * @return 资源使用量
     */
    TenantResourceUsage getTenantResourceUsage(Long tenantId);

    /**
     * 记录每日API使用量
     *
     * @param tenantId 租户ID
     * @param apiCalls API调用次数
     */
    void recordDailyUsage(Long tenantId, int apiCalls);

    /**
     * 租户列表（管理员）
     *
     * @param page       当前页码
     * @param size       每页条数
     * @param tenantName 租户名称（可选，模糊搜索）
     * @param status     状态（可选）
     * @return 租户分页数据
     */
    Page<Tenant> getTenantList(int page, int size, String tenantName, Integer status);
}
