package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
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
    @GetMapping("/get-product-page")
    public ApiResponse<PageResult<Product>> getProductPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        Page<Product> result = productService.getProductPage(tenantId, page, size, name, sku, categoryId, status);
        PageResult<Product> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @Operation(summary = "获取商品详情")
    @GetMapping("/get-product/{id}")
    public ApiResponse<Product> getProduct(@PathVariable Long id) {
        Product product = productService.getById(id);
        // 将数据库字段复制到驼峰字段，方便前端使用
        if (product != null) {
            productService.copyDbFieldsToFrontendFields(product);
        }
        return ApiResponse.success(product);
    }

    @Operation(summary = "创建商品")
    @PostMapping("/create-product")
    public ApiResponse<Product> createProduct(
            @RequestBody Product product,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        // 字段映射：将前端传递的字段（sku, name）映射到数据库字段（productCode, productName）
        if (product.getSku() != null && product.getProductCode() == null) {
            product.setProductCode(product.getSku());
        }
        if (product.getName() != null && product.getProductName() == null) {
            product.setProductName(product.getName());
        }
        if (product.getBrand() != null) {
            // 如果有品牌字段，可以存储到 specification 或其他字段
            // 这里根据实际业务需求处理
        }
        if (product.getModel() != null) {
            // 如果有型号字段，可以存储到 specification 或其他字段
            // 这里根据实际业务需求处理
        }

        product.setTenantId(tenantId);
        product.setCreatedBy(userId);
        Product created = productService.createProduct(product);
        return ApiResponse.success(created);
    }

    @Operation(summary = "更新商品")
    @PutMapping("/update-product/{id}")
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
    @DeleteMapping("/delete-product/{id}")
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
