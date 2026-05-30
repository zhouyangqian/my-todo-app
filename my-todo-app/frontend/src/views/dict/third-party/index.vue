<!-- views/dict/third-party/index.vue - 第三方API管理页面 -->
<template>
  <div class="third-party-container">
    <el-row :gutter="20">
      <!-- 左侧: API列表 + 密钥管理 -->
      <el-col :span="16">
        <!-- 搜索区域 -->
        <el-card class="search-card">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="API名称">
              <el-input v-model="searchForm.apiName" placeholder="请输入API名称" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- API列表 -->
        <el-card class="table-card">
          <template #header>
            <div class="card-header">
              <span>第三方API列表</span>
              <el-button type="primary" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                新增API
              </el-button>
            </div>
          </template>

          <el-table :data="tableData" v-loading="loading" border stripe>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="apiName" label="API名称" width="150" />
            <el-table-column prop="provider" label="服务商" width="120" />
            <el-table-column prop="apiUrl" label="接口地址" min-width="180" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '正常' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="260">
              <template #default="{ row }">
                <el-button type="info" link @click="handleViewKeys(row)">密钥</el-button>
                <el-button type="success" link @click="handleCheckHealth(row)">健康检查</el-button>
                <el-button type="warning" link @click="handleEdit(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
            class="pagination"
          />
        </el-card>
      </el-col>

      <!-- 右侧: 调用日志 -->
      <el-col :span="8">
        <el-card class="log-card">
          <template #header>
            <div class="card-header">
              <span>调用日志</span>
              <el-button size="small" @click="loadLogs">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>

          <el-table :data="logData" v-loading="logLoading" border stripe max-height="600">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="apiName" label="API" width="100" show-overflow-tooltip />
            <el-table-column prop="statusCode" label="状态码" width="70" />
            <el-table-column prop="duration" label="耗时(ms)" width="80" />
            <el-table-column prop="callTime" label="调用时间" min-width="130" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="API名称" prop="apiName">
          <el-input v-model="form.apiName" placeholder="请输入API名称" />
        </el-form-item>
        <el-form-item label="服务商" prop="provider">
          <el-input v-model="form.provider" placeholder="请输入服务商名称" />
        </el-form-item>
        <el-form-item label="接口地址" prop="apiUrl">
          <el-input v-model="form.apiUrl" placeholder="请输入接口地址" />
        </el-form-item>
        <el-form-item label="认证方式" prop="authType">
          <el-select v-model="form.authType" style="width: 100%">
            <el-option label="API Key" value="API_KEY" />
            <el-option label="OAuth2" value="OAUTH2" />
            <el-option label="Bearer Token" value="BEARER" />
            <el-option label="无认证" value="NONE" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 密钥管理对话框 -->
    <el-dialog
      v-model="keyDialogVisible"
      title="API密钥管理"
      width="600px"
      :close-on-click-modal="false"
    >
      <div style="margin-bottom: 12px;">
        <el-button type="primary" size="small" @click="handleAddKey">
          <el-icon><Plus /></el-icon>
          新增密钥
        </el-button>
      </div>
      <el-table :data="keyData" v-loading="keyLoading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="keyName" label="密钥名称" width="130" />
        <el-table-column prop="apiKey" label="密钥值" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '有效' : '失效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="handleDeleteKey(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 新增密钥对话框 -->
    <el-dialog
      v-model="addKeyDialogVisible"
      title="新增密钥"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form ref="keyFormRef" :model="keyForm" :rules="keyRules" label-width="100px">
        <el-form-item label="密钥名称" prop="keyName">
          <el-input v-model="keyForm.keyName" placeholder="请输入密钥名称" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="keyForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addKeyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleKeySubmit" :loading="keySubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getThirdPartyApis,
  createThirdPartyApi,
  updateThirdPartyApi,
  deleteThirdPartyApi,
  getApiKeys,
  createApiKey,
  deleteApiKey,
  getCallLogs,
  checkApiHealth
} from '@/api/dict'

// ==================== API列表相关 ====================
const searchForm = reactive({ apiName: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])
const loading = ref(false)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getThirdPartyApis({
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error('加载API列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadList()
}

const handleReset = () => {
  searchForm.apiName = ''
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadList()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadList()
}

// ==================== 新增/编辑对话框 ====================
const dialogVisible = ref(false)
const dialogTitle = ref('新增第三方API')
const formRef = ref()
const submitLoading = ref(false)

const form = reactive({
  id: null,
  apiName: '',
  provider: '',
  apiUrl: '',
  authType: 'API_KEY',
  description: '',
  status: 1
})

const rules = {
  apiName: [{ required: true, message: '请输入API名称', trigger: 'blur' }],
  provider: [{ required: true, message: '请输入服务商', trigger: 'blur' }],
  apiUrl: [{ required: true, message: '请输入接口地址', trigger: 'blur' }]
}

const handleAdd = () => {
  dialogTitle.value = '新增第三方API'
  Object.assign(form, {
    id: null, apiName: '', provider: '', apiUrl: '',
    authType: 'API_KEY', description: '', status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑第三方API'
  Object.assign(form, row)
  form.id = row.id
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (form.id) {
        await updateThirdPartyApi(form.id, { ...form })
        ElMessage.success('更新成功')
      } else {
        await createThirdPartyApi({ ...form })
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadList()
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定要删除API"${row.apiName}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteThirdPartyApi(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// ==================== 健康检查 ====================
const handleCheckHealth = async (row) => {
  try {
    const res = await checkApiHealth(row.id)
    if (res && res.healthy) {
      ElMessage.success(`${row.apiName} 健康状态正常，响应时间: ${res.duration}ms`)
    } else {
      ElMessage.warning(`${row.apiName} 健康检查异常`)
    }
  } catch (error) {
    ElMessage.error('健康检查失败')
  }
}

// ==================== 密钥管理 ====================
const keyDialogVisible = ref(false)
const keyData = ref([])
const keyLoading = ref(false)
const currentApiId = ref(null)

const handleViewKeys = async (row) => {
  currentApiId.value = row.id
  keyDialogVisible.value = true
  keyLoading.value = true
  try {
    const res = await getApiKeys({ apiId: row.id })
    keyData.value = res || []
  } catch (error) {
    ElMessage.error('加载密钥列表失败')
  } finally {
    keyLoading.value = false
  }
}

const addKeyDialogVisible = ref(false)
const keyFormRef = ref()
const keySubmitLoading = ref(false)
const keyForm = reactive({ keyName: '', remark: '' })
const keyRules = {
  keyName: [{ required: true, message: '请输入密钥名称', trigger: 'blur' }]
}

const handleAddKey = () => {
  keyForm.keyName = ''
  keyForm.remark = ''
  addKeyDialogVisible.value = true
}

const handleKeySubmit = async () => {
  if (!keyFormRef.value) return
  await keyFormRef.value.validate(async (valid) => {
    if (!valid) return
    keySubmitLoading.value = true
    try {
      await createApiKey({ apiId: currentApiId.value, ...keyForm })
      ElMessage.success('创建密钥成功')
      addKeyDialogVisible.value = false
      const res = await getApiKeys({ apiId: currentApiId.value })
      keyData.value = res || []
    } catch (error) {
      ElMessage.error('创建密钥失败')
    } finally {
      keySubmitLoading.value = false
    }
  })
}

const handleDeleteKey = async (row) => {
  await ElMessageBox.confirm(`确定要删除密钥"${row.keyName}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteApiKey(row.id)
    ElMessage.success('删除成功')
    const res = await getApiKeys({ apiId: currentApiId.value })
    keyData.value = res || []
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// ==================== 调用日志 ====================
const logData = ref([])
const logLoading = ref(false)

const loadLogs = async () => {
  logLoading.value = true
  try {
    const res = await getCallLogs({ page: 1, size: 20 })
    logData.value = res || []
  } catch (error) {
    ElMessage.error('加载调用日志失败')
  } finally {
    logLoading.value = false
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  loadList()
  loadLogs()
})
</script>

<style scoped lang="scss">
.third-party-container {
  padding: 20px;

  .search-card {
    margin-bottom: 20px;

    .search-form {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
    }
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .pagination {
      margin-top: 20px;
      justify-content: flex-end;
    }
  }

  .log-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
}
</style>
