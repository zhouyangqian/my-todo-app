package com.example.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.annotation.RequiresPermission;
import com.example.finance.entity.PaymentRecord;
import com.example.finance.export.ExcelExporter;
import com.example.finance.service.PaymentRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 收支记录导出控制器
 * <p>
 * 提供收支记录的 Excel 导出功能。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/finance/transactions")
@RequiredArgsConstructor
@Tag(name = "收支导出", description = "收支记录导出API")
public class TransactionExportController {

    private final PaymentRecordService paymentRecordService;

    @RequiresPermission(code = "finance:record:export", name = "导出收支记录")
    @Operation(summary = "导出收支记录Excel")
    @GetMapping("/export")
    public void exportTransactions(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) throws IOException {

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode(
                "收支记录_" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".xlsx",
                StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        // 查询收支记录
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getTenantId, tenantId)
                .eq(PaymentRecord::getDeleted, 0);
        if (type != null) {
            wrapper.eq(PaymentRecord::getRecordType, type);
        }
        wrapper.orderByDesc(PaymentRecord::getCreatedAt);
        List<PaymentRecord> records = paymentRecordService.list(wrapper);

        // 使用 ExcelExporter 导出
        String[] headers = {"ID", "单据编号", "收支类型", "金额", "交易日期", "状态", "备注"};
        String[] fields = {"id", "recordNo", "recordType", "amount", "transactionDate", "status", "remark"};
        ExcelExporter.export(records, headers, fields, response.getOutputStream());

        log.info("导出收支记录: {} 条, tenantId={}", records.size(), tenantId);
    }
}
