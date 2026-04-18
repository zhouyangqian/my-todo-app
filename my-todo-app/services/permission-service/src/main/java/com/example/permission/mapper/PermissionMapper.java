package com.example.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.permission.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限数据访问接口
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，提供权限表的增删改查基础操作。
 * 如需自定义SQL，可在此接口中声明方法并在对应的XML映射文件中编写SQL。
 * </p>
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}
