package com.example.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.finance.entity.BankAccount;
import org.apache.ibatis.annotations.Mapper;

/**
 * 银行账户数据访问接口（Mapper）
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，自动获得对 fin_bank_account 表的
 * 基础 CRUD 操作（增删改查），无需手写 SQL。
 * 如有复杂查询需求，可在此接口中定义自定义方法并配合 XML 映射文件实现。
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@Mapper
public interface BankAccountMapper extends BaseMapper<BankAccount> {
}
