package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.entity.TokenBlacklist;
import org.apache.ibatis.annotations.Mapper;

/**
 * Token黑名单数据访问层
 * <p>继承 MyBatis-Plus BaseMapper，提供Token黑名单表的 CRUD 操作</p>
 */
@Mapper
public interface TokenBlacklistMapper extends BaseMapper<TokenBlacklist> {
}
