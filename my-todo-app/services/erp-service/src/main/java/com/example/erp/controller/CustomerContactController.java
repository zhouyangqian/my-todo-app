package com.example.erp.controller;

import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.erp.entity.CustomerContact;
import com.example.erp.service.CustomerContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户联系人管理控制器
 */
@Tag(name = "客户联系人管理", description = "客户联系人增删改查API")
@RestController
@RequestMapping("/api/erp/customer-contacts")
@RequiredArgsConstructor
public class CustomerContactController {

    private final CustomerContactService customerContactService;

    @RequiresPermission(code = "erp:customer-contact:list", name = "查询客户联系人列表")
    @Operation(summary = "根据客户ID查询联系人列表")
    @GetMapping("/customer/{customerId}")
    public ApiResponse<List<CustomerContact>> getContactsByCustomerId(@PathVariable Long customerId) {
        List<CustomerContact> contacts = customerContactService.getContactsByCustomerId(customerId);
        return ApiResponse.success(contacts);
    }

    @RequiresPermission(code = "erp:customer-contact:create", name = "新增客户联系人")
    @Operation(summary = "创建客户联系人")
    @PostMapping
    public ApiResponse<CustomerContact> createContact(
            @RequestBody CustomerContact contact,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        contact.setTenantId(tenantId);
        contact.setCreatedBy(userId);
        CustomerContact created = customerContactService.createContact(contact);
        return ApiResponse.success(created);
    }

    @RequiresPermission(code = "erp:customer-contact:update", name = "更新客户联系人")
    @Operation(summary = "更新客户联系人")
    @PutMapping("/{id}")
    public ApiResponse<CustomerContact> updateContact(
            @PathVariable Long id,
            @RequestBody CustomerContact contact,
            @RequestHeader("X-User-Id") Long userId) {
        contact.setId(id);
        contact.setUpdatedBy(userId);
        CustomerContact updated = customerContactService.updateContact(contact);
        return ApiResponse.success(updated);
    }

    @RequiresPermission(code = "erp:customer-contact:delete", name = "删除客户联系人")
    @Operation(summary = "删除客户联系人")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteContact(@PathVariable Long id) {
        customerContactService.deleteContact(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:customer-contact:update", name = "设置客户默认联系人")
    @Operation(summary = "设置默认联系人")
    @PutMapping("/{id}/default")
    public ApiResponse<Void> setDefaultContact(@PathVariable Long id) {
        customerContactService.setDefaultContact(id);
        return ApiResponse.success();
    }
}
