package com.example.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dict.entity.ErrorCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 错误分类Mapper
 */
@Mapper
public interface ErrorCategoryMapper extends BaseMapper<ErrorCategory> {
}
