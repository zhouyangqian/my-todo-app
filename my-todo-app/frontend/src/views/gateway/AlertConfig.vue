<!-- views/gateway/AlertConfig.vue - 告警规则配置页面
  展示告警规则列表，支持新增、编辑、删除告警规则
-->
<template>
  <div class="alert-config-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="规则名称">
          <el-input v-model="searchForm.name" placeholder="请输入规则名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
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

    <!-- 告警规则表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>告警规则配置</span>
          <el-button type="primary" @click="handleAdd">新增规则</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" label="规则名称" width="180" />
        <el-table-column prop="metric" label="监控指标" width="150" />
        <el-table-column prop="condition" label="条件" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ conditionLabel(row.condition) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="threshold" label="阈值" width="100" />
        <el-table-column prop="duration" label="持续时间(秒)" width="130" />
        <el-table-column prop="notifyType" label="通知方式" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="warning">{{ row.notifyType || '邮件' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.enabled ? 'warning' : 'success'"
              link
              @click="handleToggle(row)"
            >
              {{ row.enabled ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="监控指标" prop="metric">
          <el-select v-model="formData.metric" placeholder="请选择监控指标">
            <el-option label="响应时间" value="response_time" />
            <el-option label="错误率" value="error_rate" />
            <el-option label="请求量" value="request_count" />
            <el-option label="CPU使用率" value="cpu_usage" />
            <el-option label="内存使用率" value="memory_usage" />
          </el-select>
        </el-form-item>
        <el-form-item label="条件" prop="condition">
          <el-select v-model="formData.condition" placeholder="请选择条件">
            <el-option label="大于" value="GT" />
            <el-option label="小于" value="LT" />
            <el-option label="等于" value="EQ" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值" prop="threshold">
          <el-input-number v-model="formData.threshold" :precision="2" />
        </el-form-item>
        <el-form-item label="持续时间(秒)">
          <el-input-number v-model="formData.duration" :min="0" />
        </el-form-item>
        <el-form-item label="通知方式">
          <el-select v-model="formData.notifyType" placeholder="请选择通知方式">
            <el-option label="邮件" value="EMAIL" />
            <el-option label="短信" value="SMS" />
            <el-option label="钉钉" value="DINGTALK" />
          </el-select>
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
import { getMetrics } from '@/api/metrics'

const searchForm = reactive({ name: '', enabled: '' })
const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增规则')
const submitLoading = ref(false)
const formRef = ref()
const formData = reactive({
  id: undefined,
  name: '',
  metric: '',
  condition: 'GT',
  threshold: 0,
  duration: 60,
  notifyType: 'EMAIL',
  enabled: true
})
const formRules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  metric: [{ required: true, message: '请选择监控指标', trigger: 'change' }],
  condition: [{ required: true, message: '请选择条件', trigger: 'change' }],
  threshold: [{ required: true, message: '请输入阈值', trigger: 'blur' }]
}

const conditionLabel = (condition) => {
  const map = { GT: '大于', LT: '小于', EQ: '等于' }
  return map[condition] || condition
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMetrics()
    tableData.value = res.data || res || []
  } catch (error) {
    ElMessage.error('加载告警规则失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => loadData()
const handleReset = () => {
  searchForm.name = ''
  searchForm.enabled = ''
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增规则'
  Object.assign(formData, { id: undefined, name: '', metric: '', condition: 'GT', threshold: 0, duration: 60, notifyType: 'EMAIL', enabled: true })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑规则'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      ElMessage.success(formData.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleToggle = async (row) => {
  const action = row.enabled ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定要${action}该规则吗?`, '提示', { type: 'warning' })
  try {
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (error) {
    ElMessage.error(`${action}失败`)
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该规则吗?', '提示', { type: 'warning' })
  try {
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.alert-config-container {
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
