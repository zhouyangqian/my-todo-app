package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erp.entity.Product;
import com.example.erp.mapper.ProductMapper;
import com.example.common.core.util.CodeGenerateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 商品服务实现类
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
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    /**
     * 分页查询商品列表
     */
    @Override
    public Page<Product> getProductPage(Long tenantId, int page, int size,
                                         String name, String sku, Long categoryId, Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getTenantId, tenantId)
               .eq(Product::getDeleted, 0);
        if (name != null && !name.isEmpty()) {
            wrapper.like(Product::getName, name);
        }
        if (sku != null && !sku.isEmpty()) {
            wrapper.eq(Product::getProductCode, sku);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getCreatedAt);
        Page<Product> result = page(new Page<>(page, size), wrapper);

        result.getRecords().forEach(this::copyDbFieldsToFrontendFields);

        return result;
    }

    /**
     * 根据商品编码和租户ID精确查询商品
     */
    @Override
    public Product getByCode(String productCode, Long tenantId) {
        Product product = getOne(
            new LambdaQueryWrapper<Product>()
                .eq(Product::getProductCode, productCode)
                .eq(Product::getTenantId, tenantId)
                .eq(Product::getDeleted, 0)
        );
        if (product != null) {
            copyDbFieldsToFrontendFields(product);
        }
        return product;
    }

    /**
     * 创建商品
     */
    @Transactional
    @Override
    public Product createProduct(Product product) {
        // 自动生成商品编码（格式：SKU-拼音首字母-时间戳）
        String generatedCode = CodeGenerateUtil.generate("SKU",
                product.getProductName(),
                code -> getByCode(code, product.getTenantId()) != null
        );
        product.setProductCode(generatedCode);
        product.setStockQuantity(BigDecimal.ZERO);
        save(product);
        log.info("创建商品: {}", product.getProductCode());
        copyDbFieldsToFrontendFields(product);
        return product;
    }

    /**
     * 更新商品信息
     */
    @Transactional
    @Override
    public Product updateProduct(Product product) {
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
        copyDbFieldsToFrontendFields(product);
        return product;
    }

    /**
     * 删除商品（逻辑删除）
     */
    @Transactional
    @Override
    public void deleteProduct(Long id) {
        Product product = getById(id);
        if (product != null) {
            product.setDeleted(1);
            updateById(product);
            log.info("删除商品: {}", product.getProductCode());
        }
    }

    /**
     * 更新商品库存数量
     */
    @Transactional
    @Override
    public void updateStock(Long productId, BigDecimal quantity) {
        Product product = getById(productId);
        if (product != null) {
            product.setStockQuantity(product.getStockQuantity().add(quantity));
            updateById(product);
        }
    }

    /**
     * 将数据库字段复制到前端字段
     */
    @Override
    public void copyDbFieldsToFrontendFields(Product product) {
        if (product.getProductCode() != null) {
            product.setSku(product.getProductCode());
        }
        if (product.getProductName() != null) {
            product.setName(product.getProductName());
        }
    }
}
