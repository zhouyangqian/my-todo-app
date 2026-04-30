package com.example.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erp.entity.SalesShipment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 销售出库单数据访问层
 */
@Mapper
public interface SalesShipmentMapper extends BaseMapper<SalesShipment> {
}
