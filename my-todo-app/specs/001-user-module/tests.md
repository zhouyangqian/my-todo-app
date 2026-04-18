# Tests: 用户模块

**Feature Branch**: `001-user-module`
**Generated**: 2026-04-07

## 测试策略概览

| 层级 | 框架 | 覆盖率目标 |
|------|------|------------|
| 单元测试 | JUnit 5 + Mockito | ≥ 80% |
| 集成测试 | Spring Boot Test + Testcontainers | ≥ 70% |
| API测试 | MockMvc | 100% 端点 |
| 前端测试 | Vitest + Vue Test Utils | ≥ 75% |

---

## 单元测试

### Service 层

#### UserServiceTest
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("创建用户 - 成功")
    void createUser_success() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");

        when(userMapper.selectByUsername("testuser")).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // When
        Long userId = userService.createUser(request);

        // Then
        assertNotNull(userId);
        verify(auditLogService).log(any());
    }

    @Test
    @DisplayName("创建用户 - 用户名已存在")
    void createUser_duplicateUsername() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("existing");

        when(userMapper.selectByUsername("existing"))
            .thenReturn(new User());

        // When & Then
        assertThrows(BusinessException.class,
            () -> userService.createUser(request));
    }

    @Test
    @DisplayName("更新用户 - 租户隔离验证")
    void updateUser_tenantIsolation() {
        // Given
        Long userId = 1L;
        Long tenantId = 100L;
        UserUpdateRequest request = new UserUpdateRequest();

        User user = new User();
        user.setId(userId);
        user.setTenantId(200L); // 不同租户

        when(userMapper.selectById(userId)).thenReturn(user);

        // When & Then
        assertThrows(TenantIsolationException.class,
            () -> userService.updateUser(userId, request, tenantId));
    }
}
```

#### RoleServiceTest
```java
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private UserRoleMapper userRoleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    @DisplayName("分配角色 - 批量分配")
    void assignRoles_batch() {
        // Given
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        List<Long> roleIds = Arrays.asList(10L, 20L);

        // When
        roleService.assignRoles(userIds, roleIds);

        // Then
        verify(userRoleMapper, times(6)).insert(any(UserRole.class));
    }

    @Test
    @DisplayName("删除角色 - 检查用户关联")
    void deleteRole_hasUsers() {
        // Given
        Long roleId = 1L;
        when(userRoleMapper.countByRoleId(roleId)).thenReturn(5L);

        // When & Then
        assertThrows(BusinessException.class,
            () -> roleService.deleteRole(roleId));
    }
}
```

### Mapper 层

#### UserMapperTest
```java
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("根据租户分页查询用户")
    void selectByTenant_page() {
        // Given
        Long tenantId = 1L;
        Page<User> page = new Page<>(1, 10);

        // When
        IPage<User> result = userMapper.selectByTenant(page, tenantId, null);

        // Then
        assertNotNull(result.getRecords());
    }

    @Test
    @DisplayName("租户插件自动过滤")
    void tenantFilter_automatically() {
        // When
        List<User> users = userMapper.selectList(null);

        // Then - 只返回当前租户数据
        users.forEach(u -> assertEquals(1L, u.getTenantId()));
    }
}
```

---

## 集成测试

### UserControllerIntegrationTest
```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("test_db");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.0")
        .withExposedPorts(6379);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TokenService tokenService;

    @BeforeEach
    void setup() {
        when(tokenService.getCurrentTenantId()).thenReturn(1L);
        when(tokenService.getCurrentUserId()).thenReturn(1L);
    }

    @Test
    @DisplayName("创建用户 API - 完整流程")
    void createUserApi_fullFlow() throws Exception {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPhone("13800138000");

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.username").value("newuser"));
    }

    @Test
    @DisplayName("查询用户列表 - 分页")
    void listUsers_pagination() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").isNumber())
            .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("更新用户 - 权限校验")
    void updateUser_unauthorized() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setNickname("New Name");

        mockMvc.perform(put("/api/v1/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }
}
```

### BatchOperationIntegrationTest
```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class BatchOperationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("批量禁用用户")
    void batchDisable_users() throws Exception {
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);

        mockMvc.perform(post("/api/v1/users/batch/disable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userIds)))
            .andExpect(status().isOk());

        // 验证数据库状态
        userIds.forEach(id -> {
            User user = userRepository.findById(id);
            assertEquals(UserStatus.DISABLED, user.getStatus());
        });
    }
}
```

---

## API 测试

### OpenAPI Contract 测试

```yaml
# tests/api/user-api-test.yaml
openapi: 3.0.3
info:
  title: User API Tests
  version: 1.0.0

tests:
  - name: 创建用户
    request:
      method: POST
      path: /api/v1/users
      body:
        username: testuser
        email: test@example.com
    expect:
      status: 200
      body:
        code: 200
        data.username: testuser

  - name: 获取用户列表
    request:
      method: GET
      path: /api/v1/users
      params:
        page: 1
        size: 10
    expect:
      status: 200
      body:
        code: 200
        data.records: "@isArray"

  - name: 更新用户
    request:
      method: PUT
      path: /api/v1/users/{id}
      pathParams:
        id: 1
      body:
        nickname: Updated Name
    expect:
      status: 200

  - name: 删除用户
    request:
      method: DELETE
      path: /api/v1/users/{id}
      pathParams:
        id: 1
    expect:
      status: 200
```

---

## 前端测试

### UserList.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import UserList from '@/views/user/UserList.vue'
import * as userApi from '@/api/user'

vi.mock('@/api/user')

describe('UserList', () => {
  it('渲染用户列表', async () => {
    vi.mocked(userApi.getUsers).mockResolvedValue({
      data: {
        records: [
          { id: 1, username: 'user1', email: 'user1@test.com' },
          { id: 2, username: 'user2', email: 'user2@test.com' }
        ],
        total: 2
      }
    })

    const wrapper = mount(UserList)
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('.user-row')).toHaveLength(2)
  })

  it('搜索用户', async () => {
    const wrapper = mount(UserList)

    await wrapper.find('.search-input').setValue('test')
    await wrapper.find('.search-btn').trigger('click')

    expect(userApi.getUsers).toHaveBeenCalledWith(
      expect.objectContaining({ keyword: 'test' })
    )
  })

  it('批量选择用户', async () => {
    const wrapper = mount(UserList)
    await wrapper.setData({
      users: [
        { id: 1, username: 'user1' },
        { id: 2, username: 'user2' }
      ]
    })

    await wrapper.findAll('.checkbox')[0].setChecked(true)
    await wrapper.findAll('.checkbox')[1].setChecked(true)

    expect(wrapper.vm.selectedUsers).toHaveLength(2)
  })
})
```

### UserForm.spec.ts
```typescript
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import UserForm from '@/views/user/UserForm.vue'

describe('UserForm', () => {
  it('表单验证 - 必填字段', async () => {
    const wrapper = mount(UserForm)

    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.find('.username-error').exists()).toBe(true)
    expect(wrapper.find('.email-error').exists()).toBe(true)
  })

  it('表单验证 - 邮箱格式', async () => {
    const wrapper = mount(UserForm)

    await wrapper.find('input[name="email"]').setValue('invalid-email')
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.find('.email-error').text()).toContain('格式不正确')
  })

  it('提交表单成功', async () => {
    const wrapper = mount(UserForm, {
      props: { visible: true }
    })

    await wrapper.find('input[name="username"]').setValue('newuser')
    await wrapper.find('input[name="email"]').setValue('new@test.com')
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.emitted('success')).toBeTruthy()
  })
})
```

---

## 性能测试

### JMeter 测试计划

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="User Module Load Test">
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments">
        <collectionProp name="Arguments.arguments">
          <elementProp name="BASE_URL" elementType="Argument">
            <stringProp name="Argument.name">BASE_URL</stringProp>
            <stringProp name="Argument.value">http://localhost:8080</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="User List">
        <stringProp name="ThreadGroup.num_threads">100</stringProp>
        <stringProp name="ThreadGroup.ramp_time">10</stringProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
        <stringProp name="ThreadGroup.duration">60</stringProp>
      </ThreadGroup>
      <hashTree>
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="Get Users">
          <stringProp name="HTTPSampler.domain">${BASE_URL}</stringProp>
          <stringProp name="HTTPSampler.path">/api/v1/users</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
        </HTTPSamplerProxy>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

### 性能指标

| 接口 | 并发数 | 平均响应时间 | 吞吐量 | 错误率 |
|------|--------|--------------|--------|--------|
| GET /users | 100 | < 200ms | > 500/s | < 0.1% |
| POST /users | 50 | < 300ms | > 200/s | < 0.1% |
| PUT /users/{id} | 50 | < 250ms | > 200/s | < 0.1% |
| DELETE /users/{id} | 50 | < 200ms | > 300/s | < 0.1% |

---

## 测试覆盖率

### JaCoCo 配置

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 覆盖率目标

| 类别 | 行覆盖率 | 分支覆盖率 |
|------|----------|------------|
| Service | ≥ 85% | ≥ 80% |
| Controller | ≥ 80% | ≥ 75% |
| Mapper | ≥ 70% | ≥ 65% |
| Utils | ≥ 90% | ≥ 85% |
| **总体** | **≥ 80%** | **≥ 75%** |

---

## CI/CD 集成

### GitHub Actions

```yaml
name: Test

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: test_db
        ports:
          - 3306:3306

      redis:
        image: redis:7.0
        ports:
          - 6379:6379

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run Tests
        run: mvn test -Dspring.profiles.active=test

      - name: Generate Coverage Report
        run: mvn jacoco:report

      - name: Upload Coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./target/site/jacoco/jacoco.xml

  frontend-test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Install Dependencies
        run: cd frontend && npm ci

      - name: Run Tests
        run: cd frontend && npm run test:coverage

      - name: Upload Coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./frontend/coverage/lcov.info
```

---

## 测试命令

```bash
# 后端测试
mvn test                          # 运行所有单元测试
mvn verify                        # 运行所有测试（含集成测试）
mvn test -Dtest=UserServiceTest   # 运行单个测试类
mvn jacoco:report                 # 生成覆盖率报告

# 前端测试
cd frontend
npm run test                      # 运行测试
npm run test:coverage             # 带覆盖率
npm run test:watch                # 监听模式
```

---

## 测试清单

- [ ] 单元测试 - UserService
- [ ] 单元测试 - RoleService
- [ ] 单元测试 - BatchOperationService
- [ ] 单元测试 - UserImportService
- [ ] 集成测试 - UserController
- [ ] 集成测试 - RoleController
- [ ] 集成测试 - BatchOperationController
- [ ] API 契约测试
- [ ] 前端组件测试 - UserList
- [ ] 前端组件测试 - UserForm
- [ ] 前端组件测试 - RoleManage
- [ ] 性能测试 - 用户列表接口
- [ ] 性能测试 - 用户创建接口
- [ ] 安全测试 - 租户隔离
- [ ] 安全测试 - 权限校验
