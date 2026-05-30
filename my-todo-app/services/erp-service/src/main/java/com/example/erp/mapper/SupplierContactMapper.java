package com.example.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erp.entity.SupplierContact;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商联系人数据访问接口
 * <p>
 * 基于 MyBatis-Plus 的 BaseMapper，提供供应商联系人表的通用 CRUD 操作。
 * </p>
 *
 * @author ERP系统
 * @since 1.0
 */
@Mapper
public interface SupplierContactMapper extends BaseMapper<SupplierContact> {
}
