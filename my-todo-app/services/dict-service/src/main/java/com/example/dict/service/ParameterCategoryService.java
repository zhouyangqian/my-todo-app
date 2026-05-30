package com.example.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dict.entity.ParameterCategory;

import java.util.List;

/**
 * 参数分类服务接口
 */
public interface ParameterCategoryService extends IService<ParameterCategory> {

    ParameterCategory createCategory(ParameterCategory category);

    ParameterCategory updateCategory(ParameterCategory category);

    void deleteCategory(Long id);

    Page<ParameterCategory> getCategoryPage(Long tenantId, int page, int size, String categoryName, String categoryType);

    List<ParameterCategory> getAllCategories(Long tenantId);
}
