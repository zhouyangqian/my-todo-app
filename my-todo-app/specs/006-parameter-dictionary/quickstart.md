# Quickstart Guide: Parameter Dictionary Management

**Feature**: Parameter Dictionary Management
**Branch**: `006-parameter-dictionary`
**Last Updated**: 2026-01-28

## 概述

本指南帮助开发者快速开始使用参数字典管理模块。参数字典模块用于管理系统参数和业务参数，支持分类管理、数据验证、分级访问控制和完整审计。

## 前置条件

### 必需服务

- **MySQL 8.0+** - 主数据库
- **Redis 7.0+** - 缓存
- **Spring Cloud Gateway** - API 网关
- **用户认证服务** - JWT 令牌验证
- **审计日志服务** - 操作日志记录

### 必需依赖

```xml
<!-- parameter-service/pom.xml -->
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.5</version>
    </dependency>

    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>

    <!-- JSON Schema Validation -->
    <dependency>
        <groupId>com.networknt</groupId>
        <artifactId>json-schema-validator</artifactId>
        <version>1.0.87</version>
    </dependency>
</dependencies>
```

## 配置步骤

### 1. 数据库初始化

执行 `data-model.md` 中的初始化 SQL 脚本：

```bash
mysql -u root -p < scripts/init-parameter-database.sql
```

### 2. 后端配置

```yaml
# services/parameter-service/src/main/resources/application.yml
spring:
  application:
    name: parameter-service
  datasource:
    url: jdbc:mysql://localhost:3306/parameter_db?useUnicode=true&characterEncoding=utf8
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  redis:
    host: ${REDIS_HOST}
    port: 6379
    password: ${REDIS_PASSWORD}
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_ADDR}

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      tenant-line: tenant_id
```

### 3. 网关路由配置

```yaml
# gateway/src/main/resources/application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: parameter-service
          uri: lb://parameter-service
          predicates:
            - Path=/api/v1/parameter/**
          filters:
            - StripPrefix=2
```

### 4. 前端配置

```javascript
// frontend/src/api/parameter.js
import request from '@/utils/request'

export function getDictionaries(params) {
  return request({
    url: '/api/v1/parameter/dictionaries',
    method: 'get',
    params
  })
}

export function createDictionary(data) {
  return request({
    url: '/api/v1/parameter/dictionaries',
    method: 'post',
    data
  })
}

export function getDictionaryItems(dictionaryId, params) {
  return request({
    url: `/api/v1/parameter/dictionaries/${dictionaryId}/items`,
    method: 'get',
    params
  })
}

export function createItem(dictionaryId, data) {
  return request({
    url: `/api/v1/parameter/dictionaries/${dictionaryId}/items`,
    method: 'post',
    data
  })
}

export function updateItem(id, data) {
  return request({
    url: `/api/v1/parameter/items/${id}`,
    method: 'put',
    data
  })
}
```

## 快速开始

### 场景 1：创建系统参数字典

**步骤**：
1. 创建分类（可选）
2. 创建字典
3. 添加参数项
4. 验证参数值

**示例**：

```bash
# 1. 创建分类
POST /api/v1/parameter/categories
Authorization: Bearer {token}
X-Tenant-Id: tenant123

{
  "code": "cache_config",
  "name": "缓存配置",
  "description": "Redis缓存相关配置",
  "sortOrder": 1
}

# 2. 创建系统参数字典
POST /api/v1/parameter/dictionaries
Authorization: Bearer {token}
X-Tenant-Id: tenant123

{
  "code": "redis_cache",
  "name": "Redis缓存配置",
  "description": "Redis连接和超时配置",
  "type": "SYSTEM",
  "categoryId": 1
}

# 3. 添加参数项
POST /api/v1/parameter/dictionaries/1/items
Authorization: Bearer {token}
X-Tenant-Id: tenant123

{
  "key": "timeout",
  "value": 30,
  "description": "连接超时时间（秒）",
  "dataType": "INTEGER",
  "validationRule": {
    "dataType": "INTEGER",
    "required": true,
    "minValue": 1,
    "maxValue": 300
  },
  "sortOrder": 1
}

# 4. 查询参数项
GET /api/v1/parameter/dictionaries/1/items
Authorization: Bearer {token}
X-Tenant-Id: tenant123
```

### 场景 2：查询业务参数

```bash
# 查询所有业务参数字典
GET /api/v1/parameter/dictionaries?type=BUSINESS&status=ENABLED
Authorization: Bearer {token}
X-Tenant-Id: tenant123

# 响应示例
{
  "records": [
    {
      "id": 2,
      "code": "order_status",
      "name": "订单状态",
      "type": "BUSINESS",
      "status": "ENABLED",
      "itemCount": 5
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20
}
```

### 场景 3：处理并发冲突

```bash
# 用户A和用户B同时获取参数项
GET /api/v1/parameter/items/1
# 响应: { "id": 1, "value": 30, "version": 1 }

# 用户A先提交更新
PUT /api/v1/parameter/items/1
{
  "value": 60,
  "version": 1
}
# 成功，version 变为 2

# 用户B提交更新（版本号已过时）
PUT /api/v1/parameter/items/1
{
  "value": 90,
  "version": 1
}
# 返回 409 Conflict，提示数据已被修改
```

## 权限控制

### 角色权限矩阵

| 操作 | 超级管理员 | 业务管理员 | 开发人员 |
|------|-----------|-----------|---------|
| 查看系统参数 | ✓ | ✗ | ✓ |
| 创建系统参数 | ✓ | ✗ | ✓ |
| 编辑系统参数 | ✓ | ✗ | ✓ |
| 删除系统参数 | ✓ | ✗ | ✗ |
| 查看业务参数 | ✓ | ✓ | ✗ |
| 创建业务参数 | ✓ | ✓ | ✗ |
| 编辑业务参数 | ✓ | ✓ | ✗ |
| 删除业务参数 | ✓ | ✗ | ✗ |

### 权限注解示例

```java
@RestController
@RequestMapping("/dictionaries")
public class ParameterDictionaryController {

    @GetMapping
    @PreAuthorize("hasAuthority('parameter:dictionary:query')")
    public Result<Page<Dictionary>> listDictionaries(...) {
        // 所有角色都可以查询
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('parameter:system:create', 'parameter:business:create')")
    public Result<Dictionary> createDictionary(...) {
        // 根据字典类型检查具体权限
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyAuthority('parameter:system:edit', 'parameter:business:edit')")
    public Result<Item> createItem(...) {
        // 根据字典类型检查编辑权限
    }
}
```

## 缓存使用

### 缓存键格式

```
tenant:{tenantId}:parameter:dictionary:{dictionaryId}
tenant:{tenantId}:parameter:dictionary:code:{dictionaryCode}
tenant:{tenantId}:parameter:items:{dictionaryId}
tenant:{tenantId}:parameter:categories
```

### 缓存操作示例

```java
@Service
public class ParameterDictionaryService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "tenant:%s:parameter:dictionary:%s";

    public Dictionary getDictionary(Long id) {
        String cacheKey = String.format(CACHE_KEY_PREFIX, getTenantId(), id);

        // 先从缓存获取
        Dictionary cached = (Dictionary) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，从数据库查询
        Dictionary dictionary = dictionaryMapper.selectById(id);

        // 写入缓存，1小时过期
        redisTemplate.opsForValue().set(cacheKey, dictionary, 1, TimeUnit.HOURS);

        return dictionary;
    }

    public void updateDictionary(Dictionary dictionary) {
        // 更新数据库
        dictionaryMapper.updateById(dictionary);

        // 失效缓存
        String cacheKey = String.format(CACHE_KEY_PREFIX, getTenantId(), dictionary.getId());
        redisTemplate.delete(cacheKey);
    }
}
```

## 审计日志

### 自动记录的操作

以下操作会自动记录到审计日志：
- 创建字典
- 更新字典
- 删除字典
- 创建参数项
- 更新参数项
- 删除参数项
- 启用/禁用操作

### 审计日志内容

```json
{
  "tenantId": "tenant123",
  "userId": "user456",
  "operation": "UPDATE_PARAMETER_ITEM",
  "targetType": "ParameterItem",
  "targetId": 1,
  "beforeValue": {"key": "timeout", "value": 30},
  "afterValue": {"key": "timeout", "value": 60},
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "requestContext": {
    "requestId": "req-123",
    "sessionId": "sess-456"
  },
  "createdAt": "2026-01-28T10:30:00Z"
}
```

## 前端组件使用

### 字典列表组件

```vue
<template>
  <div class="dictionary-list">
    <a-table
      :columns="columns"
      :data-source="dictionaries"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'type'">
          <a-tag :color="record.type === 'SYSTEM' ? 'red' : 'blue'">
            {{ record.type === 'SYSTEM' ? '系统参数' : '业务参数' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="handleView(record)">查看</a-button>
            <a-button type="link" @click="handleEdit(record)">编辑</a-button>
            <a-popconfirm title="确定删除吗？" @confirm="handleDelete(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDictionaries, deleteDictionary } from '@/api/parameter'

const dictionaries = ref([])
const loading = ref(false)
const pagination = ref({ current: 1, pageSize: 20, total: 0 })

const columns = [
  { title: '编码', dataIndex: 'code', key: 'code' },
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '类型', dataIndex: 'type', key: 'type' },
  { title: '参数项数', dataIndex: 'itemCount', key: 'itemCount' },
  { title: '操作', key: 'action' }
]

const loadDictionaries = async () => {
  loading.value = true
  try {
    const { data } = await getDictionaries({
      page: pagination.value.current,
      size: pagination.value.size
    })
    dictionaries.value = data.records
    pagination.value.total = data.total
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDictionaries()
})
</script>
```

## 故障排除

### 常见问题

**问题 1**：租户隔离失效
```
原因：MyBatis-Plus 租户插件未正确配置
解决：检查 MyBatisPlusConfig 配置，确保 tenantLineHandler 已设置
```

**问题 2**：缓存数据不一致
```
原因：参数更新后未失效缓存
解决：确保所有更新操作都调用了缓存失效方法
```

**问题 3**：并发更新冲突
```
原因：乐观锁版本号未正确传递
解决：确保更新请求包含当前 version 字段
```

**问题 4**：权限检查失败
```
原因：用户角色与操作类型不匹配
解决：检查用户角色是否具有执行该操作的权限
```

## 下一步

- 查看完整 API 文档：`contracts/parameter-dictionary-api.yaml`
- 了解数据模型：`data-model.md`
- 阅读技术决策：`research.md`
- 查看实施任务：`tasks.md` (由 `/speckit.tasks` 生成)
