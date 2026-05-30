package com.example.erp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erp.entity.ProductCategory;

import java.util.List;

/**
 * 商品分类服务接口
 */
public interface ProductCategoryService extends IService<ProductCategory> {

    Page<ProductCategory> getCategoryPage(Long tenantId, int page, int size,
                                           String categoryName, Integer status);

    List<ProductCategory> getAllCategories(Long tenantId);

    ProductCategory createCategory(ProductCategory category);

    ProductCategory updateCategory(ProductCategory category);

    void deleteCategory(Long id);
}
