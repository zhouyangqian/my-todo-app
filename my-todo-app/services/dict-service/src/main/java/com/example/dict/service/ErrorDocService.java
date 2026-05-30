package com.example.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dict.entity.ErrorCategory;
import com.example.dict.entity.ErrorSolution;

import java.util.List;

/**
 * 错误文档服务接口
 */
public interface ErrorDocService extends IService<ErrorCategory> {

    Page<ErrorCategory> getCategoryPage(int page, int size, String categoryName);

    List<ErrorCategory> listAllCategories();

    ErrorCategory createCategory(ErrorCategory category);

    ErrorCategory updateCategory(ErrorCategory category);

    void deleteCategory(Long id);

    Page<ErrorSolution> getSolutionPage(int page, int size, Long categoryId, String errorCode);

    ErrorSolution getSolution(Long id);

    ErrorSolution createSolution(ErrorSolution solution);

    ErrorSolution updateSolution(ErrorSolution solution);

    void deleteSolution(Long id);

    List<ErrorSolution> searchByErrorCode(String errorCode);
}
