package com.example.finance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.finance.entity.PaymentRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 收支记录业务服务接口
 *
 * @author finance-team
 * @since 1.0
 */
public interface PaymentRecordService extends IService<PaymentRecord> {

    Page<PaymentRecord> getPage(Long tenantId, int page, int size,
                                 Integer recordType, Integer bizType,
                                 LocalDateTime startDate, LocalDateTime endDate);

    PaymentRecord create(PaymentRecord record);

    void approve(Long id, Long approverId);

    void cancel(Long id);

    List<PaymentRecord> getByDateRange(Long tenantId, Integer recordType,
                                        LocalDate startDate, LocalDate endDate);
}
