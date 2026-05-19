package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.erp.entity.ProductPromotion;
import com.example.erp.service.ProductPromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品促销管理控制器
 */
@Tag(name = "商品促销管理", description = "商品促销活动增删改查API")
@RestController
@RequestMapping("/api/erp/product-promotions")
@RequiredArgsConstructor
public class ProductPromotionController {

    private final ProductPromotionService productPromotionService;

    @Operation(summary = "分页查询促销活动")
    @GetMapping("/get-promotion-page")
    public ApiResponse<PageResult<ProductPromotion>> getPromotionPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String promotionName,
            @RequestParam(required = false) Integer promotionType,
            @RequestParam(required = false) Integer status) {
        Page<ProductPromotion> result = productPromotionService.getPromotionPage(
                tenantId, page, size, promotionName, promotionType, status);
        PageResult<ProductPromotion> pageResult = PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @Operation(summary = "获取商品的有效促销活动")
    @GetMapping("/get-active-promotions/{productId}")
    public ApiResponse<List<ProductPromotion>> getActivePromotions(
            @PathVariable Long productId,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        List<ProductPromotion> promotions = productPromotionService.getActivePromotions(productId, tenantId);
        return ApiResponse.success(promotions);
    }

    @Operation(summary = "创建促销活动")
    @PostMapping("/create-promotion")
    public ApiResponse<ProductPromotion> createPromotion(
            @RequestBody ProductPromotion promotion,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        promotion.setTenantId(tenantId);
        promotion.setCreatedBy(userId);
        ProductPromotion created = productPromotionService.createPromotion(promotion);
        return ApiResponse.success(created);
    }

    @Operation(summary = "更新促销活动")
    @PutMapping("/update-promotion/{id}")
    public ApiResponse<ProductPromotion> updatePromotion(
            @PathVariable Long id,
            @RequestBody ProductPromotion promotion,
            @RequestHeader("X-User-Id") Long userId) {
        promotion.setId(id);
        promotion.setUpdatedBy(userId);
        ProductPromotion updated = productPromotionService.updatePromotion(promotion);
        return ApiResponse.success(updated);
    }

    @Operation(summary = "删除促销活动")
    @DeleteMapping("/delete-promotion/{id}")
    public ApiResponse<Void> deletePromotion(@PathVariable Long id) {
        productPromotionService.deletePromotion(id);
        return ApiResponse.success();
    }

    @Operation(summary = "启用促销活动")
    @PostMapping("/enable-promotion/{id}")
    public ApiResponse<Void> enablePromotion(@PathVariable Long id) {
        productPromotionService.toggleStatus(id, 1);
        return ApiResponse.success();
    }

    @Operation(summary = "停用促销活动")
    @PostMapping("/disable-promotion/{id}")
    public ApiResponse<Void> disablePromotion(@PathVariable Long id) {
        productPromotionService.toggleStatus(id, 3);
        return ApiResponse.success();
    }
}
