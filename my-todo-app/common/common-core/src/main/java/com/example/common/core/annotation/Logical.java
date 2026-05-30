package com.example.common.core.annotation;

/**
 * 权限校验逻辑枚举
 * <p>
 * 用于 {@link RequiresPermission} 注解中指定多个权限之间的逻辑关系：
 * <ul>
 *   <li>AND - 需要同时拥有所有指定权限</li>
 *   <li>OR - 只需拥有任意一个指定权限</li>
 * </ul>
 * </p>
 */
public enum Logical {

    /** 需要同时拥有所有指定权限 */
    AND,

    /** 只需拥有任意一个指定权限 */
    OR
}
