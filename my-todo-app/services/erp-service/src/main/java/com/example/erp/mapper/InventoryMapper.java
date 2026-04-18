package com.example.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erp.entity.Inventory;
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
 *
 * @author ERP系统
 * @since 1.0
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 扣减库存数量
     * <p>
     * 通过 SQL 条件判断确保库存不会被扣成负数（WHERE quantity >= #{quantity}），
     * 配合乐观锁（version字段）防止并发超卖。返回受影响行数，0表示扣减失败。
     * </p>
     *
     * @param id       库存记录ID
     * @param quantity 需要扣减的数量（正数）
     * @return 受影响的行数，0表示库存不足或版本冲突导致扣减失败
     */
    int decreaseStock(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    /**
     * 增加库存数量
     * <p>
     * 将指定库存记录的数量增加指定值，配合乐观锁确保并发安全。
     * </p>
     *
     * @param id       库存记录ID
     * @param quantity 需要增加的数量（正数）
     * @return 受影响的行数，0表示版本冲突导致增加失败
     */
    int increaseStock(@Param("id") Long id, @Param("quantity") BigDecimal quantity);
}
