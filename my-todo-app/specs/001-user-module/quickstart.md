# Quick Start: 用户模块集成指南

**Feature**: 用户模块 (001-user-module)
**Date**: 2026-01-10
**Purpose**: 快速集成和使用用户管理功能

## 概述

本指南帮助开发者快速集成用户模块到多租户 SaaS 系统中。

## 前置条件

- Java 17+
- Spring Boot 3.0+
- MySQL 8.0+
- Redis 7.0+
- Vue 3.0 + Ant Design 6.1.4
- 已配置 002-permission-module 和 003-user-auth

## 快速开始

### 1. 添加依赖

在 `user-service/pom.xml` 中添加：

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.5</version>
    </dependency>

    <!-- MySQL Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <runtime>true</runtime>
    </dependency>

    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- Apache POI (Excel) -->
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId>
        <version>5.2.5</version>
    </dependency>

    <!-- Permission Module (002) -->
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>permission-client</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### 2. 配置数据库

在 `application.yml` 中配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/saas_db?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 3000ms

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### 3. 配置租户插件

```java
@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 租户插件
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(new TenantLineHandler() {
            @Override
            public Long getTenantId() {
                // 从 JWT Token 获取租户 ID
                return TenantContext.getTenantId();
            }

            @Override
            public boolean ignoreTable(String tableName) {
                // 忽略系统表
                return "sys_permission".equals(tableName) || "sys_role".equals(tableName);
            }
        });

        interceptor.addInnerInterceptor(tenantInterceptor);
        return interceptor;
    }
}
```

### 4. 创建用户实体

```java
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long userId;

    private String username;
    private String email;
    private String phone;
    private String passwordHash;

    @TableField("tenant_id")
    private Long tenantId;

    private Integer status;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

### 5. 用户服务实现

```java
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TenantService tenantService;

    @Transactional
    public void createUser(User user, Long tenantId) {
        // 检查租户用户数量限制
        Tenant tenant = tenantService.getById(tenantId);
        long currentUserCount = userMapper.selectCount(
            new LambdaQueryWrapper<User>()
                .eq(User::getTenantId, tenantId)
        );

        if (currentUserCount >= tenant.getUserLimit()) {
            throw new UserLimitExceededException(
                "用户数量已达上限（" + tenant.getUserLimit() + "个），请升级套餐"
            );
        }

        user.setTenantId(tenantId);
        userMapper.insert(user);

        // 记录审计日志
        auditLogService.log("CREATE_USER", user.getUserId());
    }

    public Page<User> listUsers(int page, int size, Long tenantId) {
        return userMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<User>()
                .eq(User::getTenantId, tenantId)
                .orderByDesc(User::getCreatedAt)
        );
    }
}
```

### 6. 控制器

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @RequiresPermission("user:view")
    public Result<Page<User>> list(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Long tenantId = TenantContext.getTenantId();
        return Result.success(userService.listUsers(page, size, tenantId));
    }

    @PostMapping
    @RequiresPermission("user:create")
    public Result<User> create(@RequestBody CreateUserRequest request) {
        Long tenantId = TenantContext.getTenantId();
        User user = userService.createUser(request, tenantId);
        return Result.success(user);
    }
}
```

### 7. 前端 API 客户端

```javascript
// frontend/src/api/user.js
import request from '@/utils/request'

export function listUsers(params) {
  return request({
    url: '/api/v1/users',
    method: 'get',
    params
  })
}

export function createUser(data) {
  return request({
    url: '/api/v1/users',
    method: 'post',
    data
  })
}

export function batchDisable(data) {
  return request({
    url: '/api/v1/users/batch/disable',
    method: 'post',
    data
  })
}

export function importUsers(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/v1/users/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function exportUsers(params) {
  return request({
    url: '/api/v1/users/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
```

### 8. 前端用户列表页面

```vue
<template>
  <a-card title="用户管理">
    <template #extra>
      <a-space>
        <a-button type="primary" @click="showCreateModal">
          创建用户
        </a-button>
        <a-button @click="showImportModal">
          导入用户
        </a-button>
        <a-button @click="exportUsers">
          导出用户
        </a-button>
      </a-space>
    </template>

    <!-- 批量操作栏 -->
    <div v-if="selectedRowKeys.length > 0" class="batch-bar">
      <a-space>
        <span>已选择 {{ selectedRowKeys.length }} 项</span>
        <a-button size="small" @click="batchDisable">
          批量禁用
        </a-button>
        <a-button size="small" danger @click="batchDelete">
          批量删除
        </a-button>
      </a-space>
    </div>

    <!-- 用户表格 -->
    <a-table
      :row-selection="{
        selectedRowKeys,
        onChange: onSelectChange
      }"
      :columns="columns"
      :data-source="users"
      :loading="loading"
      :pagination="pagination"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '正常' : '禁用' }}
          </a-tag>
        </template>
      </template>
    </a-table>
  </a-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listUsers, batchDisable, exportUsers } from '@/api/user'

const users = ref([])
const loading = ref(false)
const selectedRowKeys = ref([])

const columns = [
  { title: '用户名', dataIndex: 'username', key: 'username' },
  { title: '邮箱', dataIndex: 'email', key: 'email' },
  { title: '手机号', dataIndex: 'phone', key: 'phone' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt' }
]

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const { data } = await listUsers({
      page: pagination.value.current,
      size: pagination.value.pageSize
    })
    users.value = data.records
    pagination.value.total = data.total
  } finally {
    loading.value = false
  }
}

const onSelectChange = (keys) => {
  selectedRowKeys.value = keys
}

onMounted(() => {
  fetchUsers()
})
</script>
```

## 租户用户数量限制

默认情况下，每个租户最多创建 5 个用户。创建用户时会自动检查：

```java
if (currentUserCount >= tenant.getUserLimit()) {
    throw new UserLimitExceededException("用户数量已达上限");
}
```

## 审计日志

所有用户操作都会自动记录审计日志：

```java
@AuditLog(operation = "CREATE_USER", target = "user")
public User createUser(User user) {
    // 业务逻辑
    return user;
}
```

## 常见问题

### Q: 如何修改租户用户数量限制？

A: 通过 Tenant API 修改 `userLimit` 字段，或在数据库直接更新 `tenant.user_limit`。

### Q: 批量操作的数量限制是多少？

A: 批量操作最多支持 100 个用户，导入最多支持 1000 条记录。

### Q: 用户删除后可以恢复吗？

A: 用户采用软删除，可以通过设置 `deleted=0` 恢复用户。

### Q: 如何处理租户隔离？

A: MyBatis-Plus 租户插件会自动在所有 SQL 查询中添加 `WHERE tenant_id = ?` 条件。

## 下一步

- 查看完整 API 文档: `contracts/`
- 了解数据模型: `data-model.md`
- 查看技术选型: `research.md`
