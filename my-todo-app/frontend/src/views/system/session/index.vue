<!-- views/system/session/index.vue - 在线用户/会话管理页面 -->
<template>
  <div class="session-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>会话管理</span>
          <div>
            <el-button @click="handleCleanExpired" :loading="cleanLoading">
              清理过期会话
            </el-button>
            <el-button type="primary" @click="loadData">
              刷新列表
            </el-button>
            <el-switch
              v-model="autoRefresh"
              active-text="自动刷新"
              inactive-text=""
              style="margin-left: 12px;"
              @change="toggleAutoRefresh"
            />
          </div>
        </div>
      </template>

      <el-table
        :data="sessionList"
        v-loading="loading"
        border
        stripe
        style="width: 100%;"
      >
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="deviceInfo" label="设备信息" min-width="160" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="userAgent" label="浏览器" min-width="200" show-overflow-tooltip />
        <el-table-column prop="loginTime" label="登录时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.loginTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastActive" label="最后活跃" width="180">
          <template #default="{ row }">
            {{ formatTime(row.lastActive) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '在线' : '已踢出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="160">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="danger"
              link
              size="small"
              @click="handleKickSession(row)"
            >
              踢出
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              size="small"
              @click="handleKickUser(row)"
            >
              踢出所有
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOnlineSessions,
  kickSession,
  kickUserSessions,
  cleanExpiredSessions
} from '@/api/permission'

const loading = ref(false)
const cleanLoading = ref(false)
const sessionList = ref([])
const autoRefresh = ref(false)
let refreshTimer = null

/** 加载在线用户数据 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getOnlineSessions()
    sessionList.value = res || []
  } catch (error) {
    ElMessage.error('加载在线用户失败')
  } finally {
    loading.value = false
  }
}

/** 踢出指定会话 */
const handleKickSession = async (row) => {
  await ElMessageBox.confirm('确定要踢出该会话吗？', '提示', { type: 'warning' })
  try {
    await kickSession(row.id)
    ElMessage.success('已踢出该会话')
    loadData()
  } catch (error) {
    ElMessage.error('踢出会话失败')
  }
}

/** 踢出用户所有会话 */
const handleKickUser = async (row) => {
  await ElMessageBox.confirm(`确定要踢出用户 ${row.userId} 的所有会话吗？`, '提示', { type: 'warning' })
  try {
    await kickUserSessions(row.userId)
    ElMessage.success('已踢出该用户所有会话')
    loadData()
  } catch (error) {
    ElMessage.error('踢出用户会话失败')
  }
}

/** 清理过期会话 */
const handleCleanExpired = async () => {
  cleanLoading.value = true
  try {
    await cleanExpiredSessions()
    ElMessage.success('过期会话已清理')
    loadData()
  } catch (error) {
    ElMessage.error('清理过期会话失败')
  } finally {
    cleanLoading.value = false
  }
}

/** 格式化时间 */
const formatTime = (time) => {
  if (!time) return '-'
  if (typeof time === 'string') return time.replace('T', ' ')
  return time
}

/** 切换自动刷新 */
const toggleAutoRefresh = (val) => {
  if (val) {
    refreshTimer = setInterval(() => {
      loadData()
    }, 10000) // 10秒刷新
  } else {
    if (refreshTimer) {
      clearInterval(refreshTimer)
      refreshTimer = null
    }
  }
}

onMounted(() => {
  loadData()
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>

<style scoped lang="scss">
.session-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
