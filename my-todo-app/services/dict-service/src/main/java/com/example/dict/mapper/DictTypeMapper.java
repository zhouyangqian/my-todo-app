package com.example.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dict.entity.DictType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典类型Mapper
 */
@Mapper
public interface DictTypeMapper extends BaseMapper<DictType> {
}
