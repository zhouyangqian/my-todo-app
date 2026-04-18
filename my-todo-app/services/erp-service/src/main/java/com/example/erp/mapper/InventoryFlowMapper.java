package com.example.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erp.entity.InventoryFlow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存流水数据访问接口
 * <p>
 * 基于 MyBatis-Plus 的 BaseMapper，提供库存流水表的通用 CRUD 操作。
 * 库存流水记录了所有出入库操作的详细日志，包括变动数量、变动前后数量、
 * 关联业务单号等信息，确保库存变动的完整追溯链。
 * 流水记录通常只插入不修改/删除，保证数据的不可篡改性。
 * </p>
 *
 * @author ERP系统
 * @since 1.0
 */
@Mapper
public interface InventoryFlowMapper extends BaseMapper<InventoryFlow> {
}
