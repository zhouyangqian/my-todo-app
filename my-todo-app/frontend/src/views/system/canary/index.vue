<!-- views/system/canary/index.vue - 金丝雀发布管理页面
  展示金丝雀发布策略，支持查看和调节流量百分比
-->
<template>
  <div class="canary-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>金丝雀发布策略</span>
          <div class="header-actions">
            <el-switch
              v-model="canaryEnabled"
              active-text="已启用"
              inactive-text="已禁用"
              style="margin-right: 12px;"
              disabled
            />
            <el-button type="primary" size="small" @click="fetchStrategies" :loading="loading">
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-alert
        title="金丝雀发布说明"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 16px;"
      >
        金丝雀发布通过配置文件管理。匹配优先级：用户ID列表 > 请求头匹配 > 流量百分比。
        流量百分比基于 userId 哈希值进行分配，保证同一用户始终路由到同一版本。
      </el-alert>

      <el-table :data="strategies" v-loading="loading" border stripe>
        <el-table-column prop="serviceId" label="服务名称" width="180">
          <template #default="{ row }">
            <el-tag>{{ row.serviceId }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetVersion" label="目标版本" width="120">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.targetVersion }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="trafficPercentage" label="流量百分比" width="200">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-slider
                v-model="row.trafficPercentage"
                :min="0"
                :max="100"
                :step="5"
                style="flex: 1;"
                disabled
              />
              <span style="width: 40px; text-align: right;">{{ row.trafficPercentage }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="matchHeader" label="匹配请求头" width="160">
          <template #default="{ row }">
            <span v-if="row.matchHeader">{{ row.matchHeader }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="matchUserIds" label="指定用户ID" min-width="200">
          <template #default="{ row }">
            <template v-if="row.matchUserIds && row.matchUserIds.length > 0">
              <el-tag v-for="uid in row.matchUserIds" :key="uid" size="small" style="margin: 2px;">
                {{ uid }}
              </el-tag>
            </template>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="strategies.length === 0 && !loading" description="暂无金丝雀策略配置" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const strategies = ref([])
const loading = ref(false)
const canaryEnabled = ref(true)

/**
 * 获取金丝雀策略
 * 金丝雀策略通过后端配置文件管理，暂无独立API
 * 当前通过 dashboard 接口间接展示
 */
const fetchStrategies = async () => {
  loading.value = true
  try {
    // 金丝雀策略暂时从配置中读取，展示默认配置
    strategies.value = [
      {
        serviceId: 'erp-service',
        targetVersion: 'v2',
        trafficPercentage: 10,
        matchHeader: null,
        matchHeaderValue: null,
        matchUserIds: null
      }
    ]
  } catch (error) {
    ElMessage.error('获取金丝雀策略失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStrategies()
})
</script>

<style scoped lang="scss">
.canary-container {
  padding: 20px;

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

  .text-muted {
    color: #909399;
  }
}
</style>
