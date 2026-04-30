package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.erp.entity.Customer;
import com.example.erp.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 客户管理控制器
 */
@Tag(name = "客户管理", description = "客户增删改查API")
@RestController
@RequestMapping("/api/erp/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "分页查询客户")
    @GetMapping("/get-customer-page")
    public ApiResponse<PageResult<Customer>> getCustomerPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Integer status) {
        Page<Customer> result = customerService.getCustomerPage(tenantId, page, size, customerName, status);
        PageResult<Customer> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @Operation(summary = "获取客户详情")
    @GetMapping("/get-customer/{id}")
    public ApiResponse<Customer> getCustomer(@PathVariable Long id) {
        Customer customer = customerService.getById(id);
        return ApiResponse.success(customer);
    }

    @Operation(summary = "创建客户")
    @PostMapping("/create-customer")
    public ApiResponse<Customer> createCustomer(
            @RequestBody Customer customer,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        customer.setTenantId(tenantId);
        customer.setCreatedBy(userId);
        Customer created = customerService.createCustomer(customer);
        return ApiResponse.success(created);
    }

    @Operation(summary = "更新客户")
    @PutMapping("/update-customer/{id}")
    public ApiResponse<Customer> updateCustomer(
            @PathVariable Long id,
            @RequestBody Customer customer,
            @RequestHeader("X-User-Id") Long userId) {
        customer.setId(id);
        customer.setUpdatedBy(userId);
        Customer updated = customerService.updateCustomer(customer);
        return ApiResponse.success(updated);
    }

    @Operation(summary = "删除客户")
    @DeleteMapping("/delete-customer/{id}")
    public ApiResponse<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ApiResponse.success();
    }
}
