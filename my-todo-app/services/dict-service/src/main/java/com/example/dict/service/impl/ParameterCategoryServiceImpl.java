package com.example.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dict.entity.ParameterCategory;
import com.example.dict.mapper.ParameterCategoryMapper;
import com.example.dict.service.ParameterCategoryService;
import com.example.common.core.util.CodeGenerateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 参数分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParameterCategoryServiceImpl extends ServiceImpl<ParameterCategoryMapper, ParameterCategory> implements ParameterCategoryService {

    @Override
    @Transactional
    public ParameterCategory createCategory(ParameterCategory category) {
        // 自动生成分类编码（格式：PCAT-拼音首字母-时间戳）
        String generatedCode = CodeGenerateUtil.generate("PCAT",
                category.getCategoryName(),
                code -> getOne(new LambdaQueryWrapper<ParameterCategory>()
                        .eq(ParameterCategory::getTenantId, category.getTenantId())
                        .eq(ParameterCategory::getCategoryCode, code)
                        .eq(ParameterCategory::getDeleted, 0)) != null
        );
        category.setCategoryCode(generatedCode);
        save(category);
        return category;
    }

    @Override
    @Transactional
    public ParameterCategory updateCategory(ParameterCategory category) {
        updateById(category);
        return category;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        ParameterCategory category = getById(id);
        if (category != null) {
            category.setDeleted(1);
            updateById(category);
        }
    }

    @Override
    public Page<ParameterCategory> getCategoryPage(Long tenantId, int page, int size, String categoryName, String categoryType) {
        LambdaQueryWrapper<ParameterCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParameterCategory::getTenantId, tenantId)
               .eq(ParameterCategory::getDeleted, 0);
        if (categoryName != null && !categoryName.isEmpty()) {
            wrapper.like(ParameterCategory::getCategoryName, categoryName);
        }
        if (categoryType != null && !categoryType.isEmpty()) {
            wrapper.eq(ParameterCategory::getCategoryType, categoryType);
        }
        wrapper.orderByAsc(ParameterCategory::getSortOrder);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<ParameterCategory> getAllCategories(Long tenantId) {
        return list(
            new LambdaQueryWrapper<ParameterCategory>()
                .eq(ParameterCategory::getTenantId, tenantId)
                .eq(ParameterCategory::getDeleted, 0)
                .eq(ParameterCategory::getStatus, 1)
                .orderByAsc(ParameterCategory::getSortOrder)
        );
    }
}
