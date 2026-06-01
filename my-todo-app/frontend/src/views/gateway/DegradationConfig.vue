<!-- views/gateway/DegradationConfig.vue - 降级策略配置页面
  展示服务降级配置，支持编辑降级响应
-->
<template>
  <div class="degradation-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="服务名称">
          <el-input v-model="searchForm.serviceId" placeholder="请输入服务名称" clearable />
        </el-form-item>
        <el-form-item label="降级状态">
          <el-select v-model="searchForm.enabled" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 降级配置表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>降级策略配置</span>
          <el-button type="primary" @click="handleAdd">新增策略</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="serviceId" label="服务名称" width="200" />
        <el-table-column prop="strategy" label="降级策略" width="140">
          <template #default="{ row }">
            <el-tag size="small">{{ strategyLabel(row.strategy) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fallbackResponse" label="降级响应内容" min-width="250">
          <template #default="{ row }">
            <span>{{ row.fallbackResponse || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.enabled ? 'warning' : 'success'"
              link
              @click="handleToggle(row)"
            >
              {{ row.enabled ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-form-item label="服务名称" prop="serviceId">
          <el-input v-model="formData.serviceId" placeholder="请输入服务名称" />
        </el-form-item>
        <el-form-item label="降级策略" prop="strategy">
          <el-select v-model="formData.strategy" placeholder="请选择降级策略">
            <el-option label="返回默认值" value="DEFAULT_VALUE" />
            <el-option label="返回缓存数据" value="CACHE" />
            <el-option label="返回空数据" value="EMPTY" />
            <el-option label="自定义响应" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="降级响应内容">
          <el-input
            v-model="formData.fallbackResponse"
            type="textarea"
            :rows="4"
            placeholder="请输入降级时的返回内容（JSON格式）"
          />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="formData.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDegradationConfig, updateDegradationConfig } from '@/api/circuitBreaker'

const searchForm = reactive({ serviceId: '', enabled: '' })
const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增降级策略')
const submitLoading = ref(false)
const formRef = ref()
const formData = reactive({
  id: undefined,
  serviceId: '',
  strategy: 'DEFAULT_VALUE',
  fallbackResponse: '',
  enabled: true
})
const formRules = {
  serviceId: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  strategy: [{ required: true, message: '请选择降级策略', trigger: 'change' }]
}

const strategyLabel = (strategy) => {
  const map = { DEFAULT_VALUE: '返回默认值', CACHE: '返回缓存', EMPTY: '返回空数据', CUSTOM: '自定义响应' }
  return map[strategy] || strategy
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDegradationConfig()
    tableData.value = res.data || res || []
  } catch (error) {
    ElMessage.error('加载降级配置失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => loadData()
const handleReset = () => {
  searchForm.serviceId = ''
  searchForm.enabled = ''
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增降级策略'
  Object.assign(formData, { id: undefined, serviceId: '', strategy: 'DEFAULT_VALUE', fallbackResponse: '', enabled: true })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑降级策略'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await updateDegradationConfig(formData)
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error('保存失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleToggle = async (row) => {
  const action = row.enabled ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定要${action}该服务的降级策略吗?`, '提示', { type: 'warning' })
  try {
    await updateDegradationConfig({ ...row, enabled: !row.enabled })
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (error) {
    ElMessage.error(`${action}失败`)
  }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.degradation-container {
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
  }
}
</style>
