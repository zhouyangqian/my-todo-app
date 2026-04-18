package com.example.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置类
 * <p>
 * 配置以下核心功能：
 * <ul>
 *   <li>分页插件 - 支持多种数据库的分页查询</li>
 *   <li>多租户插件 - 自动在 SQL 中注入租户ID条件，实现数据隔离</li>
 *   <li>乐观锁插件 - 防止并发更新导致的数据不一致</li>
 *   <li>防止全表更新删除 - 避免误操作导致的数据丢失</li>
 *   <li>自动填充 - 自动设置 createTime、updateTime 等字段</li>
 * </ul>
 * </p>
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器链
     * <p>
     * 按顺序添加以下拦截器：
     * <ol>
     *   <li>多租户拦截器 - 必须在第一位，在 SQL 执行前注入租户条件</li>
     *   <li>分页拦截器 - 自动处理分页查询</li>
     *   <li>乐观锁拦截器 - 处理 version 字段的乐观锁逻辑</li>
     *   <li>防止全表更新删除拦截器 - 安全防护</li>
     * </ol>
     * </p>
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 多租户拦截器 - 必须添加在第一位
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(new MultiTenantHandler());
        interceptor.addInnerInterceptor(tenantInterceptor);

        // 2. 分页拦截器
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置单页分页条数限制（可选，防止恶意查询超大分页）
        paginationInterceptor.setMaxLimit(1000L);
        // 当查询总数为 0 时，不执行 count 语句（提升性能）
        paginationInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInterceptor);

        // 3. 乐观锁拦截器
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 4. 防止全表更新和删除拦截器
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    /**
     * 自动填充处理器
     * <p>
     * 在插入和更新操作时，自动填充以下字段：
     * <ul>
     *   <li>createTime - 插入时自动设置为当前时间</li>
     *   <li>updateTime - 插入和更新时自动设置为当前时间</li>
     * </ul>
     * </p>
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                // 插入时自动填充创建时间和更新时间
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                // 更新时自动填充更新时间
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
