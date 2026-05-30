package com.example.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.finance.entity.Bill;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账单数据访问接口（Mapper）
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，自动获得对 fin_bill 表的
 * 基础 CRUD 操作，无需手写 SQL。
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@Mapper
public interface BillMapper extends BaseMapper<Bill> {
}
