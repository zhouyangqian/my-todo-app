package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductPrice;
import com.example.erp.mapper.ProductPriceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品价格服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPriceServiceImpl extends ServiceImpl<ProductPriceMapper, ProductPrice> implements ProductPriceService {

    private final ProductService productService;

    @Override
    public Page<ProductPrice> getPricePage(Long tenantId, int page, int size,
                                            Long productId, Integer priceType) {
        LambdaQueryWrapper<ProductPrice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductPrice::getTenantId, tenantId);
        if (productId != null) {
            wrapper.eq(ProductPrice::getProductId, productId);
        }
        if (priceType != null) {
            wrapper.eq(ProductPrice::getPriceType, priceType);
        }
        wrapper.orderByDesc(ProductPrice::getCreatedAt);

        Page<ProductPrice> result = page(new Page<>(page, size), wrapper);
        Set<Long> productIds = result.getRecords().stream()
            .map(ProductPrice::getProductId).collect(Collectors.toSet());
        if (!productIds.isEmpty()) {
            List<Product> products = productService.listByIds(productIds);
            java.util.Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
            result.getRecords().forEach(price -> {
                Product p = productMap.get(price.getProductId());
                if (p != null) {
                    price.setProductName(p.getProductName());
                    price.setProductCode(p.getProductCode());
                }
            });
        }
        return result;
    }

    @Override
    public List<ProductPrice> getPricesByProduct(Long productId, Long tenantId) {
        return list(
            new LambdaQueryWrapper<ProductPrice>()
                .eq(ProductPrice::getProductId, productId)
                .eq(ProductPrice::getTenantId, tenantId)
                .eq(ProductPrice::getStatus, 1)
                .orderByAsc(ProductPrice::getPriceType)
        );
    }

    @Transactional
    @Override
    public ProductPrice createPrice(ProductPrice price) {
        save(price);
        log.info("创建商品价格: productId={}, priceType={}, price={}",
            price.getProductId(), price.getPriceType(), price.getPrice());
        return price;
    }

    @Transactional
    @Override
    public ProductPrice updatePrice(ProductPrice price) {
        updateById(price);
        return price;
    }

    @Transactional
    @Override
    public void deletePrice(Long id) {
        removeById(id);
    }
}
