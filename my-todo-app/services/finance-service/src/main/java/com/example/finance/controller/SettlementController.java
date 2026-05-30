package com.example.finance.controller;

import com.example.common.core.result.ApiResponse;
import com.example.finance.service.SettlementService;
import com.example.finance.vo.SettlementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ERP结算控制器
 * <p>
 * 提供采购/销售结算集成接口，供 ERP 服务调用以触发财务结算流程。
 * 租户ID和操作人ID通过请求头传递。
 * </p>
 */
@Tag(name = "ERP结算", description = "采购/销售结算集成")
@RestController
@RequestMapping("/api/finance/settlement")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    /**
     * 采购完成结算
     *
     * @param vo      结算请求数据
     * @param request HTTP请求（获取租户ID和操作人ID）
     * @return 操作结果
     */
    @Operation(summary = "采购完成结算")
    @PostMapping("/purchase")
    public ApiResponse<Void> settlePurchase(@Valid @RequestBody SettlementVO vo, HttpServletRequest request) {
        Long operatorId = parseLongHeader(request, "X-User-Id");
        Long tenantId = parseLongHeader(request, "X-Tenant-Id");
        settlementService.settlePurchase(tenantId, vo.getOrderId(), vo.getOrderNo(),
                vo.getCounterpartyId(), vo.getAmount(), operatorId);
        return ApiResponse.success();
    }

    /**
     * 销售完成结算
     *
     * @param vo      结算请求数据
     * @param request HTTP请求（获取租户ID和操作人ID）
     * @return 操作结果
     */
    @Operation(summary = "销售完成结算")
    @PostMapping("/sales")
    public ApiResponse<Void> settleSales(@Valid @RequestBody SettlementVO vo, HttpServletRequest request) {
        Long operatorId = parseLongHeader(request, "X-User-Id");
        Long tenantId = parseLongHeader(request, "X-Tenant-Id");
        settlementService.settleSales(tenantId, vo.getOrderId(), vo.getOrderNo(),
                vo.getCounterpartyId(), vo.getAmount(), operatorId);
        return ApiResponse.success();
    }

    /**
     * 从请求头解析 Long 类型值
     */
    private Long parseLongHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
