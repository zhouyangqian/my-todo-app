# Research: 用户模块技术选型

**Feature**: 001-user-module
**Date**: 2026-01-10
**Purpose**: 记录技术选型决策和最佳实践研究

## MyBatis-Plus 租户插件配置

### Decision: 使用 MyBatis-Plus TenantLineInnerInterceptor

**Rationale**:
- MyBatis-Plus 3.4+ 内置租户插件，无需额外依赖
- 自动在 SQL 查询中添加租户过滤条件
- 支持自动填充租户 ID 字段

**Alternatives Considered**:
- 手动在每条 SQL 中添加 tenant_id 条件：代码冗余，容易遗漏
- 使用 AOP 拦截器：需要手动编写 SQL 拦截逻辑

**Configuration**:

```java
@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加租户插件
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(new TenantLineHandler() {
            @Override
            public Long getTenantId() {
                // 从 JWT Token 或 ThreadLocal 获取租户 ID
                return TenantContext.getTenantId();
            }

            @Override
            public boolean ignoreTable(String tableName) {
                // 忽略系统表（如权限表）
                return "sys_permission".equals(tableName);
            }
        });

        interceptor.addInnerInterceptor(tenantInterceptor);
        return interceptor;
    }
}
```

## Apache POI Excel 处理

### Decision: 使用 Apache POI 5.2+ 处理 Excel

**Rationale**:
- Apache POI 是成熟的 Java Excel 处理库
- 支持 .xls 和 .xlsx 格式
- 提供流式 API，适合处理大文件

**Alternatives Considered**:
- EasyExcel：阿里开源，但在模板处理上不如 POI 灵活
- Hutool-poi：工具类封装，但增加额外依赖

**Implementation Strategy**:

```java
@Service
public class UserImportService {

    // 同步处理小文件（<1000条）
    public ImportResult importSync(MultipartFile file, Long tenantId) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            // 逐行解析并验证
            // 检查租户用户数量限制
            // 批量插入数据库
            return ImportResult.success(count, errors);
        }
    }

    // 异步处理大文件（>=1000条）
    @Async
    public void importAsync(MultipartFile file, Long tenantId, ImportCallback callback) {
        // 使用临时文件存储
        // 分批读取和处理
        // 进度回调通知
    }
}
```

## Vue 3 + Ant Design 批量操作

### Decision: 使用 Ant Design Table rowSelection + 批量操作栏

**Rationale**:
- Ant Design Table 内置 rowSelection，无需额外开发
- 批量操作栏放在表格顶部，符合用户习惯
- 支持全选、跨页选择

**Implementation Strategy**:

```vue
<template>
  <a-card>
    <template #extra>
      <a-space>
        <a-button
          type="primary"
          :disabled="!hasSelected"
          @click="batchEnable"
        >
          批量启用 ({{ selectedRowKeys.length }})
        </a-button>
        <a-button
          danger
          :disabled="!hasSelected"
          @click="batchDisable"
        >
          批量禁用
        </a-button>
      </a-space>
    </template>

    <a-table
      :row-selection="{
        selectedRowKeys,
        onChange: onSelectChange,
        preserveSelectedRowKeys: true
      }"
      :columns="columns"
      :data-source="users"
    />
  </a-card>
</template>
```

## 异步任务处理

### Decision: 使用 @Async + ThreadPoolExecutor 处理大批量导入

**Rationale**:
- Spring Boot 内置 @Async 支持
- 可配置线程池大小和队列
- 支持 Future 返回值和进度查询

**Alternatives Considered**:
- 消息队列（RabbitMQ/Kafka）：增加系统复杂度，当前不需要
- XXL-Job 分布式任务：对于导入任务过于重量级

**Configuration**:

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("importExecutor")
    public Executor importExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("user-import-");
        executor.initialize();
        return executor;
    }
}
```

## MyBatis-Plus 软删除

### Decision: 使用 @TableLogic + deleted 字段

**Rationale**:
- MyBatis-Plus 内置 @TableLogic 注解
- 自动在查询中添加 WHERE deleted = 0
- 支持恢复（逻辑删除撤销）

**Implementation**:

```java
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long userId;

    @TableLogic
    @TableField("deleted")
    private Integer deleted; // 0=正常, 1=已删除

    // 其他字段...
}
```

## 租户用户数量限制

### Decision: 在 UserService 创建用户前检查租户用户数量

**Implementation**:

```java
@Service
public class UserService {

    @Transactional
    public void createUser(User user, Long tenantId) {
        // 检查租户用户数量限制
        Tenant tenant = tenantService.getById(tenantId);
        long currentUserCount = countByTenantId(tenantId);

        if (currentUserCount >= tenant.getUserLimit()) {
            throw new UserLimitExceededException(
                "用户数量已达上限（" + tenant.getUserLimit() + "个），请升级套餐"
            );
        }

        // 创建用户...
    }
}
```

## 审计日志

### Decision: 使用 Spring AOP + @Async 记录审计日志

**Rationale**:
- AOP 自动拦截所有用户操作
- 异步记录日志不影响主业务性能
- 日志包含租户 ID、用户 ID、操作类型、目标 ID

**Implementation**:

```java
@Aspect
@Component
public class AuditLogAspect {

    @Around("@annotation(auditLog)")
    public Object logAudit(ProceedingJoinPoint pjp) throws Throwable {
        // 记录操作前状态
        long startTime = System.currentTimeMillis();

        try {
            Object result = pjp.proceed();

            // 异步记录成功日志
            auditLogService.logAsync(
                TenantContext.getTenantId(),
                SecurityContext.getUserId(),
                pjp.getSignature().getName(),
                "SUCCESS",
                System.currentTimeMillis() - startTime
            );

            return result;
        } catch (Exception e) {
            // 记录失败日志
            auditLogService.logAsync(
                TenantContext.getTenantId(),
                SecurityContext.getUserId(),
                pjp.getSignature().getName(),
                "FAILED: " + e.getMessage(),
                System.currentTimeMillis() - startTime
            );
            throw e;
        }
    }
}
```

## 前端 ESLint + Prettier 配置

### Decision: 使用 @vue/eslint-config-prettier 避免冲突

**package.json**:

```json
{
  "devDependencies": {
    "eslint": "^8.57.0",
    "eslint-plugin-vue": "^9.23.0",
    "@vue/eslint-config-prettier": "^9.0.0",
    "prettier": "^3.2.5",
    "lint-staged": "^15.2.2",
    "husky": "^9.0.11"
  }
}
```

**.eslintrc.js**:

```javascript
module.exports = {
  extends: [
    'plugin:vue/vue3-essential',
    '@vue/eslint-config-prettier'
  ],
  rules: {
    'no-var': 'error',
    'prefer-const': 'error'
  }
}
```

**.prettierrc**:

```json
{
  "printWidth": 100,
  "tabWidth": 2,
  "useTabs": false,
  "semi": false,
  "singleQuote": true,
  "trailingComma": "es5",
  "bracketSpacing": true,
  "arrowParens": "avoid"
}
```
