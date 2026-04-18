package com.example.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.user.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收货地址数据访问接口（Mapper）
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper<UserAddress>，自动获得对 sys_user_address 表的
 * 基础 CRUD 操作能力。用于处理用户收货地址的数据库访问，
 * 包括地址的新增、查询、修改和逻辑删除等操作。
 * </p>
 *
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 * @see UserAddress 用户收货地址实体类
 */
@Mapper  // 标记为 MyBatis Mapper 接口，由 Spring 自动扫描并生成代理实现类
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}
