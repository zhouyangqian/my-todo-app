<!-- views/system/monitor/index.vue - 实时监控仪表盘页面
  展示API指标汇总、最慢API、错误率最高API，支持自动刷新
-->
<template>
  <div class="monitor-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">总请求数</div>
            <div class="stat-value">{{ summary.totalRequests || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">平均响应时间</div>
            <div class="stat-value">{{ summary.avgResponseTime || '0' }}ms</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">错误率</div>
            <div class="stat-value" :class="{ 'error': parseFloat(summary.errorRate) > 5 }">
              {{ summary.errorRate || '0.00%' }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">活跃API数</div>
            <div class="stat-value highlight">{{ summary.activeApis || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最慢API -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>最慢API (Top 10)</span>
          <div class="header-actions">
            <el-tag type="info" size="small">自动刷新: {{ countdown }}s</el-tag>
            <el-button type="primary" size="small" @click="fetchData" :loading="loading">
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="slowApis" v-loading="loading" border stripe size="small">
        <el-table-column prop="apiPath" label="API路径" min-width="250" />
        <el-table-column prop="method" label="方法" width="100" />
        <el-table-column label="平均响应时间(ms)" width="160">
          <template #default="{ row }">
            <span v-if="row.totalRequests > 0">{{ (row.totalResponseTime / row.totalRequests).toFixed(1) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="maxResponseTime" label="最大响应时间(ms)" width="160" />
        <el-table-column prop="totalRequests" label="总请求数" width="120" />
      </el-table>

      <el-empty v-if="slowApis.length === 0 && !loading" description="暂无数据（请求经过网关后自动收集）" />
    </el-card>

    <!-- 错误率最高API -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>错误率最高API (Top 10)</span>
        </div>
      </template>

      <el-table :data="errorApis" v-loading="loading" border stripe size="small">
        <el-table-column prop="apiPath" label="API路径" min-width="250" />
        <el-table-column prop="method" label="方法" width="100" />
        <el-table-column label="错误率" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.totalRequests > 0"
              :type="errorRateTagType(row.errorCount / row.totalRequests * 100)">
              {{ (row.errorCount / row.totalRequests * 100).toFixed(2) }}%
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="errorCount" label="错误次数" width="120" />
        <el-table-column prop="totalRequests" label="总请求数" width="120" />
        <el-table-column prop="lastAccessTime" label="最后访问时间" width="180" />
      </el-table>

      <el-empty v-if="errorApis.length === 0 && !loading" description="暂无数据" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMetrics, getSlowApis, getErrorApis } from '@/api/gateway'

const summary = ref({})
const slowApis = ref([])
const errorApis = ref([])
const loading = ref(false)
const countdown = ref(10)
let refreshTimer = null
let countdownTimer = null

const fetchData = async () => {
  loading.value = true
  try {
    const [metricsRes, slowRes, errorRes] = await Promise.all([
      getMetrics(),
      getSlowApis(10),
      getErrorApis(10)
    ])
    summary.value = metricsRes?.summary || {}
    slowApis.value = slowRes || []
    errorApis.value = errorRes || []
  } catch (error) {
    ElMessage.error('获取监控数据失败')
  } finally {
    loading.value = false
    countdown.value = 10
  }
}

const errorRateTagType = (rate) => {
  if (rate > 10) return 'danger'
  if (rate > 5) return 'warning'
  return 'info'
}

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
.monitor-container {
  padding: 20px;

  .stats-row {
    .stat-item {
      text-align: center;
      padding: 10px 0;

      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: #303133;

        &.highlight {
          color: #409eff;
        }

        &.error {
          color: #f56c6c;
        }
      }
    }
  }

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
</style>
