package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;

/**
 * 刷新令牌数据访问层
 * <p>继承 MyBatis-Plus BaseMapper，提供刷新令牌表的 CRUD 操作</p>
 */
@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {
}
