package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erp.entity.Product;
import com.example.erp.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品服务类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供商品相关的核心业务逻辑：
 * - 商品分页查询（支持按名称模糊搜索、按分类和状态过滤）
 * - 商品编码唯一性校验
 * - 商品的创建（初始库存为0）、更新、逻辑删除
 * - 根据商品编码精确查询
 * - 商品库存数量更新
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    /**
     * 分页查询商品列表
     * <p>
     * 根据租户ID查询该租户下的商品列表，支持按商品名称模糊搜索、
     * 按分类ID精确过滤、按状态过滤，结果按创建时间降序排列
     * </p>
     *
     * @param tenantId    租户ID
     * @param page        当前页码
     * @param size        每页条数
     * @param productName 商品名称（可选，模糊搜索）
     * @param categoryId  商品分类ID（可选，精确过滤）
     * @param status      状态（可选，0-停用，1-启用）
     * @return 商品分页数据
     */
    public Page<Product> getProductPage(Long tenantId, int page, int size,
                                         String productName, Long categoryId, Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        // 过滤条件：租户ID匹配 + 未删除
        wrapper.eq(Product::getTenantId, tenantId)
               .eq(Product::getDeleted, 0);
        // 可选条件：按商品名称模糊搜索
        if (productName != null && !productName.isEmpty()) {
            wrapper.like(Product::getProductName, productName);
        }
        // 可选条件：按分类ID精确过滤
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        // 可选条件：按状态过滤
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        // 按创建时间降序排列
        wrapper.orderByDesc(Product::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 根据商品编码和租户ID精确查询商品
     *
     * @param productCode 商品编码
     * @param tenantId    租户ID
     * @return 商品对象，未找到则返回null
     */
    public Product getByCode(String productCode, Long tenantId) {
        return getOne(
            new LambdaQueryWrapper<Product>()
                .eq(Product::getProductCode, productCode)
                .eq(Product::getTenantId, tenantId)
                .eq(Product::getDeleted, 0)
        );
    }

    /**
     * 创建商品
     * <p>
     * 创建前校验商品编码在同一租户下的唯一性。
     * 新创建的商品库存数量初始化为0。
     * </p>
     *
     * @param product 商品实体对象
     * @return 创建成功的商品对象
     * @throws IllegalArgumentException 商品编码已存在时抛出
     */
    @Transactional
    public Product createProduct(Product product) {
        // 校验商品编码在同一租户下是否已存在
        Product existing = getByCode(product.getProductCode(), product.getTenantId());
        if (existing != null) {
            throw new IllegalArgumentException("商品编码已存在: " + product.getProductCode());
        }
        // 新商品库存初始化为0
        product.setStockQuantity(java.math.BigDecimal.ZERO);
        save(product);
        log.info("创建商品: {}", product.getProductCode());
        return product;
    }

    /**
     * 更新商品信息
     * <p>
     * 更新前校验商品编码在同一租户下的唯一性（排除自身）
     * </p>
     *
     * @param product 商品实体对象（包含待更新字段和商品ID）
     * @return 更新后的商品对象
     * @throws IllegalArgumentException 商品编码已被其他商品使用时抛出
     */
    @Transactional
    public Product updateProduct(Product product) {
        // 校验商品编码在同一租户下是否已被其他商品使用（排除自身）
        Product existing = getOne(
            new LambdaQueryWrapper<Product>()
                .eq(Product::getProductCode, product.getProductCode())
                .eq(Product::getTenantId, product.getTenantId())
                .ne(Product::getId, product.getId())
                .eq(Product::getDeleted, 0)
        );
        if (existing != null) {
            throw new IllegalArgumentException("商品编码已存在: " + product.getProductCode());
        }
        updateById(product);
        log.info("更新商品: {}", product.getProductCode());
        return product;
    }

    /**
     * 删除商品（逻辑删除）
     * <p>
     * 将商品的 deleted 字段设置为1，不进行物理删除
     * </p>
     *
     * @param id 待删除的商品ID
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getById(id);
        if (product != null) {
            // 逻辑删除：设置deleted=1
            product.setDeleted(1);
            updateById(product);
            log.info("删除商品: {}", product.getProductCode());
        }
    }

    /**
     * 更新商品库存数量
     * <p>
     * 在当前库存数量的基础上增减指定数量（正数增加，负数减少）
     * </p>
     *
     * @param productId 商品ID
     * @param quantity  变动数量（正数增加，负数减少）
     */
    @Transactional
    public void updateStock(Long productId, java.math.BigDecimal quantity) {
        Product product = getById(productId);
        if (product != null) {
            // 在当前库存基础上累加变动数量
            product.setStockQuantity(product.getStockQuantity().add(quantity));
            updateById(product);
        }
    }
}
