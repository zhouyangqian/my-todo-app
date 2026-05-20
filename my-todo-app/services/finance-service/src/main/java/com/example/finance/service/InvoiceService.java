package com.example.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.exception.BusinessException;
import com.example.finance.entity.Invoice;
import com.example.finance.mapper.InvoiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 发票服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService extends ServiceImpl<InvoiceMapper, Invoice> {

    public Page<Invoice> getPage(Long tenantId, int page, int size,
                                  Integer invoiceType, Integer invoiceDirection, Integer status) {
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getTenantId, tenantId)
               .eq(Invoice::getDeleted, 0)
               .eq(invoiceType != null, Invoice::getInvoiceType, invoiceType)
               .eq(invoiceDirection != null, Invoice::getInvoiceDirection, invoiceDirection)
               .eq(status != null, Invoice::getStatus, status)
               .orderByDesc(Invoice::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    public Invoice getInvoiceById(Long id) {
        Invoice invoice = getById(id);
        if (invoice == null || invoice.getDeleted() == 1) {
            throw new BusinessException("发票不存在");
        }
        return invoice;
    }

    @Transactional
    public Invoice create(Invoice invoice) {
        if (invoice.getStatus() == null) {
            invoice.setStatus(0);
        }
        invoice.setDeleted(0);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());
        if (invoice.getInvoiceNo() == null || invoice.getInvoiceNo().isEmpty()) {
            invoice.setInvoiceNo(generateInvoiceNo());
        }
        save(invoice);
        log.info("创建发票: invoiceNo={}, tenantId={}", invoice.getInvoiceNo(), invoice.getTenantId());
        return invoice;
    }

    @Transactional
    public Invoice update(Invoice invoice) {
        Invoice existing = getInvoiceById(invoice.getId());
        if (existing.getStatus() != 0) {
            throw new BusinessException("只有待开票状态的发票才能修改");
        }
        invoice.setUpdatedAt(LocalDateTime.now());
        updateById(invoice);
        log.info("更新发票: id={}", invoice.getId());
        return invoice;
    }

    @Transactional
    public void delete(Long id) {
        Invoice existing = getInvoiceById(id);
        if (existing.getStatus() != 0) {
            throw new BusinessException("只有待开票状态的发票才能删除");
        }
        existing.setDeleted(1);
        existing.setUpdatedAt(LocalDateTime.now());
        updateById(existing);
        log.info("删除发票: id={}", id);
    }

    @Transactional
    public void voidInvoice(Long id, Long userId) {
        Invoice existing = getInvoiceById(id);
        if (existing.getStatus() == 2) {
            throw new BusinessException("发票已作废，不能重复操作");
        }
        existing.setStatus(2);
        existing.setUpdatedBy(userId);
        existing.setUpdatedAt(LocalDateTime.now());
        updateById(existing);
        log.info("作废发票: id={}, operatorId={}", id, userId);
    }

    public Map<String, Object> getStatistics(Long tenantId) {
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getTenantId, tenantId)
               .eq(Invoice::getDeleted, 0);
        List<Invoice> invoices = list(wrapper);

        Map<String, Object> stats = new HashMap<>();
        Map<Integer, Long> countByStatus = invoices.stream()
                .collect(Collectors.groupingBy(Invoice::getStatus, Collectors.counting()));
        stats.put("countByStatus", countByStatus);
        stats.put("totalCount", invoices.size());

        return stats;
    }

    private String generateInvoiceNo() {
        return "FP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));
    }
}
