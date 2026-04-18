package com.example.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.user.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户详细信息数据访问接口（Mapper）
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper<UserProfile>，自动获得对 sys_user_profile 表的
 * 基础 CRUD 操作能力。用于处理用户扩展信息（个人资料、人事信息等）的数据库访问。
 * </p>
 *
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 * @see UserProfile 用户详细信息实体类
 */
@Mapper  // 标记为 MyBatis Mapper 接口，由 Spring 自动扫描并生成代理实现类
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
