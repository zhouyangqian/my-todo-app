package com.example.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问接口（Mapper）
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper<User>，自动获得对 sys_user 表的
 * 基础 CRUD 操作能力，包括：插入、删除（含逻辑删除）、更新、
 * 单条/多条查询、分页查询等，无需手写 SQL。
 * </p>
 * <p>
 * 如需自定义 SQL，可在此接口中声明方法，并在对应的 XML 映射文件中编写 SQL。
 * </p>
 *
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 * @see User 用户实体类
 */
@Mapper  // 标记为 MyBatis Mapper 接口，由 Spring 自动扫描并生成代理实现类
public interface UserMapper extends BaseMapper<User> {
}
