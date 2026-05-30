<!-- views/system/cache/index.vue - 缓存管理页面
  展示缓存统计信息，支持清除所有缓存和单个缓存
-->
<template>
  <div class="cache-container">
    <!-- 缓存统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">缓存命中次数</div>
            <div class="stat-value">{{ stats.hitCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">缓存未命中次数</div>
            <div class="stat-value">{{ stats.missCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">总请求次数</div>
            <div class="stat-value">{{ stats.totalRequests || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">缓存命中率</div>
            <div class="stat-value highlight">{{ stats.hitRate || '0.00%' }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作区域 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>缓存操作</span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="fetchStats" :loading="loading">
              刷新统计
            </el-button>
            <el-popconfirm title="确定要清除所有缓存吗？" @confirm="handleEvictAll">
              <template #reference>
                <el-button type="danger" size="small" :loading="evictLoading">
                  清除所有缓存
                </el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </template>

      <el-alert
        title="缓存说明"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 16px;"
      >
        网关仅缓存 GET 请求的 JSON 响应，默认 TTL 60秒。缓存 key 格式: gateway:cache:{tenantId}:{path}:{query}
      </el-alert>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="缓存Key前缀">gateway:cache:</el-descriptions-item>
        <el-descriptions-item label="默认TTL">60秒</el-descriptions-item>
        <el-descriptions-item label="缓存策略">仅缓存 GET 请求的 JSON 响应</el-descriptions-item>
        <el-descriptions-item label="存储引擎">Redis</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCacheStats, evictAllCache } from '@/api/gateway'

const stats = ref({})
const loading = ref(false)
const evictLoading = ref(false)

const fetchStats = async () => {
  loading.value = true
  try {
    const res = await getCacheStats()
    stats.value = res || {}
  } catch (error) {
    console.error('获取缓存统计失败', error)
  } finally {
    loading.value = false
  }
}

const handleEvictAll = async () => {
  evictLoading.value = true
  try {
    await evictAllCache()
    ElMessage.success('缓存已清除')
    await fetchStats()
  } catch (error) {
    ElMessage.error('清除缓存失败')
  } finally {
    evictLoading.value = false
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped lang="scss">
.cache-container {
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
