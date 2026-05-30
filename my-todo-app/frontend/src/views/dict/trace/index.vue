<!-- views/dict/trace/index.vue - 追踪管理页面 -->
<template>
  <div class="trace-container">
    <el-row :gutter="20">
      <!-- 左侧: 追踪配置 -->
      <el-col :span="14">
        <el-card class="config-card">
          <template #header>
            <div class="card-header">
              <span>追踪配置</span>
              <el-button type="primary" size="small" @click="handleAddConfig">
                <el-icon><Plus /></el-icon>
                新增
              </el-button>
            </div>
          </template>

          <el-form :inline="true" :model="configSearch" class="search-form">
            <el-form-item label="配置名称">
              <el-input v-model="configSearch.configName" placeholder="请输入配置名称" clearable size="small" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" @click="loadConfigs">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
            </el-form-item>
          </el-form>

          <el-table :data="configData" v-loading="configLoading" border stripe>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="configName" label="配置名称" width="140" />
            <el-table-column prop="traceType" label="追踪类型" width="110">
              <template #default="{ row }">
                <el-tag size="small">{{ row.traceType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sampleRate" label="采样率" width="80">
              <template #default="{ row }">
                {{ row.sampleRate }}%
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="120" show-overflow-tooltip />
            <el-table-column label="操作" fixed="right" width="150">
              <template #default="{ row }">
                <el-button type="warning" link size="small" @click="handleEditConfig(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDeleteConfig(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧: 告警列表 -->
      <el-col :span="10">
        <el-card class="alert-card">
          <template #header>
            <div class="card-header">
              <span>告警列表</span>
              <el-button size="small" @click="loadAlerts">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>

          <el-table :data="alertData" v-loading="alertLoading" border stripe max-height="500">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="alertName" label="告警名称" width="130" show-overflow-tooltip />
            <el-table-column prop="level" label="级别" width="80">
              <template #default="{ row }">
                <el-tag :type="row.level === 'HIGH' ? 'danger' : row.level === 'MEDIUM' ? 'warning' : 'info'" size="small">
                  {{ row.level === 'HIGH' ? '高' : row.level === 'MEDIUM' ? '中' : '低' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACKNOWLEDGED' ? 'success' : 'danger'" size="small">
                  {{ row.status === 'ACKNOWLEDGED' ? '已确认' : '待处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="90">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  link
                  size="small"
                  :disabled="row.status === 'ACKNOWLEDGED'"
                  @click="handleAcknowledge(row)"
                >
                  确认
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 追踪配置新增/编辑对话框 -->
    <el-dialog
      v-model="configDialogVisible"
      :title="configDialogTitle"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form ref="configFormRef" :model="configForm" :rules="configRules" label-width="100px">
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="configForm.configName" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="追踪类型" prop="traceType">
          <el-select v-model="configForm.traceType" style="width: 100%">
            <el-option label="全链路" value="FULL" />
            <el-option label="抽样" value="SAMPLE" />
            <el-option label="关键节点" value="KEY_NODE" />
          </el-select>
        </el-form-item>
        <el-form-item label="采样率(%)" prop="sampleRate">
          <el-input-number v-model="configForm.sampleRate" :min="0" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="超时阈值(ms)" prop="timeoutThreshold">
          <el-input-number v-model="configForm.timeoutThreshold" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="configForm.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="configForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfigSubmit" :loading="configSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getTraceConfigs,
  createTraceConfig,
  updateTraceConfig,
  deleteTraceConfig,
  getTraceAlerts,
  acknowledgeAlert
} from '@/api/dict'

// ==================== 追踪配置相关 ====================
const configSearch = reactive({ configName: '' })
const configData = ref([])
const configLoading = ref(false)

const loadConfigs = async () => {
  configLoading.value = true
  try {
    const res = await getTraceConfigs(configSearch)
    configData.value = res?.records || []
  } catch (error) {
    ElMessage.error('加载追踪配置失败')
  } finally {
    configLoading.value = false
  }
}

// ==================== 配置对话框 ====================
const configDialogVisible = ref(false)
const configDialogTitle = ref('新增追踪配置')
const configFormRef = ref()
const configSubmitLoading = ref(false)

const configForm = reactive({
  id: null,
  configName: '',
  traceType: 'FULL',
  sampleRate: 100,
  timeoutThreshold: 3000,
  description: '',
  status: 1
})

const configRules = {
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  traceType: [{ required: true, message: '请选择追踪类型', trigger: 'change' }]
}

const handleAddConfig = () => {
  configDialogTitle.value = '新增追踪配置'
  Object.assign(configForm, {
    id: null, configName: '', traceType: 'FULL',
    sampleRate: 100, timeoutThreshold: 3000, description: '', status: 1
  })
  configDialogVisible.value = true
}

const handleEditConfig = (row) => {
  configDialogTitle.value = '编辑追踪配置'
  Object.assign(configForm, row)
  configForm.id = row.id
  configDialogVisible.value = true
}

const handleConfigSubmit = async () => {
  if (!configFormRef.value) return
  await configFormRef.value.validate(async (valid) => {
    if (!valid) return
    configSubmitLoading.value = true
    try {
      if (configForm.id) {
        await updateTraceConfig(configForm.id, { ...configForm })
        ElMessage.success('更新成功')
      } else {
        await createTraceConfig({ ...configForm })
        ElMessage.success('创建成功')
      }
      configDialogVisible.value = false
      loadConfigs()
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      configSubmitLoading.value = false
    }
  })
}

const handleDeleteConfig = async (row) => {
  await ElMessageBox.confirm(`确定要删除配置"${row.configName}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteTraceConfig(row.id)
    ElMessage.success('删除成功')
    loadConfigs()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// ==================== 告警相关 ====================
const alertData = ref([])
const alertLoading = ref(false)

const loadAlerts = async () => {
  alertLoading.value = true
  try {
    const res = await getTraceAlerts({})
    alertData.value = res?.records || []
  } catch (error) {
    ElMessage.error('加载告警列表失败')
  } finally {
    alertLoading.value = false
  }
}

const handleAcknowledge = async (row) => {
  await ElMessageBox.confirm(`确定要确认告警"${row.alertName}"吗？`, '提示', { type: 'info' })
  try {
    await acknowledgeAlert(row.id)
    ElMessage.success('确认成功')
    loadAlerts()
  } catch (error) {
    ElMessage.error('确认失败')
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  loadConfigs()
  loadAlerts()
})
</script>

<style scoped lang="scss">
.trace-container {
  padding: 20px;

  .config-card,
  .alert-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .search-form {
      margin-bottom: 12px;
    }
  }
}
</style>
