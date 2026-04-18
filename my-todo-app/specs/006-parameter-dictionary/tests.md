# Tests: 参数字典模块

**Feature Branch**: `006-parameter-dictionary`
**Generated**: 2026-04-07

## 测试策略概览

| 层级 | 框架 | 覆盖率目标 |
|------|------|------------|
| 单元测试 | JUnit 5 + Mockito | ≥ 85% |
| 集成测试 | Spring Boot Test + Testcontainers | ≥ 75% |
| API测试 | MockMvc | 100% 端点 |
| 前端测试 | Vitest + Vue Test Utils | ≥ 75% |

---

## 单元测试

### ParameterDictionaryServiceTest
```java
@ExtendWith(MockitoExtension.class)
class ParameterDictionaryServiceTest {

    @Mock
    private ParameterDictionaryMapper dictionaryMapper;

    @Mock
    private ParameterItemMapper itemMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ParameterDictionaryServiceImpl dictionaryService;

    @Test
    @DisplayName("创建参数字典 - 系统参数")
    void createDictionary_systemParam() {
        // Given
        ParameterDictionaryCreateRequest request = new ParameterDictionaryCreateRequest();
        request.setCode("SYS_TIMEZONE");
        request.setName("系统时区");
        request.setType(DictionaryType.SYSTEM);
        request.setDescription("系统默认时区配置");

        when(dictionaryMapper.selectByCode("SYS_TIMEZONE")).thenReturn(null);

        // When
        Long dictionaryId = dictionaryService.createDictionary(request);

        // Then
        assertNotNull(dictionaryId);
        verify(dictionaryMapper).insert(argThat(dict ->
            dict.getCode().equals("SYS_TIMEZONE") &&
            dict.getType() == DictionaryType.SYSTEM &&
            dict.getStatus() == Status.ENABLED
        ));
    }

    @Test
    @DisplayName("创建参数字典 - 业务参数")
    void createDictionary_businessParam() {
        // Given
        ParameterDictionaryCreateRequest request = new ParameterDictionaryCreateRequest();
        request.setCode("BIZ_ORDER_STATUS");
        request.setName("订单状态");
        request.setType(DictionaryType.BUSINESS);

        when(dictionaryMapper.selectByCode("BIZ_ORDER_STATUS")).thenReturn(null);

        // When
        Long dictionaryId = dictionaryService.createDictionary(request);

        // Then
        assertNotNull(dictionaryId);
        verify(dictionaryMapper).insert(argThat(dict ->
            dict.getType() == DictionaryType.BUSINESS
        ));
    }

    @Test
    @DisplayName("创建参数字典 - 编码重复")
    void createDictionary_duplicateCode() {
        // Given
        ParameterDictionaryCreateRequest request = new ParameterDictionaryCreateRequest();
        request.setCode("SYS_TIMEZONE");

        when(dictionaryMapper.selectByCode("SYS_TIMEZONE"))
            .thenReturn(new ParameterDictionary());

        // When & Then
        assertThrows(DuplicateCodeException.class,
            () -> dictionaryService.createDictionary(request));
    }

    @Test
    @DisplayName("创建参数字典 - 编码格式验证")
    void createDictionary_invalidCodeFormat() {
        // Given
        ParameterDictionaryCreateRequest request = new ParameterDictionaryCreateRequest();
        request.setCode("1_INVALID_CODE"); // 以数字开头

        // When & Then
        assertThrows(ValidationException.class,
            () -> dictionaryService.createDictionary(request));
    }

    @Test
    @DisplayName("更新参数字典")
    void updateDictionary() {
        // Given
        Long dictionaryId = 1L;
        ParameterDictionaryUpdateRequest request = new ParameterDictionaryUpdateRequest();
        request.setName("更新后的名称");
        request.setDescription("更新后的描述");

        ParameterDictionary dictionary = new ParameterDictionary();
        dictionary.setId(dictionaryId);
        dictionary.setCode("SYS_TIMEZONE");

        when(dictionaryMapper.selectById(dictionaryId)).thenReturn(dictionary);

        // When
        dictionaryService.updateDictionary(dictionaryId, request);

        // Then
        verify(dictionaryMapper).updateById(argThat(dict ->
            dict.getName().equals("更新后的名称")
        ));
        verify(auditLogService).logUpdate(any());
    }

    @Test
    @DisplayName("删除参数字典 - 逻辑删除")
    void deleteDictionary_logicalDelete() {
        // Given
        Long dictionaryId = 1L;

        ParameterDictionary dictionary = new ParameterDictionary();
        dictionary.setId(dictionaryId);

        when(dictionaryMapper.selectById(dictionaryId)).thenReturn(dictionary);

        // When
        dictionaryService.deleteDictionary(dictionaryId);

        // Then
        verify(dictionaryMapper).updateById(argThat(dict ->
            dict.getDeleted() == true
        ));
    }
}
```

### ParameterItemServiceTest
```java
@ExtendWith(MockitoExtension.class)
class ParameterItemServiceTest {

    @Mock
    private ParameterItemMapper itemMapper;

    @Mock
    private ParameterDictionaryMapper dictionaryMapper;

    @Mock
    private ValidationRuleMapper ruleMapper;

    @InjectMocks
    private ParameterItemServiceImpl itemService;

    @Test
    @DisplayName("添加参数项 - 成功")
    void addItem_success() {
        // Given
        Long dictionaryId = 1L;
        ParameterItemCreateRequest request = new ParameterItemCreateRequest();
        request.setKey("DEFAULT_TIMEZONE");
        request.setValue("Asia/Shanghai");
        request.setDescription("默认时区");
        request.setSortOrder(1);

        ParameterDictionary dictionary = new ParameterDictionary();
        dictionary.setId(dictionaryId);
        dictionary.setType(DictionaryType.SYSTEM);

        when(dictionaryMapper.selectById(dictionaryId)).thenReturn(dictionary);
        when(itemMapper.selectByDictionaryIdAndKey(dictionaryId, "DEFAULT_TIMEZONE"))
            .thenReturn(null);

        // When
        Long itemId = itemService.addItem(dictionaryId, request);

        // Then
        assertNotNull(itemId);
        verify(itemMapper).insert(any(ParameterItem.class));
    }

    @Test
    @DisplayName("添加参数项 - Key重复")
    void addItem_duplicateKey() {
        // Given
        Long dictionaryId = 1L;
        ParameterItemCreateRequest request = new ParameterItemCreateRequest();
        request.setKey("EXISTING_KEY");

        when(itemMapper.selectByDictionaryIdAndKey(dictionaryId, "EXISTING_KEY"))
            .thenReturn(new ParameterItem());

        // When & Then
        assertThrows(DuplicateKeyException.class,
            () -> itemService.addItem(dictionaryId, request));
    }

    @Test
    @DisplayName("更新参数项值")
    void updateItemValue() {
        // Given
        Long itemId = 1L;
        String newValue = "America/New_York";

        ParameterItem item = new ParameterItem();
        item.setId(itemId);
        item.setValue("Asia/Shanghai");

        when(itemMapper.selectById(itemId)).thenReturn(item);

        // When
        itemService.updateItemValue(itemId, newValue);

        // Then
        verify(itemMapper).updateById(argThat(i ->
            i.getValue().equals(newValue)
        ));
    }

    @Test
    @DisplayName("启用/禁用参数项")
    void toggleItemStatus() {
        // Given
        Long itemId = 1L;

        ParameterItem item = new ParameterItem();
        item.setId(itemId);
        item.setStatus(Status.ENABLED);

        when(itemMapper.selectById(itemId)).thenReturn(item);

        // When
        itemService.toggleStatus(itemId);

        // Then
        verify(itemMapper).updateById(argThat(i ->
            i.getStatus() == Status.DISABLED
        ));
    }

    @Test
    @DisplayName("批量更新参数项")
    void batchUpdateItems() {
        // Given
        Long dictionaryId = 1L;
        List<ParameterItemUpdateRequest> requests = List.of(
            new ParameterItemUpdateRequest(1L, "value1"),
            new ParameterItemUpdateRequest(2L, "value2"),
            new ParameterItemUpdateRequest(3L, "value3")
        );

        // When
        itemService.batchUpdateItems(dictionaryId, requests);

        // Then
        verify(itemMapper, times(3)).updateById(any());
    }
}
```

### ParameterCategoryServiceTest
```java
@ExtendWith(MockitoExtension.class)
class ParameterCategoryServiceTest {

    @Mock
    private ParameterCategoryMapper categoryMapper;

    @Mock
    private ParameterDictionaryMapper dictionaryMapper;

    @InjectMocks
    private ParameterCategoryServiceImpl categoryService;

    @Test
    @DisplayName("创建参数分类")
    void createCategory() {
        // Given
        ParameterCategoryCreateRequest request = new ParameterCategoryCreateRequest();
        request.setCode("SYS_CONFIG");
        request.setName("系统配置");
        request.setSortOrder(1);

        when(categoryMapper.selectByCode("SYS_CONFIG")).thenReturn(null);

        // When
        Long categoryId = categoryService.createCategory(request);

        // Then
        assertNotNull(categoryId);
        verify(categoryMapper).insert(any(ParameterCategory.class));
    }

    @Test
    @DisplayName("删除分类 - 无关联字典")
    void deleteCategory_noDictionaries() {
        // Given
        Long categoryId = 1L;

        when(dictionaryMapper.countByCategoryId(categoryId)).thenReturn(0L);

        // When
        categoryService.deleteCategory(categoryId);

        // Then
        verify(categoryMapper).updateById(argThat(cat ->
            cat.getDeleted() == true
        ));
    }

    @Test
    @DisplayName("删除分类 - 有关联字典时警告")
    void deleteCategory_hasDictionaries() {
        // Given
        Long categoryId = 1L;

        when(dictionaryMapper.countByCategoryId(categoryId)).thenReturn(5L);

        // When & Then
        assertThrows(CategoryHasDictionariesException.class,
            () -> categoryService.deleteCategory(categoryId));
    }
}
```

### ValidationServiceTest
```java
@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @InjectMocks
    private ValidationServiceImpl validationService;

    @Test
    @DisplayName("验证参数值 - 字符串类型")
    void validateValue_string() {
        // Given
        ValidationRule rule = new ValidationRule();
        rule.setDataType(DataType.STRING);
        rule.setMaxLength(50);

        // When
        ValidationResult result = validationService.validate("test value", rule);

        // Then
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("验证参数值 - 超出最大长度")
    void validateValue_exceedMaxLength() {
        // Given
        ValidationRule rule = new ValidationRule();
        rule.setDataType(DataType.STRING);
        rule.setMaxLength(10);

        // When
        ValidationResult result = validationService.validate("this is a very long string", rule);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("长度超出限制"));
    }

    @Test
    @DisplayName("验证参数值 - 整数类型")
    void validateValue_integer() {
        // Given
        ValidationRule rule = new ValidationRule();
        rule.setDataType(DataType.INTEGER);
        rule.setMinValue(0);
        rule.setMaxValue(100);

        // When
        ValidationResult result = validationService.validate("50", rule);

        // Then
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("验证参数值 - 整数范围无效")
    void validateValue_integerOutOfRange() {
        // Given
        ValidationRule rule = new ValidationRule();
        rule.setDataType(DataType.INTEGER);
        rule.setMinValue(0);
        rule.setMaxValue(100);

        // When
        ValidationResult result = validationService.validate("150", rule);

        // Then
        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("验证参数值 - 正则表达式")
    void validateValue_regex() {
        // Given
        ValidationRule rule = new ValidationRule();
        rule.setDataType(DataType.STRING);
        rule.setRegexPattern("^[A-Z]{2,4}$");

        // When
        ValidationResult validResult = validationService.validate("ABC", rule);
        ValidationResult invalidResult = validationService.validate("abc", rule);

        // Then
        assertTrue(validResult.isValid());
        assertFalse(invalidResult.isValid());
    }

    @Test
    @DisplayName("验证参数值 - JSON对象")
    void validateValue_jsonObject() {
        // Given
        ValidationRule rule = new ValidationRule();
        rule.setDataType(DataType.JSON);

        String validJson = "{\"name\": \"test\", \"value\": 123}";
        String invalidJson = "{invalid json}";

        // When
        ValidationResult validResult = validationService.validate(validJson, rule);
        ValidationResult invalidResult = validationService.validate(invalidJson, rule);

        // Then
        assertTrue(validResult.isValid());
        assertFalse(invalidResult.isValid());
    }

    @Test
    @DisplayName("验证参数值 - 枚举类型")
    void validateValue_enum() {
        // Given
        ValidationRule rule = new ValidationRule();
        rule.setDataType(DataType.ENUM);
        rule.setEnumValues("ACTIVE,INACTIVE,PENDING");

        // When
        ValidationResult validResult = validationService.validate("ACTIVE", rule);
        ValidationResult invalidResult = validationService.validate("UNKNOWN", rule);

        // Then
        assertTrue(validResult.isValid());
        assertFalse(invalidResult.isValid());
    }
}
```

### AccessControlServiceTest
```java
@ExtendWith(MockitoExtension.class)
class AccessControlServiceTest {

    @Mock
    private UserRoleMapper userRoleMapper;

    @InjectMocks
    private AccessControlServiceImpl accessControlService;

    @Test
    @DisplayName("权限检查 - 超级管理员访问系统参数")
    void checkAccess_superAdminSystemParam() {
        // Given
        Long userId = 1L;
        when(userRoleMapper.getRoles(userId))
            .thenReturn(List.of(Role.SUPER_ADMIN));

        // When
        boolean hasAccess = accessControlService.canManageSystemParams(userId);

        // Then
        assertTrue(hasAccess);
    }

    @Test
    @DisplayName("权限检查 - 业务管理员访问系统参数")
    void checkAccess_businessAdminSystemParam() {
        // Given
        Long userId = 2L;
        when(userRoleMapper.getRoles(userId))
            .thenReturn(List.of(Role.BUSINESS_ADMIN));

        // When
        boolean hasAccess = accessControlService.canManageSystemParams(userId);

        // Then
        assertFalse(hasAccess);
    }

    @Test
    @DisplayName("权限检查 - 业务管理员访问业务参数")
    void checkAccess_businessAdminBusinessParam() {
        // Given
        Long userId = 2L;
        when(userRoleMapper.getRoles(userId))
            .thenReturn(List.of(Role.BUSINESS_ADMIN));

        // When
        boolean hasAccess = accessControlService.canManageBusinessParams(userId);

        // Then
        assertTrue(hasAccess);
    }

    @Test
    @DisplayName("权限检查 - 开发人员设置系统参数")
    void checkAccess_developerSystemParam() {
        // Given
        Long userId = 3L;
        when(userRoleMapper.getRoles(userId))
            .thenReturn(List.of(Role.DEVELOPER));

        // When
        boolean hasAccess = accessControlService.canManageSystemParams(userId);

        // Then
        assertTrue(hasAccess);
    }
}
```

### AuditLogServiceTest
```java
@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @Test
    @DisplayName("记录创建操作日志")
    void logCreate() {
        // Given
        Long userId = 1L;
        String operation = "CREATE_DICTIONARY";
        ParameterDictionary dictionary = new ParameterDictionary();
        dictionary.setCode("TEST_CODE");

        // When
        auditLogService.logCreate(userId, operation, dictionary, "192.168.1.1");

        // Then
        verify(auditLogMapper).insert(argThat(log ->
            log.getOperationType() == OperationType.CREATE &&
            log.getNewValue() != null &&
            log.getOldValue() == null
        ));
    }

    @Test
    @DisplayName("记录更新操作日志 - 值对比")
    void logUpdate_withDiff() {
        // Given
        ParameterDictionary oldValue = new ParameterDictionary();
        oldValue.setName("旧名称");

        ParameterDictionary newValue = new ParameterDictionary();
        newValue.setName("新名称");

        // When
        auditLogService.logUpdate(1L, "UPDATE_DICTIONARY", oldValue, newValue, "192.168.1.1");

        // Then
        verify(auditLogMapper).insert(argThat(log ->
            log.getOperationType() == OperationType.UPDATE &&
            log.getOldValue().contains("旧名称") &&
            log.getNewValue().contains("新名称")
        ));
    }

    @Test
    @DisplayName("记录批量操作日志 - 逐条记录")
    void logBatchOperation() {
        // Given
        List<ParameterItem> items = List.of(
            new ParameterItem(1L, "key1", "value1"),
            new ParameterItem(2L, "key2", "value2"),
            new ParameterItem(3L, "key3", "value3")
        );

        // When
        auditLogService.logBatchCreate(1L, "BATCH_CREATE_ITEMS", items, "192.168.1.1");

        // Then
        verify(auditLogMapper, times(3)).insert(any(AuditLog.class));
    }
}
```

---

## 集成测试

### ParameterDictionaryIntegrationTest
```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ParameterDictionaryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.0");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParameterDictionaryMapper dictionaryMapper;

    @Autowired
    private ParameterItemMapper itemMapper;

    @BeforeEach
    void setup() {
        dictionaryMapper.delete(null);
        itemMapper.delete(null);
    }

    @Test
    @DisplayName("参数字典完整生命周期")
    void dictionaryLifecycle() throws Exception {
        // 1. 创建字典
        String createBody = """
            {
                "code": "BIZ_STATUS",
                "name": "业务状态",
                "type": "BUSINESS",
                "description": "通用业务状态字典"
            }
            """;

        String response = mockMvc.perform(post("/api/v1/parameters/dictionaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").exists())
            .andReturn().getResponse().getContentAsString();

        Long dictionaryId = JsonPath.parse(response).read("$.data.id", Long.class);

        // 2. 添加参数项
        String itemBody = """
            {
                "key": "ACTIVE",
                "value": "激活",
                "description": "激活状态",
                "sortOrder": 1
            }
            """;

        mockMvc.perform(post("/api/v1/parameters/dictionaries/{id}/items", dictionaryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(itemBody))
            .andExpect(status().isOk());

        // 3. 查询字典和参数项
        mockMvc.perform(get("/api/v1/parameters/dictionaries/{id}", dictionaryId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.code").value("BIZ_STATUS"))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.items[0].key").value("ACTIVE"));

        // 4. 更新字典
        mockMvc.perform(put("/api/v1/parameters/dictionaries/{id}", dictionaryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"业务状态字典\"}"))
            .andExpect(status().isOk());

        // 5. 删除字典
        mockMvc.perform(delete("/api/v1/parameters/dictionaries/{id}", dictionaryId))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("按分类查询字典")
    void queryByCategory() throws Exception {
        // 准备数据
        createDictionaryWithCategory("DICT1", 1L);
        createDictionaryWithCategory("DICT2", 1L);
        createDictionaryWithCategory("DICT3", 2L);

        // 按分类查询
        mockMvc.perform(get("/api/v1/parameters/dictionaries")
                .param("categoryId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    @DisplayName("并发编辑冲突检测")
    void concurrentEditConflict() throws Exception {
        // 创建字典
        Long dictionaryId = createDictionary("TEST_DICT");

        // 获取字典（版本1）
        String response1 = mockMvc.perform(get("/api/v1/parameters/dictionaries/{id}", dictionaryId))
            .andReturn().getResponse().getContentAsString();
        Integer version = JsonPath.parse(response1).read("$.data.version");

        // 用户A更新（成功）
        mockMvc.perform(put("/api/v1/parameters/dictionaries/{id}", dictionaryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"用户A修改\", \"version\": " + version + "}"))
            .andExpect(status().isOk());

        // 用户B更新（冲突）
        mockMvc.perform(put("/api/v1/parameters/dictionaries/{id}", dictionaryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"用户B修改\", \"version\": " + version + "}"))
            .andExpect(status().isConflict());
    }
}
```

### ParameterItemIntegrationTest
```java
@SpringBootTest
@AutoConfigureMockMvc
class ParameterItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("参数项验证规则")
    void parameterItemValidation() throws Exception {
        Long dictionaryId = createDictionaryWithValidation("INT_PARAM", DataType.INTEGER, 0, 100);

        // 有效值
        mockMvc.perform(post("/api/v1/parameters/dictionaries/{id}/items", dictionaryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"key\": \"KEY1\", \"value\": \"50\"}"))
            .andExpect(status().isOk());

        // 无效值 - 超出范围
        mockMvc.perform(post("/api/v1/parameters/dictionaries/{id}/items", dictionaryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"key\": \"KEY2\", \"value\": \"150\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("批量更新参数项")
    void batchUpdateItems() throws Exception {
        Long dictionaryId = createDictionary("BATCH_TEST");
        createItems(dictionaryId, "KEY1", "KEY2", "KEY3");

        String body = """
            {
                "items": [
                    {"id": 1, "value": "new_value1"},
                    {"id": 2, "value": "new_value2"},
                    {"id": 3, "value": "new_value3"}
                ]
            }
            """;

        mockMvc.perform(put("/api/v1/parameters/dictionaries/{id}/items/batch", dictionaryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());

        // 验证所有项已更新
        mockMvc.perform(get("/api/v1/parameters/dictionaries/{id}", dictionaryId))
            .andExpect(jsonPath("$.data.items[?(@.key=='KEY1')].value").value("new_value1"));
    }
}
```

---

## 前端测试

### DictionaryList.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DictionaryList from '@/views/parameter/DictionaryList.vue'
import * as dictApi from '@/api/dictionary'

vi.mock('@/api/dictionary')

describe('DictionaryList', () => {
  it('渲染字典列表', async () => {
    vi.mocked(dictApi.getDictionaries).mockResolvedValue({
      data: {
        records: [
          { id: 1, code: 'SYS_TIMEZONE', name: '系统时区', type: 'SYSTEM' },
          { id: 2, code: 'BIZ_STATUS', name: '业务状态', type: 'BUSINESS' }
        ],
        total: 2
      }
    })

    const wrapper = mount(DictionaryList)
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('.dict-row')).toHaveLength(2)
  })

  it('按类型筛选', async () => {
    const wrapper = mount(DictionaryList)

    await wrapper.find('.type-filter').setValue('SYSTEM')
    await wrapper.vm.loadDictionaries()

    expect(dictApi.getDictionaries).toHaveBeenCalledWith(
      expect.objectContaining({ type: 'SYSTEM' })
    )
  })

  it('搜索字典', async () => {
    const wrapper = mount(DictionaryList)

    await wrapper.find('.search-input').setValue('时区')
    await wrapper.find('.search-btn').trigger('click')

    expect(dictApi.getDictionaries).toHaveBeenCalledWith(
      expect.objectContaining({ keyword: '时区' })
    )
  })

  it('创建新字典', async () => {
    vi.mocked(dictApi.createDictionary).mockResolvedValue({ data: { id: 1 } })

    const wrapper = mount(DictionaryList)

    await wrapper.find('.create-btn').trigger('click')
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.dict-form-dialog').isVisible()).toBe(true)
  })
})
```

### DictionaryForm.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DictionaryForm from '@/views/parameter/DictionaryForm.vue'

describe('DictionaryForm', () => {
  it('验证编码格式', async () => {
    const wrapper = mount(DictionaryForm)

    // 无效编码 - 以数字开头
    await wrapper.find('input[name="code"]').setValue('1_INVALID')
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.find('.code-error').exists()).toBe(true)
  })

  it('验证编码长度', async () => {
    const wrapper = mount(DictionaryForm)

    // 太短的编码
    await wrapper.find('input[name="code"]').setValue('A')
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.find('.code-error').text()).toContain('2-50')
  })

  it('提交表单成功', async () => {
    const wrapper = mount(DictionaryForm, {
      props: { visible: true }
    })

    await wrapper.find('input[name="code"]').setValue('VALID_CODE')
    await wrapper.find('input[name="name"]').setValue('有效名称')
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.emitted('success')).toBeTruthy()
  })
})
```

### ParameterItemList.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ParameterItemList from '@/views/parameter/ParameterItemList.vue'
import * as itemApi from '@/api/parameterItem'

vi.mock('@/api/parameterItem')

describe('ParameterItemList', () => {
  it('渲染参数项列表', async () => {
    vi.mocked(itemApi.getItems).mockResolvedValue({
      data: [
        { id: 1, key: 'KEY1', value: 'value1', status: 'ENABLED' },
        { id: 2, key: 'KEY2', value: 'value2', status: 'DISABLED' }
      ]
    })

    const wrapper = mount(ParameterItemList, {
      props: { dictionaryId: 1 }
    })
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('.item-row')).toHaveLength(2)
  })

  it('切换参数项状态', async () => {
    vi.mocked(itemApi.toggleStatus).mockResolvedValue({ data: {} })

    const wrapper = mount(ParameterItemList, {
      props: { dictionaryId: 1 },
      data() {
        return {
          items: [{ id: 1, key: 'KEY1', status: 'ENABLED' }]
        }
      }
    })

    await wrapper.find('.toggle-btn').trigger('click')

    expect(itemApi.toggleStatus).toHaveBeenCalledWith(1)
  })

  it('批量删除确认', async () => {
    const wrapper = mount(ParameterItemList, {
      props: { dictionaryId: 1 },
      data() {
        return {
          items: [
            { id: 1, key: 'KEY1', selected: true },
            { id: 2, key: 'KEY2', selected: true }
          ]
        }
      }
    })

    await wrapper.find('.batch-delete-btn').trigger('click')

    expect(wrapper.find('.confirm-dialog').isVisible()).toBe(true)
    expect(wrapper.vm.selectedCount).toBe(2)
  })
})
```

### CategoryTree.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import CategoryTree from '@/views/parameter/CategoryTree.vue'
import * as categoryApi from '@/api/category'

vi.mock('@/api/category')

describe('CategoryTree', () => {
  it('渲染分类树', async () => {
    vi.mocked(categoryApi.getCategories).mockResolvedValue({
      data: [
        { id: 1, code: 'SYS', name: '系统配置', children: [
          { id: 2, code: 'SYS_TIME', name: '时间配置' }
        ]}
      ]
    })

    const wrapper = mount(CategoryTree)
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('.category-node')).toHaveLength(2)
  })

  it('选择分类触发事件', async () => {
    const wrapper = mount(CategoryTree, {
      data() {
        return {
          categories: [{ id: 1, code: 'SYS', name: '系统配置' }]
        }
      }
    })

    await wrapper.find('.category-node').trigger('click')

    expect(wrapper.emitted('select')).toBeTruthy()
    expect(wrapper.emitted('select')[0]).toEqual([1])
  })

  it('删除分类 - 有字典关联时提示', async () => {
    vi.mocked(categoryApi.hasDictionaries).mockResolvedValue({ data: true })

    const wrapper = mount(CategoryTree, {
      data() {
        return {
          categories: [{ id: 1, code: 'SYS', name: '系统配置' }]
        }
      }
    })

    await wrapper.find('.delete-btn').trigger('click')

    expect(wrapper.find('.warning-dialog').isVisible()).toBe(true)
  })
})
```

---

## 并发测试

### ConcurrentEditTest
```java
@SpringBootTest
class ConcurrentEditTest {

    @Autowired
    private ParameterDictionaryService dictionaryService;

    @Autowired
    private ParameterDictionaryMapper dictionaryMapper;

    @BeforeEach
    void setup() {
        ParameterDictionary dict = new ParameterDictionary();
        dict.setCode("CONCURRENT_TEST");
        dict.setName("并发测试");
        dict.setVersion(0);
        dictionaryMapper.insert(dict);
    }

    @Test
    @DisplayName("乐观锁并发更新")
    void optimisticLockConcurrentUpdate() throws InterruptedException {
        Long dictionaryId = dictionaryMapper.selectByCode("CONCURRENT_TEST").getId();
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    ParameterDictionary dict = dictionaryMapper.selectById(dictionaryId);
                    dict.setName("更新" + index);
                    dictionaryService.updateDictionary(dictionaryId, dict);
                    successCount.incrementAndGet();
                } catch (OptimisticLockException e) {
                    conflictCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // 只有一个成功，其他都冲突
        assertEquals(1, successCount.get());
        assertEquals(9, conflictCount.get());
    }
}
```

---

## 性能测试

### ParameterPerformanceTest
```java
@SpringBootTest
@AutoConfigureMockMvc
class ParameterPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParameterItemMapper itemMapper;

    @BeforeEach
    void setup() {
        // 插入10000条测试数据
        for (int i = 0; i < 10000; i++) {
            ParameterItem item = new ParameterItem();
            item.setDictionaryId(1L);
            item.setKey("KEY_" + i);
            item.setValue("VALUE_" + i);
            itemMapper.insert(item);
        }
    }

    @Test
    @DisplayName("查询性能 - 10000条数据分页")
    void queryPerformance_largeDataset() throws Exception {
        long start = System.nanoTime();

        mockMvc.perform(get("/api/v1/parameters/dictionaries/1/items")
                .param("page", "1")
                .param("size", "20"))
            .andExpect(status().isOk());

        long duration = System.nanoTime() - start;

        // 查询时间 < 1秒
        assertTrue(duration < 1_000_000_000);
    }

    @Test
    @DisplayName("搜索性能")
    void searchPerformance() throws Exception {
        long start = System.nanoTime();

        mockMvc.perform(get("/api/v1/parameters/dictionaries/1/items")
                .param("keyword", "KEY_5000"))
            .andExpect(status().isOk());

        long duration = System.nanoTime() - start;

        // 搜索时间 < 500ms
        assertTrue(duration < 500_000_000);
    }
}
```

### 性能指标

| 接口 | 数据量 | 响应时间 |
|------|--------|----------|
| GET /dictionaries | 1000字典 | < 200ms |
| GET /dictionaries/{id}/items | 10000项分页 | < 1s |
| POST /dictionaries | - | < 100ms |
| PUT /dictionaries/{id} | - | < 100ms |
| GET /dictionaries/search | 1000字典 | < 300ms |

---

## 测试清单

- [ ] 单元测试 - ParameterDictionaryService
- [ ] 单元测试 - ParameterItemService
- [ ] 单元测试 - ParameterCategoryService
- [ ] 单元测试 - ValidationService
- [ ] 单元测试 - AccessControlService
- [ ] 单元测试 - AuditLogService
- [ ] 集成测试 - 字典生命周期
- [ ] 集成测试 - 参数项CRUD
- [ ] 集成测试 - 分类管理
- [ ] 集成测试 - 并发编辑冲突
- [ ] 集成测试 - 验证规则
- [ ] 前端测试 - DictionaryList
- [ ] 前端测试 - DictionaryForm
- [ ] 前端测试 - ParameterItemList
- [ ] 前端测试 - CategoryTree
- [ ] 并发测试 - 乐观锁
- [ ] 性能测试 - 大数据量查询
- [ ] 性能测试 - 搜索性能
