package com.example.erp.controller;

import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.erp.entity.SupplierContact;
import com.example.erp.service.SupplierContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应商联系人管理控制器
 */
@Tag(name = "供应商联系人管理", description = "供应商联系人增删改查API")
@RestController
@RequestMapping("/api/erp/supplier-contacts")
@RequiredArgsConstructor
public class SupplierContactController {

    private final SupplierContactService supplierContactService;

    @RequiresPermission(code = "erp:supplier-contact:list", name = "查询供应商联系人列表")
    @Operation(summary = "根据供应商ID查询联系人列表")
    @GetMapping("/supplier/{supplierId}")
    public ApiResponse<List<SupplierContact>> getContactsBySupplierId(@PathVariable Long supplierId) {
        List<SupplierContact> contacts = supplierContactService.getContactsBySupplierId(supplierId);
        return ApiResponse.success(contacts);
    }

    @RequiresPermission(code = "erp:supplier-contact:create", name = "新增供应商联系人")
    @Operation(summary = "创建供应商联系人")
    @PostMapping
    public ApiResponse<SupplierContact> createContact(
            @RequestBody SupplierContact contact,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        contact.setTenantId(tenantId);
        contact.setCreatedBy(userId);
        SupplierContact created = supplierContactService.createContact(contact);
        return ApiResponse.success(created);
    }

    @RequiresPermission(code = "erp:supplier-contact:update", name = "更新供应商联系人")
    @Operation(summary = "更新供应商联系人")
    @PutMapping("/{id}")
    public ApiResponse<SupplierContact> updateContact(
            @PathVariable Long id,
            @RequestBody SupplierContact contact,
            @RequestHeader("X-User-Id") Long userId) {
        contact.setId(id);
        contact.setUpdatedBy(userId);
        SupplierContact updated = supplierContactService.updateContact(contact);
        return ApiResponse.success(updated);
    }

    @RequiresPermission(code = "erp:supplier-contact:delete", name = "删除供应商联系人")
    @Operation(summary = "删除供应商联系人")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteContact(@PathVariable Long id) {
        supplierContactService.deleteContact(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:supplier-contact:update", name = "设置供应商默认联系人")
    @Operation(summary = "设置默认联系人")
    @PutMapping("/{id}/default")
    public ApiResponse<Void> setDefaultContact(@PathVariable Long id) {
        supplierContactService.setDefaultContact(id);
        return ApiResponse.success();
    }
}
