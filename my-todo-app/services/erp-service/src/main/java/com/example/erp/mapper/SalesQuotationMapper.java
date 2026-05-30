package com.example.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erp.entity.SalesQuotation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 销售报价单数据访问接口
 * <p>
 * 基于 MyBatis-Plus 的 BaseMapper，提供销售报价单表的通用 CRUD 操作。
 * </p>
 *
 * @author ERP系统
 * @since 1.0
 */
@Mapper
public interface SalesQuotationMapper extends BaseMapper<SalesQuotation> {
}
