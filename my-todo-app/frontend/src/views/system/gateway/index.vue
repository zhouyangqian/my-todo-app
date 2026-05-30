<!-- views/system/gateway/index.vue - 网关监控页面
  展示服务健康状态和断路器状态，支持自动刷新
-->
<template>
  <div class="gateway-container">
    <!-- 服务健康状态 -->
    <el-card class="status-card">
      <template #header>
        <div class="card-header">
          <span>服务健康状态</span>
          <div class="header-actions">
            <el-tag type="info" size="small">自动刷新: {{ countdown }}s</el-tag>
            <el-button type="primary" size="small" @click="fetchData" :loading="loading">
              手动刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="serviceStatuses" v-loading="loading" border stripe>
        <el-table-column prop="serviceName" label="服务名称" width="180" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastCheckTime" label="最后检查时间" width="200" />
        <el-table-column prop="responseTime" label="响应时间(ms)" width="140">
          <template #default="{ row }">
            <span v-if="row.responseTime != null">{{ row.responseTime }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" min-width="200">
          <template #default="{ row }">
            <span v-if="row.errorMessage" class="error-text">{{ row.errorMessage }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 断路器状态 -->
    <el-card class="status-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>断路器状态</span>
        </div>
      </template>

      <el-table :data="circuitBreakerStatuses" v-loading="loading" border stripe>
        <el-table-column prop="serviceId" label="服务名称" width="180" />
        <el-table-column prop="state" label="断路器状态" width="160">
          <template #default="{ row }">
            <el-tag :type="circuitTagType(row.state)">
              {{ row.state }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failureCount" label="失败次数" width="120" />
        <el-table-column prop="successCount" label="成功次数" width="120" />
        <el-table-column prop="lastFailureTime" label="最后失败时间" min-width="200">
          <template #default="{ row }">
            <span v-if="row.lastFailureTime">{{ row.lastFailureTime }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="circuitBreakerStatuses.length === 0 && !loading" description="暂无断路器数据（请求经过网关后自动生成）" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getServiceStatuses, getCircuitBreakerStatus } from '@/api/gateway'

// 服务健康状态数据
const serviceStatuses = ref([])
// 断路器状态数据
const circuitBreakerStatuses = ref([])
// 加载状态
const loading = ref(false)
// 自动刷新倒计时（秒）
const countdown = ref(10)
// 定时器引用
let refreshTimer = null
let countdownTimer = null

/**
 * 获取服务健康状态
 */
const fetchServiceStatuses = async () => {
  try {
    const res = await getServiceStatuses()
    serviceStatuses.value = res || []
  } catch (error) {
    console.error('获取服务状态失败', error)
  }
}

/**
 * 获取断路器状态
 */
const fetchCircuitBreakerStatus = async () => {
  try {
    const res = await getCircuitBreakerStatus()
    circuitBreakerStatuses.value = res || []
  } catch (error) {
    console.error('获取断路器状态失败', error)
  }
}

/**
 * 获取所有数据
 */
const fetchData = async () => {
  loading.value = true
  try {
    await Promise.all([fetchServiceStatuses(), fetchCircuitBreakerStatus()])
  } catch (error) {
    ElMessage.error('获取监控数据失败')
  } finally {
    loading.value = false
    countdown.value = 10
  }
}

/**
 * 健康状态对应的 tag 类型
 */
const statusTagType = (status) => {
  switch (status) {
    case 'UP': return 'success'
    case 'DOWN': return 'danger'
    default: return 'info'
  }
}

/**
 * 断路器状态对应的 tag 类型
 */
const circuitTagType = (state) => {
  switch (state) {
    case 'CLOSED': return 'success'
    case 'OPEN': return 'danger'
    case 'HALF_OPEN': return 'warning'
    default: return 'info'
  }
}

// 启动自动刷新
const startAutoRefresh = () => {
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      countdown.value = 10
    }
  }, 1000)

  refreshTimer = setInterval(() => {
    fetchData()
  }, 10000)
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

onMounted(() => {
  fetchData()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped lang="scss">
.gateway-container {
  padding: 20px;

  .status-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-actions {
        display: flex;
        align-items: center;
        gap: 12px;
      }
    }
  }

  .text-muted {
    color: #909399;
  }

  .error-text {
    color: #f56c6c;
    font-size: 12px;
  }
}
</style>
