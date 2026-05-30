package com.example.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dict.entity.ThirdPartyCallLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 第三方API调用日志Mapper
 */
@Mapper
public interface ThirdPartyCallLogMapper extends BaseMapper<ThirdPartyCallLog> {
}
