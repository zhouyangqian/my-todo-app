package com.example.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dict.entity.ErrorCategory;
import com.example.dict.entity.ErrorSolution;
import com.example.dict.mapper.ErrorCategoryMapper;
import com.example.dict.mapper.ErrorSolutionMapper;
import com.example.dict.service.ErrorDocService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 错误文档服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorDocServiceImpl extends ServiceImpl<ErrorCategoryMapper, ErrorCategory> implements ErrorDocService {

    private final ErrorSolutionMapper errorSolutionMapper;

    // ==================== 错误分类 ====================

    @Override
    public Page<ErrorCategory> getCategoryPage(int page, int size, String categoryName) {
        LambdaQueryWrapper<ErrorCategory> wrapper = new LambdaQueryWrapper<>();
        if (categoryName != null && !categoryName.isEmpty()) {
            wrapper.like(ErrorCategory::getCategoryName, categoryName);
        }
        wrapper.orderByAsc(ErrorCategory::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<ErrorCategory> listAllCategories() {
        return list(new LambdaQueryWrapper<ErrorCategory>()
                .orderByAsc(ErrorCategory::getCreatedAt));
    }

    @Override
    @Transactional
    public ErrorCategory createCategory(ErrorCategory category) {
        // 检查编码唯一性
        ErrorCategory existing = getOne(new LambdaQueryWrapper<ErrorCategory>()
                .eq(ErrorCategory::getCategoryCode, category.getCategoryCode()));
        if (existing != null) {
            throw new IllegalArgumentException("分类编码已存在: " + category.getCategoryCode());
        }
        save(category);
        return category;
    }

    @Override
    @Transactional
    public ErrorCategory updateCategory(ErrorCategory category) {
        updateById(category);
        return category;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        // 删除分类下的所有解决方案
        errorSolutionMapper.delete(
                new LambdaQueryWrapper<ErrorSolution>()
                        .eq(ErrorSolution::getCategoryId, id));
        removeById(id);
    }

    // ==================== 错误解决方案 ====================

    @Override
    public Page<ErrorSolution> getSolutionPage(int page, int size, Long categoryId, String errorCode) {
        LambdaQueryWrapper<ErrorSolution> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(ErrorSolution::getCategoryId, categoryId);
        }
        if (errorCode != null && !errorCode.isEmpty()) {
            wrapper.like(ErrorSolution::getErrorCode, errorCode);
        }
        wrapper.orderByAsc(ErrorSolution::getErrorCode);
        return errorSolutionMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public ErrorSolution getSolution(Long id) {
        return errorSolutionMapper.selectById(id);
    }

    @Override
    @Transactional
    public ErrorSolution createSolution(ErrorSolution solution) {
        errorSolutionMapper.insert(solution);
        return solution;
    }

    @Override
    @Transactional
    public ErrorSolution updateSolution(ErrorSolution solution) {
        errorSolutionMapper.updateById(solution);
        return solution;
    }

    @Override
    @Transactional
    public void deleteSolution(Long id) {
        errorSolutionMapper.deleteById(id);
    }

    @Override
    public List<ErrorSolution> searchByErrorCode(String errorCode) {
        return errorSolutionMapper.selectList(
                new LambdaQueryWrapper<ErrorSolution>()
                        .eq(ErrorSolution::getErrorCode, errorCode));
    }
}
