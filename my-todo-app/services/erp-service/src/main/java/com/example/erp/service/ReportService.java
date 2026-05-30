package com.example.erp.service;

import com.example.erp.api.dto.DashboardDTO;
import com.example.erp.api.dto.SalesReportDTO;
import com.example.erp.api.dto.PurchaseReportDTO;
import com.example.erp.api.dto.InventoryReportDTO;
import com.example.erp.api.dto.ProfitReportDTO;
import com.example.erp.api.dto.SupplierStatementDTO;
import com.example.erp.api.dto.CustomerStatementDTO;

/**
 * 报表统计服务接口
 */
public interface ReportService {

    DashboardDTO getDashboardData(Long tenantId);

    SalesReportDTO getSalesReport(Long tenantId, String startDate, String endDate);

    PurchaseReportDTO getPurchaseReport(Long tenantId, String startDate, String endDate);

    InventoryReportDTO getInventoryReport(Long tenantId, Long warehouseId);

    ProfitReportDTO getProfitReport(Long tenantId, String startDate, String endDate);

    SupplierStatementDTO getSupplierStatement(Long tenantId, Long supplierId, String startDate, String endDate);

    CustomerStatementDTO getCustomerStatement(Long tenantId, Long customerId, String startDate, String endDate);
}
