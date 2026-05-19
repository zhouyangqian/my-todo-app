package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erp.entity.ProductCategory;
import com.example.erp.mapper.ProductCategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品分类服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCategoryService extends ServiceImpl<ProductCategoryMapper, ProductCategory> {

    /**
     * 分页查询商品分类
     */
    public Page<ProductCategory> getCategoryPage(Long tenantId, int page, int size,
                                                  String categoryName, Integer status) {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getTenantId, tenantId);
        if (categoryName != null && !categoryName.isEmpty()) {
            wrapper.like(ProductCategory::getCategoryName, categoryName);
        }
        if (status != null) {
            wrapper.eq(ProductCategory::getStatus, status);
        }
        wrapper.orderByAsc(ProductCategory::getSortOrder);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 获取所有启用的分类（树形结构用）
     */
    public List<ProductCategory> getAllCategories(Long tenantId) {
        return list(
            new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getTenantId, tenantId)
                .eq(ProductCategory::getStatus, 1)
                .eq(ProductCategory::getDeleted, 0)
                .orderByAsc(ProductCategory::getSortOrder)
        );
    }

    /**
     * 创建分类
     */
    @Transactional
    public ProductCategory createCategory(ProductCategory category) {
        // 校验编码唯一性
        ProductCategory existing = getOne(
            new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getCategoryCode, category.getCategoryCode())
                .eq(ProductCategory::getTenantId, category.getTenantId())
                .eq(ProductCategory::getDeleted, 0)
        );
        if (existing != null) {
            throw new IllegalArgumentException("分类编码已存在: " + category.getCategoryCode());
        }
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        save(category);
        log.info("创建商品分类: {}", category.getCategoryName());
        return category;
    }

    /**
     * 更新分类
     */
    @Transactional
    public ProductCategory updateCategory(ProductCategory category) {
        // 校验编码唯一性（排除自身）
        ProductCategory existing = getOne(
            new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getCategoryCode, category.getCategoryCode())
                .eq(ProductCategory::getTenantId, category.getTenantId())
                .ne(ProductCategory::getId, category.getId())
                .eq(ProductCategory::getDeleted, 0)
        );
        if (existing != null) {
            throw new IllegalArgumentException("分类编码已存在: " + category.getCategoryCode());
        }
        updateById(category);
        log.info("更新商品分类: {}", category.getCategoryName());
        return category;
    }

    /**
     * 删除分类（逻辑删除）
     */
    @Transactional
    public void deleteCategory(Long id) {
        ProductCategory category = getById(id);
        if (category != null) {
            // 检查是否有子分类
            long childCount = count(
                new LambdaQueryWrapper<ProductCategory>()
                    .eq(ProductCategory::getParentId, id)
                    .eq(ProductCategory::getDeleted, 0)
            );
            if (childCount > 0) {
                throw new IllegalArgumentException("该分类下有子分类，无法删除");
            }
            category.setDeleted(1);
            updateById(category);
            log.info("删除商品分类: {}", category.getCategoryName());
        }
    }
}
