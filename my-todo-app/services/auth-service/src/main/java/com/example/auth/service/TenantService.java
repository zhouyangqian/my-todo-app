package com.example.auth.service;

import com.example.auth.api.dto.TenantDTO;
import com.example.auth.api.dto.TenantRegisterResultDTO;
import com.example.auth.api.vo.TenantRegisterVO;

/**
 * 租户服务接口
 * <p>
 * 处理租户注册、查询等业务逻辑。
 * </p>
 */
public interface TenantService {

    /**
     * 租户注册
     *
     * @param request 租户注册请求
     * @return 注册结果（含租户ID和管理员账户信息）
     */
    TenantRegisterResultDTO registerTenant(TenantRegisterVO request);

    /**
     * 查询租户信息
     *
     * @param tenantId 租户ID
     * @return 租户DTO
     */
    TenantDTO getTenantInfo(Long tenantId);
}
