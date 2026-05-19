package com.example.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erp.entity.PurchaseOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购订单明细数据访问接口
 */
@Mapper
public interface PurchaseOrderItemMapper extends BaseMapper<PurchaseOrderItem> {
}
