package com.example.common.core.annotation;

import java.lang.annotation.*;

/**
 * 数据范围注解
 * <p>
 * 标注在 Controller 方法上，用于指定数据权限查询时的表别名和部门字段映射。
 * 配合数据权限拦截器使用，为行级数据过滤提供表结构信息。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * &#64;DataScope(tableAlias = "t", deptIdColumn = "dept_id")
 * &#64;GetMapping("/list")
 * public ApiResponse&lt;List&lt;Order&gt;&gt; listOrders() { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 表别名（如 "t"、"u"），用于 SQL 条件中的字段前缀
     */
    String tableAlias() default "";

    /**
     * 部门ID列名，默认为 "dept_id"
     */
    String deptIdColumn() default "dept_id";
}
