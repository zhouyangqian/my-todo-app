package com.example.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.inventory.entity.InventoryFlow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存流水数据访问接口
 * <p>
 * 基于 MyBatis-Plus 的 BaseMapper，提供库存流水表的通用 CRUD 操作。
 * 库存流水记录了所有出入库操作的详细日志，确保库存变动的完整追溯链。
 * 流水记录通常只插入不修改/删除，保证数据的不可篡改性。
 * </p>
 */
@Mapper
public interface InventoryFlowMapper extends BaseMapper<InventoryFlow> {
}
