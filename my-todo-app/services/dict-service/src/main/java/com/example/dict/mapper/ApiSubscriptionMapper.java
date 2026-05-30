package com.example.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dict.entity.ApiSubscription;
import org.apache.ibatis.annotations.Mapper;

/**
 * API订阅Mapper
 */
@Mapper
public interface ApiSubscriptionMapper extends BaseMapper<ApiSubscription> {
}
