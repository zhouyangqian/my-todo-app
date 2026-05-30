package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductPromotion;
import com.example.erp.mapper.ProductPromotionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品促销服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPromotionServiceImpl extends ServiceImpl<ProductPromotionMapper, ProductPromotion> implements ProductPromotionService {

    private final ProductService productService;

    @Override
    public Page<ProductPromotion> getPromotionPage(Long tenantId, int page, int size,
                                                    String promotionName, Integer promotionType, Integer status) {
        LambdaQueryWrapper<ProductPromotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductPromotion::getTenantId, tenantId);
        if (promotionName != null && !promotionName.isEmpty()) {
            wrapper.like(ProductPromotion::getPromotionName, promotionName);
        }
        if (promotionType != null) {
            wrapper.eq(ProductPromotion::getPromotionType, promotionType);
        }
        if (status != null) {
            wrapper.eq(ProductPromotion::getStatus, status);
        }
        wrapper.orderByDesc(ProductPromotion::getCreatedAt);

        Page<ProductPromotion> result = page(new Page<>(page, size), wrapper);
        fillProductInfo(result.getRecords());
        return result;
    }

    @Override
    public List<ProductPromotion> getActivePromotions(Long productId, Long tenantId) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<ProductPromotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductPromotion::getTenantId, tenantId)
               .eq(ProductPromotion::getStatus, 1)
               .le(ProductPromotion::getStartDate, now)
               .ge(ProductPromotion::getEndDate, now)
               .and(w -> w.isNull(ProductPromotion::getProductId)
                         .or().eq(ProductPromotion::getProductId, productId));
        return list(wrapper);
    }

    @Transactional
    @Override
    public ProductPromotion createPromotion(ProductPromotion promotion) {
        autoSetStatus(promotion);
        save(promotion);
        log.info("创建促销活动: name={}, type={}", promotion.getPromotionName(), promotion.getPromotionType());
        return promotion;
    }

    @Transactional
    @Override
    public ProductPromotion updatePromotion(ProductPromotion promotion) {
        autoSetStatus(promotion);
        updateById(promotion);
        return promotion;
    }

    @Transactional
    @Override
    public void deletePromotion(Long id) {
        removeById(id);
    }

    @Transactional
    @Override
    public void toggleStatus(Long id, Integer status) {
        ProductPromotion promotion = getById(id);
        if (promotion != null) {
            promotion.setStatus(status);
            promotion.setUpdatedBy(null);
            updateById(promotion);
            log.info("更新促销状态: id={}, status={}", id, status);
        }
    }

    private void autoSetStatus(ProductPromotion promotion) {
        if (promotion.getStatus() != null && promotion.getStatus() == 3) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (promotion.getStartDate() != null && promotion.getEndDate() != null) {
            if (now.isBefore(promotion.getStartDate())) {
                promotion.setStatus(0);
            } else if (now.isAfter(promotion.getEndDate())) {
                promotion.setStatus(2);
            } else {
                promotion.setStatus(1);
            }
        }
    }

    private void fillProductInfo(List<ProductPromotion> records) {
        Set<Long> productIds = new HashSet<>();
        records.forEach(p -> {
            if (p.getProductId() != null) productIds.add(p.getProductId());
            if (p.getGiftProductId() != null) productIds.add(p.getGiftProductId());
        });
        if (productIds.isEmpty()) return;

        List<Product> products = productService.listByIds(productIds);
        Map<Long, Product> productMap = products.stream()
            .collect(Collectors.toMap(Product::getId, p -> p));

        records.forEach(p -> {
            if (p.getProductId() != null) {
                Product prod = productMap.get(p.getProductId());
                if (prod != null) {
                    p.setProductName(prod.getProductName());
                    p.setProductCode(prod.getProductCode());
                }
            }
            if (p.getGiftProductId() != null) {
                Product gift = productMap.get(p.getGiftProductId());
                if (gift != null) {
                    p.setGiftProductName(gift.getProductName());
                }
            }
        });
    }
}
