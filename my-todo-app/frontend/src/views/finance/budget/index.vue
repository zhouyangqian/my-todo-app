<!-- views/finance/budget/index.vue - 预算管理页面 -->
<template>
  <div class="budget-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="预算类型">
          <el-select v-model="searchForm.budgetType" placeholder="请选择" clearable>
            <el-option label="部门预算" value="DEPARTMENT" />
            <el-option label="项目预算" value="PROJECT" />
            <el-option label="总体预算" value="OVERALL" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="已审批" :value="1" />
            <el-option label="执行中" :value="2" />
            <el-option label="已结束" :value="3" />
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

    <!-- 预算列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>预算管理</span>
          <el-button type="primary" @click="handleAdd" v-if="userStore.hasPermission('finance:budget:create')">
            <el-icon><Plus /></el-icon>
            新增预算
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="budgetName" label="预算名称" width="160" />
        <el-table-column prop="budgetType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getBudgetTypeTag(row.budgetType)" size="small">{{ getBudgetTypeText(row.budgetType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="期间" width="200">
          <template #default="{ row }">
            {{ row.periodStart }} ~ {{ row.periodEnd }}
          </template>
        </el-table-column>
        <el-table-column prop="budgetAmount" label="预算金额" width="130">
          <template #default="{ row }">
            <span style="font-weight: bold;">¥ {{ row.budgetAmount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="usedAmount" label="已用金额" width="130">
          <template #default="{ row }">
            <span style="color: #e6a23c;">¥ {{ row.usedAmount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remainingAmount" label="剩余金额" width="130">
          <template #default="{ row }">
            <span :style="{ color: row.remainingAmount >= 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              ¥ {{ row.remainingAmount?.toFixed(2) || '0.00' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="controlLevel" label="控制级别" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getControlLevelTag(row.controlLevel)" size="small">{{ getControlLevelText(row.controlLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="260">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)" v-if="row.status <= 1">编辑</el-button>
            <el-button type="success" link @click="handleApprove(row)" v-if="row.status === 0">审批</el-button>
            <el-button type="info" link @click="handleViewExecution(row)">执行情况</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-if="row.status === 0">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="(s) => { pagination.size = s; loadData() }"
        @current-change="(p) => { pagination.page = p; loadData() }"
        class="pagination"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="预算名称" prop="budgetName">
          <el-input v-model="formData.budgetName" placeholder="请输入预算名称" />
        </el-form-item>
        <el-form-item label="预算类型" prop="budgetType">
          <el-select v-model="formData.budgetType" placeholder="请选择预算类型" style="width: 100%;">
            <el-option label="部门预算" value="DEPARTMENT" />
            <el-option label="项目预算" value="PROJECT" />
            <el-option label="总体预算" value="OVERALL" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标ID" prop="targetId" v-if="formData.budgetType !== 'OVERALL'">
          <el-input v-model="formData.targetId" placeholder="请输入部门ID或项目ID" />
        </el-form-item>
        <el-form-item label="周期类型" prop="periodType">
          <el-select v-model="formData.periodType" placeholder="请选择周期类型" style="width: 100%;">
            <el-option label="月度" value="MONTHLY" />
            <el-option label="季度" value="QUARTERLY" />
            <el-option label="年度" value="YEARLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="periodStart">
          <el-date-picker v-model="formData.periodStart" type="date" placeholder="选择开始日期" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="结束日期" prop="periodEnd">
          <el-date-picker v-model="formData.periodEnd" type="date" placeholder="选择结束日期" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="预算金额" prop="budgetAmount">
          <el-input-number v-model="formData.budgetAmount" :min="0" :precision="2" :step="1000" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="控制级别" prop="controlLevel">
          <el-select v-model="formData.controlLevel" placeholder="请选择控制级别" style="width: 100%;">
            <el-option label="强制控制" value="FORCE" />
            <el-option label="警告" value="WARN" />
            <el-option label="仅记录" value="LOG" />
          </el-select>
        </el-form-item>
        <el-form-item label="警告阈值(%)" prop="warningThreshold">
          <el-input-number v-model="formData.warningThreshold" :min="1" :max="100" :precision="2" :step="10" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 执行情况弹窗 -->
    <el-dialog
      v-model="executionDialogVisible"
      title="预算执行情况"
      width="600px"
    >
      <div v-if="executionData" class="execution-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="预算名称">{{ executionData.budgetName }}</el-descriptions-item>
          <el-descriptions-item label="预算类型">{{ getBudgetTypeText(executionData.budgetType) }}</el-descriptions-item>
          <el-descriptions-item label="预算金额">¥ {{ executionData.budgetAmount?.toFixed(2) || '0.00' }}</el-descriptions-item>
          <el-descriptions-item label="已用金额">¥ {{ executionData.usedAmount?.toFixed(2) || '0.00' }}</el-descriptions-item>
          <el-descriptions-item label="冻结金额">¥ {{ executionData.frozenAmount?.toFixed(2) || '0.00' }}</el-descriptions-item>
          <el-descriptions-item label="剩余金额">
            <span :style="{ color: executionData.remainingAmount >= 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              ¥ {{ executionData.remainingAmount?.toFixed(2) || '0.00' }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="控制级别">{{ getControlLevelText(executionData.controlLevel) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ getStatusText(executionData.status) }}</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 20px;">
          <div style="margin-bottom: 8px; display: flex; justify-content: space-between;">
            <span>预算使用进度</span>
            <span :style="{ color: executionData.usageRate >= 80 ? '#f56c6c' : '#67c23a' }">
              {{ executionData.usageRate?.toFixed(1) || 0 }}%
            </span>
          </div>
          <el-progress
            :percentage="Math.min(executionData.usageRate || 0, 100)"
            :color="getProgressColor(executionData.usageRate)"
            :stroke-width="20"
            :text-inside="true"
          />
          <div style="margin-top: 8px; color: #909399; font-size: 12px;">
            警告阈值: {{ executionData.warningThreshold }}%
            <span v-if="executionData.isOverThreshold" style="color: #e6a23c; margin-left: 10px;">已达警告阈值</span>
            <span v-if="executionData.isOverBudget" style="color: #f56c6c; margin-left: 10px;">已超预算</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getBudgetPage,
  createBudget,
  updateBudget,
  approveBudget,
  deleteBudget,
  getBudgetExecution
} from '@/api/finance'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// ===== 辅助函数 =====
const getBudgetTypeText = (type) => {
  const texts = { DEPARTMENT: '部门预算', PROJECT: '项目预算', OVERALL: '总体预算' }
  return texts[type] || '未知'
}
const getBudgetTypeTag = (type) => {
  const tags = { DEPARTMENT: 'primary', PROJECT: 'warning', OVERALL: 'success' }
  return tags[type] || 'info'
}
const getControlLevelText = (level) => {
  const texts = { FORCE: '强制控制', WARN: '警告', LOG: '仅记录' }
  return texts[level] || '未知'
}
const getControlLevelTag = (level) => {
  const tags = { FORCE: 'danger', WARN: 'warning', LOG: 'info' }
  return tags[level] || 'info'
}
const getStatusText = (status) => {
  const texts = { 0: '草稿', 1: '已审批', 2: '执行中', 3: '已结束' }
  return texts[status] || '未知'
}
const getStatusTag = (status) => {
  const tags = { 0: 'info', 1: 'success', 2: 'primary', 3: '' }
  return tags[status] || 'info'
}
const getProgressColor = (rate) => {
  if (!rate) return '#67c23a'
  if (rate >= 100) return '#f56c6c'
  if (rate >= 80) return '#e6a23c'
  return '#67c23a'
}

// ===== 搜索 =====
const searchForm = reactive({ budgetType: undefined, status: undefined })
const handleSearch = () => { pagination.page = 1; loadData() }
const handleReset = () => { searchForm.budgetType = undefined; searchForm.status = undefined; handleSearch() }

// ===== 列表 =====
const tableData = ref([])
const loading = ref(false)
const pagination = reactive({ page: 1, size: 10, total: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getBudgetPage({
      page: pagination.page,
      size: pagination.size,
      budgetType: searchForm.budgetType || undefined,
      status: searchForm.status !== undefined && searchForm.status !== null ? searchForm.status : undefined
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch {
    ElMessage.error('加载预算数据失败')
  } finally {
    loading.value = false
  }
}

// ===== 对话框 =====
const dialogVisible = ref(false)
const dialogTitle = ref('新增预算')
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref()
const submitLoading = ref(false)

const formData = reactive({
  budgetName: '',
  budgetType: 'DEPARTMENT',
  targetId: null,
  periodType: 'MONTHLY',
  periodStart: '',
  periodEnd: '',
  budgetAmount: 0,
  controlLevel: 'WARN',
  warningThreshold: 80
})

const formRules = {
  budgetName: [{ required: true, message: '请输入预算名称', trigger: 'blur' }],
  budgetType: [{ required: true, message: '请选择预算类型', trigger: 'change' }],
  periodType: [{ required: true, message: '请选择周期类型', trigger: 'change' }],
  periodStart: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  periodEnd: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  budgetAmount: [{ required: true, message: '请输入预算金额', trigger: 'blur' }]
}

const resetForm = () => {
  Object.assign(formData, {
    budgetName: '', budgetType: 'DEPARTMENT', targetId: null,
    periodType: 'MONTHLY', periodStart: '', periodEnd: '',
    budgetAmount: 0, controlLevel: 'WARN', warningThreshold: 80
  })
}

const handleAdd = () => {
  dialogTitle.value = '新增预算'
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑预算'
  isEdit.value = true
  editId.value = row.id
  Object.assign(formData, {
    budgetName: row.budgetName,
    budgetType: row.budgetType,
    targetId: row.targetId,
    periodType: row.periodType,
    periodStart: row.periodStart,
    periodEnd: row.periodEnd,
    budgetAmount: row.budgetAmount,
    controlLevel: row.controlLevel,
    warningThreshold: row.warningThreshold
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateBudget(editId.value, formData)
        ElMessage.success('更新成功')
      } else {
        await createBudget(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
    } catch {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// ===== 审批 =====
const handleApprove = async (row) => {
  await ElMessageBox.confirm('确认审批该预算？审批后将进入执行阶段。', '提示', { type: 'info' })
  try {
    await approveBudget(row.id)
    ElMessage.success('审批成功')
    loadData()
  } catch {
    ElMessage.error('审批失败')
  }
}

// ===== 删除 =====
const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该预算吗？', '提示', { type: 'warning' })
  try {
    await deleteBudget(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    ElMessage.error('删除失败')
  }
}

// ===== 执行情况 =====
const executionDialogVisible = ref(false)
const executionData = ref(null)

const handleViewExecution = async (row) => {
  try {
    const res = await getBudgetExecution(row.id)
    executionData.value = res || {}
    executionDialogVisible.value = true
  } catch {
    ElMessage.error('加载执行情况失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.budget-container {
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

  .execution-content {
    padding: 10px 0;
  }
}
</style>
