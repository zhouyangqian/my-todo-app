<!-- views/system/blacklist/index.vue - 黑名单管理页面 -->
<template>
  <div class="blacklist-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>黑名单管理</span>
          <div>
            <el-button type="primary" @click="showAddDialog">加入黑名单</el-button>
            <el-button @click="loadData">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchStatus" placeholder="全部" clearable @change="loadData">
            <el-option label="生效中" :value="1" />
            <el-option label="已解除" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%;">
        <el-table-column prop="userId" label="被拉黑用户ID" width="130" />
        <el-table-column prop="reason" label="拉黑原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="operatorId" label="操作人ID" width="130" />
        <el-table-column prop="operatorType" label="操作人类型" width="150" />
        <el-table-column prop="addedAt" label="加入时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.addedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'danger' : 'info'">
              {{ row.status === 1 ? '生效中' : '已解除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              size="small"
              @click="handleRemove(row)"
            >
              解除黑名单
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 加入黑名单对话框 -->
    <el-dialog v-model="addDialogVisible" title="加入黑名单" width="500px" destroy-on-close>
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="100px">
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="addForm.userId" :min="1" placeholder="请输入用户ID" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="拉黑原因" prop="reason">
          <el-input
            v-model="addForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入拉黑原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="操作人类型">
          <el-select v-model="addForm.operatorType" placeholder="请选择">
            <el-option label="租户管理员" value="TENANT_ADMIN" />
            <el-option label="系统管理员" value="SYSTEM_ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getBlacklistPage,
  addToBlacklist,
  removeFromBlacklist
} from '@/api/permission'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchStatus = ref(null)
const addDialogVisible = ref(false)
const addFormRef = ref(null)

const addForm = ref({
  userId: null,
  reason: '',
  operatorType: 'TENANT_ADMIN'
})

const addRules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }]
}

/** 加载黑名单列表 */
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchStatus.value !== null && searchStatus.value !== '') {
      params.status = searchStatus.value
    }
    const res = await getBlacklistPage(params)
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } catch (error) {
    ElMessage.error('加载黑名单列表失败')
  } finally {
    loading.value = false
  }
}

/** 显示加入黑名单对话框 */
const showAddDialog = () => {
  addForm.value = { userId: null, reason: '', operatorType: 'TENANT_ADMIN' }
  addDialogVisible.value = true
}

/** 加入黑名单 */
const handleAdd = async () => {
  const valid = await addFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    await addToBlacklist(addForm.value)
    ElMessage.success('已加入黑名单')
    addDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error?.message || '加入黑名单失败')
  } finally {
    submitLoading.value = false
  }
}

/** 解除黑名单 */
const handleRemove = async (row) => {
  await ElMessageBox.confirm(
    `确定要解除用户 ${row.userId} 的黑名单吗？`,
    '提示',
    { type: 'warning' }
  )
  try {
    await removeFromBlacklist(row.id)
    ElMessage.success('已解除黑名单')
    loadData()
  } catch (error) {
    ElMessage.error('解除黑名单失败')
  }
}

/** 格式化时间 */
const formatTime = (time) => {
  if (!time) return '-'
  if (typeof time === 'string') return time.replace('T', ' ')
  return time
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.blacklist-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 16px;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
