package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志数据访问层
 * <p>继承 MyBatis-Plus BaseMapper，提供登录日志表的 CRUD 操作</p>
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
}
