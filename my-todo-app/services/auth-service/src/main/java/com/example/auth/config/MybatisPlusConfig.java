package com.example.auth.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置类
 * <p>
 * 配置分页插件和自动填充处理器。
 * 自动填充：插入时自动填充创建时间、更新时间、软删除标记、租户ID、操作人ID；
 * 更新时自动填充更新时间和操作人ID。
 * </p>
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器配置
     * <p>添加 MySQL 分页插件，使分页查询生效</p>
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 自动填充处理器
     * <p>
     * 从请求头中获取租户ID和用户ID，自动填充到实体对应字段。
     * 插入操作：填充创建时间、更新时间、删除标记、租户ID、创建人、更新人
     * 更新操作：填充更新时间、更新人
     * </p>
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            /** 插入时自动填充 */
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "deleted", Integer.class, 0);  // 默认未删除

                // 从请求头获取租户ID和用户ID（由网关注入）
                Long tenantId = getHeaderAsLong("X-Tenant-Id");
                Long userId = getHeaderAsLong("X-User-Id");

                if (tenantId != null) {
                    this.strictInsertFill(metaObject, "tenantId", Long.class, tenantId);
                }
                if (userId != null) {
                    this.strictInsertFill(metaObject, "createdBy", Long.class, userId);
                    this.strictInsertFill(metaObject, "updatedBy", Long.class, userId);
                }
            }

            /** 更新时自动填充 */
            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());

                Long userId = getHeaderAsLong("X-User-Id");
                if (userId != null) {
                    this.strictUpdateFill(metaObject, "updatedBy", Long.class, userId);
                }
            }

            /**
             * 从请求头中获取 Long 类型的值
             *
             * @param headerName 请求头名称
             * @return 解析后的 Long 值，获取失败返回 null
             */
            private Long getHeaderAsLong(String headerName) {
                try {
                    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attrs != null) {
                        String value = attrs.getRequest().getHeader(headerName);
                        if (value != null && !value.isEmpty()) {
                            return Long.parseLong(value);
                        }
                    }
                } catch (Exception ignored) {
                    // 获取请求头失败时忽略（非 HTTP 请求上下文中调用）
                }
                return null;
            }
        };
    }
}
