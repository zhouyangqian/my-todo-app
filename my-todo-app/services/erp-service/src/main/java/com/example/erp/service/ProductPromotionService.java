package com.example.erp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erp.entity.ProductPromotion;

import java.util.List;

/**
 * 商品促销服务接口
 */
public interface ProductPromotionService extends IService<ProductPromotion> {

    Page<ProductPromotion> getPromotionPage(Long tenantId, int page, int size,
                                             String promotionName, Integer promotionType, Integer status);

    List<ProductPromotion> getActivePromotions(Long productId, Long tenantId);

    ProductPromotion createPromotion(ProductPromotion promotion);

    ProductPromotion updatePromotion(ProductPromotion promotion);

    void deletePromotion(Long id);

    void toggleStatus(Long id, Integer status);
}
