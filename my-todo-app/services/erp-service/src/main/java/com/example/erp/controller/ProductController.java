package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.erp.entity.Product;
import com.example.erp.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品管理控制器
 */
@Tag(name = "商品管理", description = "商品增删改查API")
@RestController
@RequestMapping("/api/erp/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "分页查询商品")
    @GetMapping
    public ApiResponse<Page<Product>> getProductPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        Page<Product> result = productService.getProductPage(tenantId, page, size, productName, categoryId, status);
        return ApiResponse.success(result);
    }

    @Operation(summary = "获取商品详情")
    @GetMapping("/{id}")
    public ApiResponse<Product> getProduct(@PathVariable Long id) {
        Product product = productService.getById(id);
        return ApiResponse.success(product);
    }

    @Operation(summary = "创建商品")
    @PostMapping
    public ApiResponse<Product> createProduct(
            @RequestBody Product product,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        product.setTenantId(tenantId);
        product.setCreatedBy(userId);
        Product created = productService.createProduct(product);
        return ApiResponse.success(created);
    }

    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    public ApiResponse<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product,
            @RequestHeader("X-User-Id") Long userId) {
        product.setId(id);
        product.setUpdatedBy(userId);
        Product updated = productService.updateProduct(product);
        return ApiResponse.success(updated);
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success();
    }

    @Operation(summary = "根据编码查询商品")
    @GetMapping("/code/{productCode}")
    public ApiResponse<Product> getProductByCode(
            @PathVariable String productCode,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        Product product = productService.getByCode(productCode, tenantId);
        return ApiResponse.success(product);
    }
}
