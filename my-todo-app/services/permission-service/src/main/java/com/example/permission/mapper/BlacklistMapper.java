package com.example.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.permission.entity.Blacklist;
import org.apache.ibatis.annotations.Mapper;

/**
 * 黑名单数据访问层
 */
@Mapper
public interface BlacklistMapper extends BaseMapper<Blacklist> {
}
