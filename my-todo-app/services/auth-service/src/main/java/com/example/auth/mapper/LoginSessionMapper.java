package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.entity.LoginSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录会话数据访问层
 * <p>继承 MyBatis-Plus BaseMapper，提供登录会话表的 CRUD 操作</p>
 */
@Mapper
public interface LoginSessionMapper extends BaseMapper<LoginSession> {
}
