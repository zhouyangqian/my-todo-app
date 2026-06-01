<!-- views/gateway/TrafficSwitch.vue - 流量切换/灰度发布页面
  展示金丝雀发布配置，支持调整流量比例
-->
<template>
  <div class="traffic-switch-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="服务名称">
          <el-input v-model="searchForm.serviceName" placeholder="请输入服务名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 金丝雀配置表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>灰度发布配置</span>
          <el-button type="primary" @click="handleAdd">新增规则</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="serviceName" label="服务名称" width="180" />
        <el-table-column prop="stableVersion" label="稳定版本" width="140" />
        <el-table-column prop="canaryVersion" label="灰度版本" width="140" />
        <el-table-column prop="canaryWeight" label="灰度流量比例" width="180">
          <template #default="{ row }">
            <el-slider
              v-model="row.canaryWeight"
              :min="0"
              :max="100"
              :step="5"
              :format-tooltip="(val) => val + '%'"
              @change="(val) => handleWeightChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? '运行中' : '已停止' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.enabled ? 'warning' : 'success'"
              link
              @click="handleToggle(row)"
            >
              {{ row.enabled ? '停止' : '启动' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-form-item label="服务名称" prop="serviceName">
          <el-input v-model="formData.serviceName" placeholder="请输入服务名称" />
        </el-form-item>
        <el-form-item label="稳定版本" prop="stableVersion">
          <el-input v-model="formData.stableVersion" placeholder="请输入稳定版本号" />
        </el-form-item>
        <el-form-item label="灰度版本" prop="canaryVersion">
          <el-input v-model="formData.canaryVersion" placeholder="请输入灰度版本号" />
        </el-form-item>
        <el-form-item label="灰度流量比例">
          <el-slider v-model="formData.canaryWeight" :min="0" :max="100" :step="5" show-input />
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
import { getCanaryConfig, updateCanaryConfig } from '@/api/canary'

const searchForm = reactive({ serviceName: '' })
const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增灰度规则')
const submitLoading = ref(false)
const formRef = ref()
const formData = reactive({
  id: undefined,
  serviceName: '',
  stableVersion: '',
  canaryVersion: '',
  canaryWeight: 10,
  enabled: true
})
const formRules = {
  serviceName: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  stableVersion: [{ required: true, message: '请输入稳定版本', trigger: 'blur' }],
  canaryVersion: [{ required: true, message: '请输入灰度版本', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCanaryConfig()
    tableData.value = res.data || res || []
  } catch (error) {
    ElMessage.error('加载灰度配置失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => loadData()
const handleReset = () => {
  searchForm.serviceName = ''
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增灰度规则'
  Object.assign(formData, { id: undefined, serviceName: '', stableVersion: '', canaryVersion: '', canaryWeight: 10, enabled: true })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑灰度规则'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await updateCanaryConfig(formData)
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

const handleWeightChange = async (row, val) => {
  try {
    await updateCanaryConfig({ ...row, canaryWeight: val })
    ElMessage.success('流量比例已更新')
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const handleToggle = async (row) => {
  const action = row.enabled ? '停止' : '启动'
  await ElMessageBox.confirm(`确定要${action}该灰度规则吗?`, '提示', { type: 'warning' })
  try {
    await updateCanaryConfig({ ...row, enabled: !row.enabled })
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (error) {
    ElMessage.error(`${action}失败`)
  }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.traffic-switch-container {
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
