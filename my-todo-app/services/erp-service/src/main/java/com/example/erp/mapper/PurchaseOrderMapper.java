package com.example.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erp.entity.PurchaseOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购订单数据访问接口
 * <p>
 * 基于 MyBatis-Plus 的 BaseMapper，提供采购订单表的通用 CRUD 操作。
 * 包括插入、删除（逻辑删除）、更新、查询（单条/列表/分页）等基础数据访问能力。
 * </p>
 *
 * @author ERP系统
 * @since 1.0
 */
@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {
}
