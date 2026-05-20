package com.example.finance.api.feign;

import com.example.common.core.result.ApiResponse;
import com.example.finance.api.vo.CreateReceivableVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 财务服务 Feign 客户端
 * <p>
 * 供 erp-service、inventory-service 等服务调用 finance-service 创建应收/应付账款。
 * </p>
 */
@FeignClient(name = "finance-service", path = "/api/finance")
public interface FinanceFeignClient {

    @PostMapping("/receivables/create-receivable")
    ApiResponse<Void> createReceivable(@RequestBody CreateReceivableVO request);
}
