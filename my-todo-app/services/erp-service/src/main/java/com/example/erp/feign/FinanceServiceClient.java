package com.example.erp.feign;

import com.example.common.core.result.ApiResponse;
import com.example.erp.dto.AccountReceivableCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 财务服务 Feign 客户端
 * <p>
 * 用于 erp-service 调用 finance-service 的接口，
 * 主要用于在销售出库单审核后自动生成应收账款。
 * </p>
 * <p>
 * FeignConfig 会自动传递以下请求头：
 * <ul>
 *   <li>Authorization - JWT 认证令牌</li>
 *   <li>X-Tenant-Id - 租户ID</li>
 *   <li>X-Trace-Id - 追踪ID</li>
 * </ul>
 * </p>
 */
@FeignClient(name = "finance-service", path = "/api/finance")
public interface FinanceServiceClient {

    /**
     * 创建应收账款
     * <p>
     * 在销售出库单审核时调用，为出库单对应的客户创建应收账款记录。
     * </p>
     *
     * @param request 应收账款创建请求
     * @return 创建成功的应收账款
     */
    @PostMapping("/receivables")
    ApiResponse<Void> createReceivable(@RequestBody AccountReceivableCreateRequest request);
}
