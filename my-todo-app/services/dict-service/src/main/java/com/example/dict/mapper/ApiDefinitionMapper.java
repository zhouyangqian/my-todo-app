package com.example.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dict.entity.ApiDefinition;
import org.apache.ibatis.annotations.Mapper;

/**
 * API定义Mapper
 */
@Mapper
public interface ApiDefinitionMapper extends BaseMapper<ApiDefinition> {
}
