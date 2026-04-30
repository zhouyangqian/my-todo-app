package com.example.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erp.entity.SalesShipmentItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 销售出库单明细数据访问层
 */
@Mapper
public interface SalesShipmentItemMapper extends BaseMapper<SalesShipmentItem> {
}
