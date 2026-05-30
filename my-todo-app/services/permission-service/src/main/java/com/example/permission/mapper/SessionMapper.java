package com.example.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.permission.entity.Session;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话数据访问接口
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，提供会话表的增删改查基础操作。
 * </p>
 */
@Mapper
public interface SessionMapper extends BaseMapper<Session> {
}
