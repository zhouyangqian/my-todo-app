<!-- views/dict/codegen/index.vue - 代码生成页面 -->
<template>
  <div class="codegen-container">
    <el-row :gutter="20">
      <!-- 左侧: 代码模板 -->
      <el-col :span="14">
        <el-card class="template-card">
          <template #header>
            <div class="card-header">
              <span>代码模板</span>
              <div>
                <el-button type="success" size="small" @click="handleBatchGenerate">
                  批量生成
                </el-button>
                <el-button type="primary" size="small" @click="handleAddTemplate">
                  <el-icon><Plus /></el-icon>
                  新增模板
                </el-button>
              </div>
            </div>
          </template>

          <el-form :inline="true" :model="templateSearch" class="search-form">
            <el-form-item label="模板名称">
              <el-input v-model="templateSearch.templateName" placeholder="请输入" clearable size="small" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" @click="loadTemplates">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
            </el-form-item>
          </el-form>

          <el-table :data="templateData" v-loading="templateLoading" border stripe>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="templateName" label="模板名称" width="150" />
            <el-table-column prop="templateType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.templateType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="120" show-overflow-tooltip />
            <el-table-column label="操作" fixed="right" width="200">
              <template #default="{ row }">
                <el-button type="success" link size="small" @click="handleGenerate(row)">生成</el-button>
                <el-button type="warning" link size="small" @click="handleEditTemplate(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDeleteTemplate(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧: 生成历史 -->
      <el-col :span="10">
        <el-card class="history-card">
          <template #header>
            <div class="card-header">
              <span>生成历史</span>
              <el-button size="small" @click="loadHistory">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>

          <el-table :data="historyData" v-loading="historyLoading" border stripe max-height="500">
            <el-table-column prop="id" label="ID" width="50" />
            <el-table-column prop="tableName" label="表名" width="110" show-overflow-tooltip />
            <el-table-column prop="genType" label="类型" width="70" />
            <el-table-column prop="createdAt" label="生成时间" width="140" show-overflow-tooltip />
            <el-table-column label="操作" fixed="right" width="60">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleViewHistory(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 模板新增/编辑对话框 -->
    <el-dialog
      v-model="templateDialogVisible"
      :title="templateDialogTitle"
      width="650px"
      :close-on-click-modal="false"
    >
      <el-form ref="templateFormRef" :model="templateForm" :rules="templateRules" label-width="100px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="templateForm.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板类型" prop="templateType">
          <el-select v-model="templateForm.templateType" style="width: 100%">
            <el-option label="Entity" value="ENTITY" />
            <el-option label="Mapper" value="MAPPER" />
            <el-option label="Service" value="SERVICE" />
            <el-option label="Controller" value="CONTROLLER" />
            <el-option label="Vue页面" value="VUE" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板内容" prop="templateContent">
          <el-input
            v-model="templateForm.templateContent"
            type="textarea"
            :rows="12"
            placeholder="支持变量: ${tableName} ${EntityName} ${entityName} ${packageName} ${moduleName} ${businessName}&#10;字段循环: ${foreach column in columns}...${endfor}&#10;字段变量: ${column.fieldName} ${column.javaType} ${column.columnName} ${column.columnComment}"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="templateForm.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTemplateSubmit" :loading="templateSubmitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 代码生成对话框 -->
    <el-dialog
      v-model="generateDialogVisible"
      title="代码生成"
      width="750px"
      :close-on-click-modal="false"
    >
      <el-form ref="genFormRef" :model="genForm" :rules="genRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="数据表" prop="tableName">
              <el-select
                v-model="genForm.tableName"
                filterable
                placeholder="请选择数据库表"
                style="width: 100%"
                @change="handleTableChange"
              >
                <el-option-group v-for="group in tableGrouped" :key="group.label" :label="group.label">
                  <el-option
                    v-for="t in group.options"
                    :key="t.tableSchema + '.' + t.tableName"
                    :label="t.tableName + (t.tableComment ? ' (' + t.tableComment + ')' : '')"
                    :value="t.tableName"
                  />
                </el-option-group>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="包名" prop="packageName">
              <el-input v-model="genForm.packageName" placeholder="如: com.example.erp" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模块名">
              <el-input v-model="genForm.moduleName" placeholder="如: product" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务名">
              <el-input v-model="genForm.businessName" placeholder="如: 商品" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 字段预览 -->
      <div v-if="columnData.length > 0" style="margin-top: 10px;">
        <h4 style="margin-bottom: 8px;">字段预览 ({{ columnData.length }} 个字段)</h4>
        <el-table :data="columnData" border stripe size="small" max-height="250">
          <el-table-column prop="columnName" label="列名" width="130" />
          <el-table-column prop="fieldName" label="Java字段" width="130" />
          <el-table-column prop="javaType" label="Java类型" width="120" />
          <el-table-column prop="columnType" label="数据库类型" width="120" />
          <el-table-column prop="columnKey" label="键" width="60">
            <template #default="{ row }">
              <el-tag v-if="row.columnKey === 'PRI'" type="danger" size="small">PRI</el-tag>
              <span v-else>{{ row.columnKey }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="columnComment" label="注释" min-width="100" show-overflow-tooltip />
        </el-table>
      </div>

      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGenerateSubmit" :loading="genLoading">
          生成 ({{ currentTemplate?.templateType || '单模板' }})
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量生成对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量代码生成"
      width="750px"
      :close-on-click-modal="false"
    >
      <el-form :model="genForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="数据表">
              <el-select
                v-model="genForm.tableName"
                filterable
                placeholder="请选择数据库表"
                style="width: 100%"
                @change="handleTableChange"
              >
                <el-option-group v-for="group in tableGrouped" :key="group.label" :label="group.label">
                  <el-option
                    v-for="t in group.options"
                    :key="t.tableSchema + '.' + t.tableName"
                    :label="t.tableName + (t.tableComment ? ' (' + t.tableComment + ')' : '')"
                    :value="t.tableName"
                  />
                </el-option-group>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="包名">
              <el-input v-model="genForm.packageName" placeholder="如: com.example.erp" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模块名">
              <el-input v-model="genForm.moduleName" placeholder="如: product" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务名">
              <el-input v-model="genForm.businessName" placeholder="如: 商品" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="生成类型">
          <el-checkbox-group v-model="selectedTypes">
            <el-checkbox label="ENTITY" value="ENTITY">Entity</el-checkbox>
            <el-checkbox label="MAPPER" value="MAPPER">Mapper</el-checkbox>
            <el-checkbox label="SERVICE" value="SERVICE">Service</el-checkbox>
            <el-checkbox label="CONTROLLER" value="CONTROLLER">Controller</el-checkbox>
            <el-checkbox label="VUE" value="VUE">Vue页面</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>

      <!-- 字段预览 -->
      <div v-if="columnData.length > 0" style="margin-top: 10px;">
        <h4 style="margin-bottom: 8px;">字段预览 ({{ columnData.length }} 个字段)</h4>
        <el-table :data="columnData" border stripe size="small" max-height="250">
          <el-table-column prop="columnName" label="列名" width="130" />
          <el-table-column prop="fieldName" label="Java字段" width="130" />
          <el-table-column prop="javaType" label="Java类型" width="120" />
          <el-table-column prop="columnType" label="数据库类型" width="120" />
          <el-table-column prop="columnKey" label="键" width="60">
            <template #default="{ row }">
              <el-tag v-if="row.columnKey === 'PRI'" type="danger" size="small">PRI</el-tag>
              <span v-else>{{ row.columnKey }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="columnComment" label="注释" min-width="100" show-overflow-tooltip />
        </el-table>
      </div>

      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchSubmit" :loading="batchLoading">
          批量生成 ({{ selectedTypes.length }} 层)
        </el-button>
      </template>
    </el-dialog>

    <!-- 代码预览对话框 -->
    <el-dialog v-model="previewVisible" title="代码预览" width="900px" fullscreen :close-on-click-modal="false">
      <el-tabs v-model="previewActiveTab">
        <el-tab-pane
          v-for="(code, type) in previewResults"
          :key="type"
          :label="type"
          :name="type"
        >
          <div style="margin-bottom: 8px;">
            <el-button size="small" @click="copyCode(code)">复制代码</el-button>
          </div>
          <pre class="code-block"><code>{{ code }}</code></pre>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getTemplates,
  createTemplate,
  updateTemplate,
  deleteTemplate,
  generateCode,
  getGenHistory,
  getGenHistoryDetail,
  listDbTables,
  listDbColumns,
  generateCodeBatch
} from '@/api/dict'

// ==================== 模板相关 ====================
const templateSearch = reactive({ templateName: '' })
const templateData = ref([])
const templateLoading = ref(false)

const loadTemplates = async () => {
  templateLoading.value = true
  try {
    const res = await getTemplates(templateSearch)
    templateData.value = res?.records || []
  } catch (error) {
    ElMessage.error('加载模板列表失败')
  } finally {
    templateLoading.value = false
  }
}

// ==================== 模板对话框 ====================
const templateDialogVisible = ref(false)
const templateDialogTitle = ref('新增模板')
const templateFormRef = ref()
const templateSubmitLoading = ref(false)

const templateForm = reactive({
  id: null,
  templateName: '',
  templateType: 'CONTROLLER',
  templateContent: '',
  description: ''
})

const templateRules = {
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  templateType: [{ required: true, message: '请选择模板类型', trigger: 'change' }],
  templateContent: [{ required: true, message: '请输入模板内容', trigger: 'blur' }]
}

const handleAddTemplate = () => {
  templateDialogTitle.value = '新增模板'
  Object.assign(templateForm, {
    id: null, templateName: '', templateType: 'CONTROLLER',
    templateContent: '', description: ''
  })
  templateDialogVisible.value = true
}

const handleEditTemplate = (row) => {
  templateDialogTitle.value = '编辑模板'
  Object.assign(templateForm, {
    id: row.id,
    templateName: row.templateName,
    templateType: row.templateType,
    templateContent: row.templateContent,
    description: row.description
  })
  templateDialogVisible.value = true
}

const handleTemplateSubmit = async () => {
  if (!templateFormRef.value) return
  await templateFormRef.value.validate(async (valid) => {
    if (!valid) return
    templateSubmitLoading.value = true
    try {
      if (templateForm.id) {
        await updateTemplate(templateForm.id, { ...templateForm })
        ElMessage.success('更新成功')
      } else {
        await createTemplate({ ...templateForm })
        ElMessage.success('创建成功')
      }
      templateDialogVisible.value = false
      loadTemplates()
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      templateSubmitLoading.value = false
    }
  })
}

const handleDeleteTemplate = async (row) => {
  await ElMessageBox.confirm(`确定要删除模板"${row.templateName}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteTemplate(row.id)
    ElMessage.success('删除成功')
    loadTemplates()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// ==================== 数据库元数据 ====================
const tableOptions = ref([])
const columnData = ref([])

const loadTables = async () => {
  try {
    tableOptions.value = await listDbTables() || []
  } catch (error) {
    ElMessage.error('加载表列表失败')
  }
}

/** 按数据库分组 */
const tableGrouped = computed(() => {
  const map = {}
  for (const t of tableOptions.value) {
    const schema = t.tableSchema
    if (!map[schema]) map[schema] = { label: schema, options: [] }
    map[schema].options.push(t)
  }
  return Object.values(map)
})

const handleTableChange = async (val) => {
  const selected = tableOptions.value.find(t => t.tableName === val)
  if (selected) {
    genForm.schema = selected.tableSchema
    try {
      columnData.value = await listDbColumns(selected.tableSchema, selected.tableName) || []
    } catch (error) {
      columnData.value = []
    }
    // 自动填充模块名（取表名前缀如 erp_product -> erp）
    if (!genForm.moduleName && val.includes('_')) {
      genForm.moduleName = val.split('_')[0]
    }
  }
}

// ==================== 单模板生成 ====================
const generateDialogVisible = ref(false)
const currentTemplate = ref(null)
const genFormRef = ref()
const genLoading = ref(false)

const genForm = reactive({
  schema: '',
  tableName: '',
  packageName: 'com.example',
  moduleName: '',
  businessName: ''
})

const genRules = {
  tableName: [{ required: true, message: '请选择表', trigger: 'change' }],
  packageName: [{ required: true, message: '请输入包名', trigger: 'blur' }]
}

const handleGenerate = async (row) => {
  currentTemplate.value = row
  genForm.schema = ''
  genForm.tableName = ''
  genForm.packageName = 'com.example'
  genForm.moduleName = ''
  genForm.businessName = ''
  columnData.value = []
  await loadTables()
  generateDialogVisible.value = true
}

const handleGenerateSubmit = async () => {
  if (!genFormRef.value) return
  await genFormRef.value.validate(async (valid) => {
    if (!valid) return
    genLoading.value = true
    try {
      const res = await generateCode({
        templateId: currentTemplate.value.id,
        schema: genForm.schema,
        tableName: genForm.tableName,
        packageName: genForm.packageName,
        moduleName: genForm.moduleName,
        businessName: genForm.businessName
      })
      // 打开代码预览
      previewResults.value = { [currentTemplate.value.templateType]: res }
      previewActiveTab.value = currentTemplate.value.templateType
      previewVisible.value = true
      generateDialogVisible.value = false
      loadHistory()
    } catch (error) {
      ElMessage.error('代码生成失败')
    } finally {
      genLoading.value = false
    }
  })
}

// ==================== 批量生成 ====================
const batchDialogVisible = ref(false)
const batchLoading = ref(false)
const selectedTypes = ref(['ENTITY', 'MAPPER', 'SERVICE', 'CONTROLLER', 'VUE'])

const handleBatchGenerate = async () => {
  genForm.schema = ''
  genForm.tableName = ''
  genForm.packageName = 'com.example'
  genForm.moduleName = ''
  genForm.businessName = ''
  columnData.value = []
  selectedTypes.value = ['ENTITY', 'MAPPER', 'SERVICE', 'CONTROLLER', 'VUE']
  await loadTables()
  batchDialogVisible.value = true
}

const handleBatchSubmit = async () => {
  if (!genForm.tableName) {
    ElMessage.warning('请先选择数据库表')
    return
  }
  if (selectedTypes.value.length === 0) {
    ElMessage.warning('请至少选择一种生成类型')
    return
  }
  batchLoading.value = true
  try {
    const res = await generateCodeBatch({
      schema: genForm.schema,
      tableName: genForm.tableName,
      packageName: genForm.packageName,
      moduleName: genForm.moduleName,
      businessName: genForm.businessName,
      templateTypes: selectedTypes.value
    })
    previewResults.value = res || {}
    const keys = Object.keys(previewResults.value)
    previewActiveTab.value = keys.length > 0 ? keys[0] : ''
    previewVisible.value = true
    batchDialogVisible.value = false
    loadHistory()
  } catch (error) {
    ElMessage.error('批量生成失败')
  } finally {
    batchLoading.value = false
  }
}

// ==================== 代码预览 ====================
const previewVisible = ref(false)
const previewResults = ref({})
const previewActiveTab = ref('')

const copyCode = async (code) => {
  try {
    await navigator.clipboard.writeText(code)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

// ==================== 生成历史 ====================
const historyData = ref([])
const historyLoading = ref(false)

const loadHistory = async () => {
  historyLoading.value = true
  try {
    const res = await getGenHistory({ page: 1, size: 20 })
    historyData.value = res?.records || []
  } catch (error) {
    ElMessage.error('加载生成历史失败')
  } finally {
    historyLoading.value = false
  }
}

const handleViewHistory = async (row) => {
  try {
    const res = await getGenHistoryDetail(row.id)
    if (res?.genContent) {
      previewResults.value = { [row.genType || 'HISTORY']: res.genContent }
      previewActiveTab.value = row.genType || 'HISTORY'
      previewVisible.value = true
    }
  } catch {
    ElMessage.error('加载历史详情失败')
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  loadTemplates()
  loadHistory()
})
</script>

<style scoped lang="scss">
.codegen-container {
  padding: 20px;

  .template-card,
  .history-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .search-form {
      margin-bottom: 12px;
    }
  }

  .code-block {
    background: #1e1e1e;
    color: #d4d4d4;
    padding: 16px;
    border-radius: 6px;
    overflow: auto;
    max-height: calc(100vh - 250px);
    font-size: 13px;
    line-height: 1.5;
    white-space: pre;
    tab-size: 4;
  }
}
</style>
