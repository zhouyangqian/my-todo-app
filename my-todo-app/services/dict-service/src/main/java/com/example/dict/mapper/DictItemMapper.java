package com.example.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dict.entity.DictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典项Mapper
 */
@Mapper
public interface DictItemMapper extends BaseMapper<DictItem> {

    /**
     * 根据字典类型编码查询字典项
     */
    List<DictItem> selectByDictCode(@Param("dictCode") String dictCode, @Param("tenantId") Long tenantId);
}
