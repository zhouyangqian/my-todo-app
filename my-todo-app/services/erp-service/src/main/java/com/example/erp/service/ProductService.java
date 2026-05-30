package com.example.erp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erp.entity.Product;

import java.math.BigDecimal;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {

    Page<Product> getProductPage(Long tenantId, int page, int size,
                                 String name, String sku, Long categoryId, Integer status);

    Product getByCode(String productCode, Long tenantId);

    Product createProduct(Product product);

    Product updateProduct(Product product);

    void deleteProduct(Long id);

    void updateStock(Long productId, BigDecimal quantity);

    void copyDbFieldsToFrontendFields(Product product);
}
