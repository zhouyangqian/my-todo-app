package com.example.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.permission.entity.Department;

import java.util.List;

/**
 * 部门服务接口
 */
public interface DepartmentService extends IService<Department> {

    List<Department> getDepartmentTree(Long tenantId);

    Department createDepartment(Department department);

    Department updateDepartment(Department department);

    void deleteDepartment(Long id, Long tenantId);
}
