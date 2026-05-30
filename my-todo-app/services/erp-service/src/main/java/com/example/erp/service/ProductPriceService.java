package com.example.erp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erp.entity.ProductPrice;

import java.util.List;

/**
 * 商品价格服务接口
 */
public interface ProductPriceService extends IService<ProductPrice> {

    Page<ProductPrice> getPricePage(Long tenantId, int page, int size,
                                    Long productId, Integer priceType);

    List<ProductPrice> getPricesByProduct(Long productId, Long tenantId);

    ProductPrice createPrice(ProductPrice price);

    ProductPrice updatePrice(ProductPrice price);

    void deletePrice(Long id);
}
