<!-- views/finance/record/index.vue - 收支记录管理页面 -->
<template>
  <div class="record-container">
    <!-- 搜索栏区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="收支类型">
          <el-select v-model="searchForm.recordType" placeholder="请选择" clearable>
            <el-option label="收入" :value="1" />
            <el-option label="支出" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="searchForm.bizType" placeholder="请选择" clearable>
            <el-option label="销售收款" :value="1" />
            <el-option label="采购付款" :value="2" />
            <el-option label="退款" :value="3" />
            <el-option label="其他收入" :value="4" />
            <el-option label="其他支出" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
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

    <!-- 数据表格区域 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>收支记录列表</span>
          <el-button type="primary" @click="handleAdd" v-if="userStore.hasPermission('finance:record:create')">
            <el-icon><Plus /></el-icon>
            新增记录
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="recordNo" label="单据编号" width="180" />
        <el-table-column prop="recordType" label="收支类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.recordType === 1 ? 'success' : 'danger'">
              {{ row.recordType === 1 ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bizType" label="业务类型" width="120">
          <template #default="{ row }">
            {{ getBizTypeText(row.bizType) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.recordType === 1 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              {{ row.recordType === 1 ? '+' : '-' }}¥ {{ row.amount?.toFixed(2) || '0.00' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="bankAccountName" label="银行账户" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="transDate" label="交易日期" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button type="success" link @click="handleApprove(row)" :disabled="row.status !== 0" v-if="userStore.hasPermission('finance:record:approve')">
              审核
            </el-button>
            <el-button type="danger" link @click="handleCancel(row)" :disabled="row.status === 2" v-if="userStore.hasPermission('finance:record:cancel')">
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
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

    <!-- 新增对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增收支记录"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="收支类型" prop="recordType">
          <el-radio-group v-model="formData.recordType">
            <el-radio :value="1">收入</el-radio>
            <el-radio :value="2">支出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="业务类型" prop="bizType">
          <el-select v-model="formData.bizType" placeholder="请选择业务类型" style="width: 100%;">
            <el-option label="销售收款" :value="1" />
            <el-option label="采购付款" :value="2" />
            <el-option label="退款" :value="3" />
            <el-option label="其他收入" :value="4" />
            <el-option label="其他支出" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="formData.amount" :min="0.01" :precision="2" :step="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="银行账户" prop="bankAccountId">
          <el-select v-model="formData.bankAccountId" placeholder="请选择银行账户" style="width: 100%;">
            <el-option
              v-for="account in bankAccounts"
              :key="account.id"
              :label="account.accountName"
              :value="account.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="交易日期" prop="transDate">
          <el-date-picker
            v-model="formData.transDate"
            type="date"
            placeholder="选择交易日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getPaymentRecordPage, createPaymentRecord, approveRecord, cancelRecord } from '@/api/finance'
import { getAllBankAccounts } from '@/api/finance'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// ===== 搜索相关 =====
const searchForm = reactive({
  recordType: undefined,
  bizType: undefined
})
const dateRange = ref([])

// ===== 分页相关 =====
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// ===== 表格相关 =====
const tableData = ref([])
const loading = ref(false)

// ===== 银行账户列表 =====
const bankAccounts = ref([])

// ===== 对话框相关 =====
const dialogVisible = ref(false)
const formRef = ref()
const submitLoading = ref(false)

// ===== 表单数据 =====
const formData = reactive({
  recordType: 1,
  bizType: undefined,
  amount: 0,
  bankAccountId: undefined,
  transDate: '',
  remark: ''
})

const formRules = {
  recordType: [{ required: true, message: '请选择收支类型', trigger: 'change' }],
  bizType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  bankAccountId: [{ required: true, message: '请选择银行账户', trigger: 'change' }],
  transDate: [{ required: true, message: '请选择交易日期', trigger: 'change' }]
}

// ===== 辅助函数 =====
const getBizTypeText = (bizType) => {
  const texts = { 1: '销售收款', 2: '采购付款', 3: '退款', 4: '其他收入', 5: '其他支出' }
  return texts[bizType] || '未知'
}

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待审核', 1: '已审核', 2: '已取消' }
  return texts[status] || '未知'
}

// ===== 数据加载 =====
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    if (dateRange.value?.length === 2) {
      params.startDate = dateRange.value[0] + ' 00:00:00'
      params.endDate = dateRange.value[1] + ' 23:59:59'
    }
    const res = await getPaymentRecordPage(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadBankAccounts = async () => {
  try {
    const res = await getAllBankAccounts()
    bankAccounts.value = res || []
  } catch (error) {
    console.error('加载银行账户失败:', error)
  }
}

// ===== 搜索相关 =====
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.recordType = undefined
  searchForm.bizType = undefined
  dateRange.value = []
  handleSearch()
}

// ===== 表单操作 =====
const handleAdd = () => {
  Object.assign(formData, {
    recordType: 1,
    bizType: undefined,
    amount: 0,
    bankAccountId: undefined,
    transDate: '',
    remark: ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await createPaymentRecord(formData)
      ElMessage.success('创建成功')
      dialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error('创建失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleApprove = async (row) => {
  await ElMessageBox.confirm('确定要审核该收支记录吗? 审核后将调整银行账户余额。', '提示', { type: 'warning' })
  try {
    await approveRecord(row.id)
    ElMessage.success('审核成功')
    loadData()
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm('确定要取消该收支记录吗?', '提示', { type: 'warning' })
  try {
    await cancelRecord(row.id)
    ElMessage.success('取消成功')
    loadData()
  } catch (error) {
    ElMessage.error('取消失败')
  }
}

// ===== 分页相关 =====
const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadData()
}

onMounted(() => {
  loadData()
  loadBankAccounts()
})
</script>

<style scoped lang="scss">
.record-container {
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
