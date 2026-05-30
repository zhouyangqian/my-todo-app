package com.example.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.permission.entity.Department;
import com.example.permission.mapper.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务实现类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供部门相关的核心业务逻辑：
 * - 部门树形结构查询
 * - 部门的创建、更新、删除
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    /**
     * 获取部门树形结构
     * <p>
     * 根据租户ID查询该租户下的所有部门，并构建成树形结构返回。
     * 树形结构通过 parentId 字段关联父子部门节点。
     * </p>
     *
     * @param tenantId 租户ID
     * @return 部门树列表（顶级部门节点列表）
     */
    @Override
    public List<Department> getDepartmentTree(Long tenantId) {
        // 查询所有部门
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getTenantId, tenantId)
               .eq(Department::getDeleted, 0)
               .orderByAsc(Department::getSort);

        List<Department> allDepartments = list(wrapper);

        // 构建树形结构
        return buildTree(allDepartments, 0L);
    }

    /**
     * 构建树形结构
     *
     * @param allDepartments 所有部门列表
     * @param parentId 父部门ID
     * @return 树形结构的部门列表
     */
    private List<Department> buildTree(List<Department> allDepartments, Long parentId) {
        List<Department> tree = new ArrayList<>();

        for (Department dept : allDepartments) {
            // 处理 parentId 为 null 的情况，视为顶级部门
            Long currentParentId = dept.getParentId();
            if (currentParentId == null) {
                currentParentId = 0L;
            }

            // 找到当前父节点的子节点
            if (currentParentId.equals(parentId)) {
                // 递归查找子节点
                List<Department> children = buildTree(allDepartments, dept.getId());
                dept.setChildren(children);
                tree.add(dept);
            }
        }

        return tree;
    }

    /**
     * 创建部门
     * <p>
     * 创建新部门，会校验部门编码在同一租户下的唯一性
     * </p>
     *
     * @param department 部门实体对象
     * @return 创建成功的部门对象
     */
    @Override
    public Department createDepartment(Department department) {
        // 校验部门编码唯一性
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getTenantId, department.getTenantId())
               .eq(Department::getDeptCode, department.getDeptCode())
               .eq(Department::getDeleted, 0);

        if (count(wrapper) > 0) {
            throw new IllegalArgumentException("部门编码已存在: " + department.getDeptCode());
        }

        save(department);
        return department;
    }

    /**
     * 更新部门
     * <p>
     * 更新指定ID的部门信息，会校验部门编码在同一租户下的唯一性（排除自身）
     * </p>
     *
     * @param department 部门实体对象
     * @return 更新后的部门对象
     */
    @Override
    public Department updateDepartment(Department department) {
        // 校验部门编码唯一性（排除自身）
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getTenantId, department.getTenantId())
               .eq(Department::getDeptCode, department.getDeptCode())
               .eq(Department::getDeleted, 0)
               .ne(Department::getId, department.getId());

        if (count(wrapper) > 0) {
            throw new IllegalArgumentException("部门编码已存在: " + department.getDeptCode());
        }

        updateById(department);
        return department;
    }

    /**
     * 删除部门（逻辑删除）
     * <p>
     * 逻辑删除指定部门，如果有子部门则不允许删除
     * </p>
     *
     * @param id 部门ID
     * @param tenantId 租户ID
     */
    @Override
    public void deleteDepartment(Long id, Long tenantId) {
        // 检查是否有子部门
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getParentId, id)
               .eq(Department::getTenantId, tenantId)
               .eq(Department::getDeleted, 0);

        if (count(wrapper) > 0) {
            throw new IllegalArgumentException("该部门下有子部门，不能删除");
        }

        // 逻辑删除
        Department department = getById(id);
        if (department != null) {
            department.setDeleted(1);
            updateById(department);
        }
    }
}
