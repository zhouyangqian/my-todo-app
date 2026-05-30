package com.example.common.core.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * <p>
 * 标注在 Controller 方法或类上，表示该接口需要指定权限才能访问。
 * 支持单个权限或多个权限校验，支持 AND/OR 逻辑关系。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 单个权限（兼容旧用法）
 * &#64;RequiresPermission(code = "system:user:kick", name = "踢出用户")
 *
 * // 多个权限 - AND 逻辑（默认）
 * &#64;RequiresPermission(value = {"system:user:list", "system:user:detail"}, logical = Logical.AND)
 *
 * // 多个权限 - OR 逻辑
 * &#64;RequiresPermission(value = {"system:user:list", "system:user:detail"}, logical = Logical.OR)
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {

    /**
     * 权限编码（如 system:user:kick）
     * <p>
     * 兼容旧用法，与 value() 二选一。如果同时指定了 value()，则优先使用 value()。
     * </p>
     */
    String code() default "";

    /**
     * 权限名称（用于日志和错误提示）
     */
    String name() default "";

    /**
     * 权限编码数组，支持多权限校验
     * <p>
     * 与 logical() 配合使用，默认 AND 逻辑。
     * 如果同时指定了 code() 和 value()，优先使用 value()。
     * </p>
     */
    String[] value() default {};

    /**
     * 多权限校验的逻辑关系，默认 AND（需要同时拥有所有权限）
     */
    Logical logical() default Logical.AND;
}
