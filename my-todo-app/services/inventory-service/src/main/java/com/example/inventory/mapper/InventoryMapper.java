package com.example.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.inventory.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * 库存数据访问接口
 * <p>
 * 基于 MyBatis-Plus 的 BaseMapper，提供库存表的通用 CRUD 操作。
 * 额外提供了库存扣减和库存增加的自定义 SQL 方法，
 * 这些方法配合乐观锁使用，确保并发场景下库存操作的原子性。
 * </p>
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 扣减库存数量（带乐观锁）
     *
     * @param id       库存记录ID
     * @param quantity 需要扣减的数量（正数）
     * @return 受影响的行数，0表示库存不足或版本冲突导致扣减失败
     */
    int decreaseStock(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    /**
     * 增加库存数量
     *
     * @param id       库存记录ID
     * @param quantity 需要增加的数量（正数）
     * @return 受影响的行数
     */
    int increaseStock(@Param("id") Long id, @Param("quantity") BigDecimal quantity);
}
