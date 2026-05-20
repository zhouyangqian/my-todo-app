package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.erp.entity.ProductPrice;
import com.example.erp.service.ProductPriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品价格管理控制器
 */
@Tag(name = "商品价格管理", description = "商品价格增删改查API")
@RestController
@RequestMapping("/api/erp/product-prices")
@RequiredArgsConstructor
public class ProductPriceController {

    private final ProductPriceService productPriceService;

    @RequiresPermission(code = "erp:productPrice:list", name = "查询价格列表")
    @Operation(summary = "分页查询商品价格")
    @GetMapping("/get-price-page")
    public ApiResponse<PageResult<ProductPrice>> getPricePage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer priceType) {
        Page<ProductPrice> result = productPriceService.getPricePage(tenantId, page, size, productId, priceType);
        PageResult<ProductPrice> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "erp:productPrice:list", name = "查询价格列表")
    @Operation(summary = "获取商品的所有价格")
    @GetMapping("/get-prices-by-product/{productId}")
    public ApiResponse<List<ProductPrice>> getPricesByProduct(
            @PathVariable Long productId,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        List<ProductPrice> prices = productPriceService.getPricesByProduct(productId, tenantId);
        return ApiResponse.success(prices);
    }

    @RequiresPermission(code = "erp:productPrice:create", name = "新增价格")
    @Operation(summary = "创建价格")
    @PostMapping("/create-price")
    public ApiResponse<ProductPrice> createPrice(
            @RequestBody ProductPrice price,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        price.setTenantId(tenantId);
        price.setCreatedBy(userId);
        ProductPrice created = productPriceService.createPrice(price);
        return ApiResponse.success(created);
    }

    @RequiresPermission(code = "erp:productPrice:update", name = "更新价格")
    @Operation(summary = "更新价格")
    @PutMapping("/update-price/{id}")
    public ApiResponse<ProductPrice> updatePrice(
            @PathVariable Long id,
            @RequestBody ProductPrice price,
            @RequestHeader("X-User-Id") Long userId) {
        price.setId(id);
        price.setUpdatedBy(userId);
        ProductPrice updated = productPriceService.updatePrice(price);
        return ApiResponse.success(updated);
    }

    @RequiresPermission(code = "erp:productPrice:delete", name = "删除价格")
    @Operation(summary = "删除价格")
    @DeleteMapping("/delete-price/{id}")
    public ApiResponse<Void> deletePrice(@PathVariable Long id) {
        productPriceService.deletePrice(id);
        return ApiResponse.success();
    }
}
