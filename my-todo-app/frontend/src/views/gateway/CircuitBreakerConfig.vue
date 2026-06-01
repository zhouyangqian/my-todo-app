<!-- views/gateway/CircuitBreakerConfig.vue - 熔断器配置页面
  展示各服务的熔断器状态，支持重置操作
-->
<template>
  <div class="circuit-breaker-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="服务名称">
          <el-input v-model="searchForm.serviceId" placeholder="请输入服务名称" clearable />
        </el-form-item>
        <el-form-item label="熔断状态">
          <el-select v-model="searchForm.state" placeholder="请选择状态" clearable>
            <el-option label="关闭(正常)" value="CLOSED" />
            <el-option label="开启(熔断)" value="OPEN" />
            <el-option label="半开(探测)" value="HALF_OPEN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleResetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 熔断器状态表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>熔断器配置</span>
          <el-button type="primary" @click="loadData" :loading="loading">刷新</el-button>
        </div>
      </template>

      <el-table :data="filteredData" v-loading="loading" border stripe>
        <el-table-column prop="serviceId" label="服务名称" width="200" />
        <el-table-column prop="state" label="熔断状态" width="140">
          <template #default="{ row }">
            <el-tag :type="stateTagType(row.state)">
              {{ stateLabel(row.state) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failureRateThreshold" label="失败率阈值(%)" width="140" />
        <el-table-column prop="slowCallRateThreshold" label="慢调用率阈值(%)" width="140" />
        <el-table-column prop="failureCount" label="失败次数" width="100" />
        <el-table-column prop="successCount" label="成功次数" width="100" />
        <el-table-column prop="lastFailureTime" label="最后失败时间" min-width="180">
          <template #default="{ row }">
            <span v-if="row.lastFailureTime">{{ row.lastFailureTime }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="{ row }">
            <el-button
              type="warning"
              link
              :disabled="row.state === 'CLOSED'"
              @click="handleReset(row)"
            >
              重置
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCircuitBreakerStatus, resetCircuitBreaker } from '@/api/circuitBreaker'

const searchForm = reactive({ serviceId: '', state: '' })
const tableData = ref([])
const loading = ref(false)

const filteredData = computed(() => {
  return tableData.value.filter(item => {
    if (searchForm.serviceId && !item.serviceId?.includes(searchForm.serviceId)) return false
    if (searchForm.state && item.state !== searchForm.state) return false
    return true
  })
})

const stateTagType = (state) => {
  switch (state) {
    case 'CLOSED': return 'success'
    case 'OPEN': return 'danger'
    case 'HALF_OPEN': return 'warning'
    default: return 'info'
  }
}

const stateLabel = (state) => {
  switch (state) {
    case 'CLOSED': return '关闭(正常)'
    case 'OPEN': return '开启(熔断)'
    case 'HALF_OPEN': return '半开(探测)'
    default: return state
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCircuitBreakerStatus()
    tableData.value = res.data || res || []
  } catch (error) {
    ElMessage.error('加载熔断器状态失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => loadData()
const handleResetSearch = () => {
  searchForm.serviceId = ''
  searchForm.state = ''
  loadData()
}

const handleReset = async (row) => {
  await ElMessageBox.confirm(`确定要重置服务「${row.serviceId}」的熔断器吗?`, '提示', { type: 'warning' })
  try {
    await resetCircuitBreaker(row.serviceId)
    ElMessage.success('熔断器已重置')
    loadData()
  } catch (error) {
    ElMessage.error('重置失败')
  }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.circuit-breaker-container {
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

  .text-muted {
    color: #909399;
  }
}
</style>
