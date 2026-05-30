package com.example.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dict.entity.ApiUsageRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * API使用记录Mapper
 */
@Mapper
public interface ApiUsageRecordMapper extends BaseMapper<ApiUsageRecord> {
}
