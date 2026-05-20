package com.example.permission.controller;

import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.permission.entity.Department;
import com.example.permission.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 * <p>
 * 提供部门相关的RESTful API接口，包括：
 * - 查询部门树
 * - 部门的创建、更新、删除
 * </p>
 * <p>
 * 所有接口通过请求头 X-Tenant-Id 实现多租户隔离，
 * 通过请求头 X-User-Id 获取当前操作用户ID
 * </p>
 */
@Tag(name = "Department", description = "Department Management API")
@RestController
@RequestMapping("/api/system/dept")
@RequiredArgsConstructor
public class DepartmentController {

    /** 部门服务，处理部门相关的业务逻辑 */
    private final DepartmentService departmentService;

    /**
     * 获取部门树
     * <p>
     * 根据租户ID查询该租户下的所有部门，并构建成树形结构返回。
     * 树形结构通过 parentId 字段关联父子部门节点。
     * </p>
     *
     * @param tenantId 租户ID（请求头）
     * @return 部门树列表（顶级部门节点列表）
     */
    @RequiresPermission(code = "system:dept:list", name = "查询部门列表")
    @Operation(summary = "Get department tree")
    @GetMapping("/get-department-tree")
    public ApiResponse<List<Department>> getDepartmentTree(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        List<Department> tree = departmentService.getDepartmentTree(tenantId);
        return ApiResponse.success(tree);
    }

    /**
     * 创建部门
     * <p>
     * 创建新部门，会校验部门编码在同一租户下的唯一性
     * </p>
     *
     * @param department 部门实体对象（请求体）
     * @return 创建成功的部门对象
     */
    @Operation(summary = "Create department")
    @RequiresPermission(code = "system:dept:create", name = "新增部门")
    @PostMapping("/create-department")
    public ApiResponse<Department> createDepartment(@RequestBody Department department) {
        Department created = departmentService.createDepartment(department);
        return ApiResponse.success(created);
    }

    /**
     * 更新部门
     * <p>
     * 更新指定ID的部门信息，会校验部门编码在同一租户下的唯一性（排除自身）
     * </p>
     *
     * @param id 部门ID（路径参数）
     * @param department 部门实体对象（请求体）
     * @return 更新后的部门对象
     */
    @Operation(summary = "Update department")
    @RequiresPermission(code = "system:dept:update", name = "更新部门")
    @PutMapping("/update-department/{id}")
    public ApiResponse<Department> updateDepartment(
            @PathVariable Long id,
            @RequestBody Department department) {
        department.setId(id);
        Department updated = departmentService.updateDepartment(department);
        return ApiResponse.success(updated);
    }

    /**
     * 删除部门（逻辑删除）
     * <p>
     * 逻辑删除指定部门，如果有子部门则不允许删除
     * </p>
     *
     * @param id 部门ID（路径参数）
     * @param tenantId 租户ID（请求头）
     * @return 空响应
     */
    @Operation(summary = "Delete department")
    @RequiresPermission(code = "system:dept:delete", name = "删除部门")
    @DeleteMapping("/delete-department/{id}")
    public ApiResponse<Void> deleteDepartment(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        departmentService.deleteDepartment(id, tenantId);
        return ApiResponse.success();
    }
}
