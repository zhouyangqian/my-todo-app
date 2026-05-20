package com.example.common.core.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * <p>
 * 标注在 Controller 方法上，表示该接口需要指定权限才能访问。
 * 由各服务的 AOP 切面拦截并调用 permission-service 进行权限校验。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * &#64;RequiresPermission(code = "system:user:kick", name = "踢出用户")
 * &#64;PostMapping("/kick-user/{id}")
 * public ApiResponse&lt;Void&gt; kickUser(&#64;PathVariable Long id) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {

    /**
     * 权限编码（如 system:user:kick）
     */
    String code();

    /**
     * 权限名称（用于日志和错误提示）
     */
    String name() default "";
}
