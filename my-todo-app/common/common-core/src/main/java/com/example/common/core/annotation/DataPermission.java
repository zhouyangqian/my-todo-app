package com.example.common.core.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * <p>
 * 标注在 Controller 方法上，启用行级数据权限过滤。
 * 根据 value 指定的过滤类型，自动在 SQL 查询中添加数据过滤条件。
 * </p>
 * <p>
 * 过滤类型：
 * <ul>
 *   <li>DEPT - 按部门过滤，只能查看本部门数据</li>
 *   <li>DEPT_AND_SUB - 按部门及子部门过滤</li>
 *   <li>SELF - 仅查看本人数据</li>
 *   <li>ALL - 查看所有数据（需要特定权限）</li>
 * </ul>
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /** 数据权限过滤类型，默认按部门过滤 */
    DataScope value() default DataScope.DEPT;

    /** 数据权限字段名（对应 SQL 中的字段），默认为 "dept_id" */
    String deptField() default "dept_id";

    /** 用户字段名，默认为 "created_by" */
    String userField() default "created_by";

    /**
     * 数据权限范围枚举
     */
    enum DataScope {
        ALL,          // 全部数据
        DEPT,         // 本部门数据
        DEPT_AND_SUB, // 本部门及子部门数据
        SELF          // 仅本人数据
    }
}
