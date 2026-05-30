<!-- views/dict/api-market/index.vue - API市场页面 -->
<template>
  <div class="api-market-container">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="API名称">
          <el-input v-model="searchForm.apiName" placeholder="请输入API名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="已发布" :value="1" />
            <el-option label="未发布" :value="0" />
          </el-select>
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
          <span>API列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增API
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="apiName" label="API名称" width="150" />
        <el-table-column prop="apiCode" label="API编码" width="140" />
        <el-table-column prop="apiUrl" label="接口地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="method" label="请求方式" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.method === 'GET' ? 'success' : row.method === 'POST' ? 'primary' : row.method === 'PUT' ? 'warning' : 'danger'">
              {{ row.method }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '未发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleSubscribe(row)">订阅</el-button>
            <el-button type="info" link @click="handleViewUsage(row)">用量</el-button>
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
        <el-form-item label="API编码" prop="apiCode">
          <el-input v-model="form.apiCode" placeholder="请输入API编码" />
        </el-form-item>
        <el-form-item label="接口地址" prop="apiUrl">
          <el-input v-model="form.apiUrl" placeholder="请输入接口地址" />
        </el-form-item>
        <el-form-item label="请求方式" prop="method">
          <el-select v-model="form.method" style="width: 100%">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本" prop="version">
          <el-input v-model="form.version" placeholder="请输入版本号" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">已发布</el-radio>
            <el-radio :value="0">未发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 使用统计对话框 -->
    <el-dialog
      v-model="usageDialogVisible"
      title="API使用统计"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-table :data="usageData" v-loading="usageLoading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="callerName" label="调用方" width="140" />
        <el-table-column prop="callCount" label="调用次数" width="100" />
        <el-table-column prop="avgDuration" label="平均耗时(ms)" width="120" />
        <el-table-column prop="successRate" label="成功率" width="100">
          <template #default="{ row }">
            <el-tag :type="row.successRate >= 95 ? 'success' : row.successRate >= 80 ? 'warning' : 'danger'" size="small">
              {{ row.successRate }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastCallTime" label="最后调用时间" min-width="160" />
      </el-table>
    </el-dialog>

    <!-- 订阅对话框 -->
    <el-dialog
      v-model="subscribeDialogVisible"
      title="订阅API"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="subscribeFormRef" :model="subscribeForm" :rules="subscribeRules" label-width="100px">
        <el-form-item label="API名称">
          <el-input :model-value="currentApi?.apiName" disabled />
        </el-form-item>
        <el-form-item label="调用配额" prop="quota">
          <el-input-number v-model="subscribeForm.quota" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="subscribeForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="subscribeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubscribeSubmit" :loading="subscribeLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import {
  getApiList,
  createApi,
  updateApi,
  deleteApi,
  subscribeApi,
  getApiUsage
} from '@/api/dict'

// ==================== 列表相关 ====================
const searchForm = reactive({ apiName: '', status: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])
const loading = ref(false)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getApiList({
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
  searchForm.status = null
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
const dialogTitle = ref('新增API')
const formRef = ref()
const submitLoading = ref(false)

const form = reactive({
  id: null,
  apiName: '',
  apiCode: '',
  apiUrl: '',
  method: 'GET',
  version: '1.0',
  description: '',
  status: 1
})

const rules = {
  apiName: [{ required: true, message: '请输入API名称', trigger: 'blur' }],
  apiCode: [{ required: true, message: '请输入API编码', trigger: 'blur' }],
  apiUrl: [{ required: true, message: '请输入接口地址', trigger: 'blur' }],
  method: [{ required: true, message: '请选择请求方式', trigger: 'change' }]
}

const handleAdd = () => {
  dialogTitle.value = '新增API'
  Object.assign(form, {
    id: null, apiName: '', apiCode: '', apiUrl: '',
    method: 'GET', version: '1.0', description: '', status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑API'
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
        await updateApi(form.id, { ...form })
        ElMessage.success('更新成功')
      } else {
        await createApi({ ...form })
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
    await deleteApi(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// ==================== 订阅相关 ====================
const subscribeDialogVisible = ref(false)
const subscribeLoading = ref(false)
const currentApi = ref(null)
const subscribeFormRef = ref()
const subscribeForm = reactive({ quota: 1000, remark: '' })
const subscribeRules = {
  quota: [{ required: true, message: '请输入调用配额', trigger: 'blur' }]
}

const handleSubscribe = (row) => {
  currentApi.value = row
  subscribeForm.quota = 1000
  subscribeForm.remark = ''
  subscribeDialogVisible.value = true
}

const handleSubscribeSubmit = async () => {
  if (!subscribeFormRef.value) return
  await subscribeFormRef.value.validate(async (valid) => {
    if (!valid) return
    subscribeLoading.value = true
    try {
      await subscribeApi({
        apiId: currentApi.value.id,
        quota: subscribeForm.quota,
        remark: subscribeForm.remark
      })
      ElMessage.success('订阅成功')
      subscribeDialogVisible.value = false
    } catch (error) {
      ElMessage.error('订阅失败')
    } finally {
      subscribeLoading.value = false
    }
  })
}

// ==================== 使用统计相关 ====================
const usageDialogVisible = ref(false)
const usageData = ref([])
const usageLoading = ref(false)

const handleViewUsage = async (row) => {
  usageDialogVisible.value = true
  usageLoading.value = true
  try {
    const res = await getApiUsage({ apiId: row.id })
    usageData.value = res || []
  } catch (error) {
    ElMessage.error('加载使用统计失败')
  } finally {
    usageLoading.value = false
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  loadList()
})
</script>

<style scoped lang="scss">
.api-market-container {
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
}
</style>
