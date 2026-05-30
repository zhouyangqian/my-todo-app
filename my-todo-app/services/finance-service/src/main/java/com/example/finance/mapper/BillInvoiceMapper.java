package com.example.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.finance.entity.BillInvoice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账单-发票关联数据访问接口（Mapper）
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，自动获得对 fin_bill_invoice 表的
 * 基础 CRUD 操作，无需手写 SQL。
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@Mapper
public interface BillInvoiceMapper extends BaseMapper<BillInvoice> {
}
