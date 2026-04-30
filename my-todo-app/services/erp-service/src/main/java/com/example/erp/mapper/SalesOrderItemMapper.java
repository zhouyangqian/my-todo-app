package com.example.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erp.entity.SalesOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 销售订单明细数据访问层
 */
@Mapper
public interface SalesOrderItemMapper extends BaseMapper<SalesOrderItem> {
}
