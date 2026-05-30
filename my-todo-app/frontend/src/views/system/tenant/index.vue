<!-- views/system/tenant/index.vue - 租户管理页面
  提供租户列表、配额编辑、资源使用查看等功能
-->
<template>
  <div class="tenant-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="租户名称">
          <el-input v-model="searchForm.tenantName" placeholder="请输入租户名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
            <el-option label="过期" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 租户列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>租户列表</span>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="tenantName" label="租户名称" width="160" />
        <el-table-column prop="tenantCode" label="租户编码" width="140" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userLimit" label="用户限制" width="100" align="center" />
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="contactEmail" label="联系邮箱" width="180" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEditQuota(row)">配额</el-button>
            <el-button type="success" link @click="handleViewUsage(row)">资源</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        class="pagination"
      />
    </el-card>

    <!-- 配额编辑对话框 -->
    <el-dialog
      v-model="quotaDialogVisible"
      title="编辑配额"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="quotaFormRef" :model="quotaForm" label-width="140px">
        <el-form-item label="租户">
          <el-input :value="currentTenant?.tenantName" disabled />
        </el-form-item>
        <el-form-item label="最大用户数">
          <el-input-number v-model="quotaForm.maxUsers" :min="1" :max="10000" />
        </el-form-item>
        <el-form-item label="最大存储空间(MB)">
          <el-input-number v-model="quotaForm.maxStorageMb" :min="100" :max="1048576" />
        </el-form-item>
        <el-form-item label="每日API调用上限">
          <el-input-number v-model="quotaForm.maxApiCallsPerDay" :min="100" :max="10000000" />
        </el-form-item>
        <el-form-item label="最大并发请求数">
          <el-input-number v-model="quotaForm.maxConcurrentRequests" :min="10" :max="100000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="quotaDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveQuota" :loading="quotaLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 资源使用弹窗 -->
    <el-dialog
      v-model="usageDialogVisible"
      title="资源使用情况"
      width="500px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="租户">{{ currentTenant?.tenantName }}</el-descriptions-item>
        <el-descriptions-item label="用户数量">{{ usageData.userCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="已用存储(MB)">{{ usageData.storageUsedMb || 0 }}</el-descriptions-item>
        <el-descriptions-item label="今日API调用">{{ usageData.apiCallsToday || 0 }}</el-descriptions-item>
        <el-descriptions-item label="当前并发请求">{{ usageData.concurrentRequests || 0 }}</el-descriptions-item>
        <el-descriptions-item label="记录日期">{{ usageData.recordDate }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getTenantList, getTenantQuota, updateTenantQuota, getTenantUsage } from '@/api/auth'

// ===== 搜索相关 =====
const searchForm = reactive({
  tenantName: '',
  status: undefined
})

// ===== 分页相关 =====
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// ===== 表格相关 =====
const tableData = ref([])
const loading = ref(false)

// ===== 配额对话框 =====
const quotaDialogVisible = ref(false)
const quotaLoading = ref(false)
const quotaFormRef = ref()
const currentTenant = ref(null)
const quotaForm = reactive({
  maxUsers: 5,
  maxStorageMb: 1024,
  maxApiCallsPerDay: 10000,
  maxConcurrentRequests: 100
})

// ===== 资源使用弹窗 =====
const usageDialogVisible = ref(false)
const usageData = ref({})

/**
 * 加载租户列表
 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getTenantList({
      page: pagination.page,
      size: pagination.size,
      tenantName: searchForm.tenantName || undefined,
      status: searchForm.status
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error('加载租户列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 状态标签类型
 */
const statusTagType = (status) => {
  switch (status) {
    case 1: return 'success'
    case 0: return 'danger'
    case 2: return 'warning'
    default: return 'info'
  }
}

/**
 * 状态文本
 */
const statusText = (status) => {
  switch (status) {
    case 1: return '正常'
    case 0: return '禁用'
    case 2: return '过期'
    default: return '未知'
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

/**
 * 重置
 */
const handleReset = () => {
  searchForm.tenantName = ''
  searchForm.status = undefined
  handleSearch()
}

/**
 * 编辑配额
 */
const handleEditQuota = async (row) => {
  currentTenant.value = row
  try {
    const quota = await getTenantQuota(row.id)
    quotaForm.maxUsers = quota.maxUsers
    quotaForm.maxStorageMb = quota.maxStorageMb
    quotaForm.maxApiCallsPerDay = quota.maxApiCallsPerDay
    quotaForm.maxConcurrentRequests = quota.maxConcurrentRequests
  } catch (error) {
    // 配额不存在时使用默认值
    quotaForm.maxUsers = 5
    quotaForm.maxStorageMb = 1024
    quotaForm.maxApiCallsPerDay = 10000
    quotaForm.maxConcurrentRequests = 100
  }
  quotaDialogVisible.value = true
}

/**
 * 保存配额
 */
const handleSaveQuota = async () => {
  quotaLoading.value = true
  try {
    await updateTenantQuota(currentTenant.value.id, {
      maxUsers: quotaForm.maxUsers,
      maxStorageMb: quotaForm.maxStorageMb,
      maxApiCallsPerDay: quotaForm.maxApiCallsPerDay,
      maxConcurrentRequests: quotaForm.maxConcurrentRequests
    })
    ElMessage.success('配额更新成功')
    quotaDialogVisible.value = false
  } catch (error) {
    ElMessage.error('配额更新失败')
  } finally {
    quotaLoading.value = false
  }
}

/**
 * 查看资源使用
 */
const handleViewUsage = async (row) => {
  currentTenant.value = row
  try {
    const usage = await getTenantUsage(row.id)
    usageData.value = usage || {}
  } catch (error) {
    usageData.value = {}
  }
  usageDialogVisible.value = true
}

/**
 * 每页条数变化
 */
const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

/**
 * 页码变化
 */
const handlePageChange = (page) => {
  pagination.page = page
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.tenant-container {
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

    .pagination {
      margin-top: 20px;
      justify-content: flex-end;
    }
  }
}
</style>
