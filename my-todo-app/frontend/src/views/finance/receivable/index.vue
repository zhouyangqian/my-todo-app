<!-- views/finance/receivable/index.vue - 应收账款管理页面 -->
<template>
  <div class="receivable-container">
    <!-- 搜索栏区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="结算状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="未结算" :value="0" />
            <el-option label="部分结算" :value="1" />
            <el-option label="已结算" :value="2" />
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

    <!-- 数据表格区域 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>应收账款列表</span>
          <div>
            <el-button type="warning" plain @click="handleShowOverdue">
              <el-icon><Warning /></el-icon>
              查看逾期
            </el-button>
            <el-button type="primary" @click="handleAdd" v-if="userStore.hasPermission('finance:receivable:create')">
              <el-icon><Plus /></el-icon>
              新增应收
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="customerName" label="客户名称" width="150" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="totalAmount" label="应收金额" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">¥ {{ row.totalAmount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="receivedAmount" label="已收金额" width="120">
          <template #default="{ row }">
            <span style="color: #67c23a;">¥ {{ row.receivedAmount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remainingAmount" label="剩余金额" width="120">
          <template #default="{ row }">
            <span>¥ {{ (row.totalAmount - row.receivedAmount)?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="结算状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="到期日期" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="220">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleReceive(row)" :disabled="row.status === 2" v-if="userStore.hasPermission('finance:receivable:receive')">
              收款
            </el-button>
            <el-button type="info" link @click="handleView(row)">详情</el-button>
            <el-button type="danger" link @click="handleDelete(row)" :disabled="row.status !== 0" v-if="userStore.hasPermission('finance:receivable:delete')">
              删除
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="formData.customerName" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="formData.orderNo" placeholder="请输入订单号" />
        </el-form-item>
        <el-form-item label="应收金额" prop="totalAmount">
          <el-input-number v-model="formData.totalAmount" :min="0" :precision="2" :step="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="到期日期" prop="dueDate">
          <el-date-picker
            v-model="formData.dueDate"
            type="date"
            placeholder="选择到期日期"
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

    <!-- 收款对话框 -->
    <el-dialog
      v-model="receiveDialogVisible"
      title="应收账款收款"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="receiveFormRef" :model="receiveForm" :rules="receiveRules" label-width="100px">
        <el-form-item label="应收金额">
          <el-input :value="`¥ ${currentRow?.totalAmount?.toFixed(2) || '0.00'}`" disabled />
        </el-form-item>
        <el-form-item label="已收金额">
          <el-input :value="`¥ ${currentRow?.receivedAmount?.toFixed(2) || '0.00'}`" disabled />
        </el-form-item>
        <el-form-item label="剩余金额">
          <el-input :value="`¥ ${(currentRow?.totalAmount - currentRow?.receivedAmount)?.toFixed(2) || '0.00'}`" disabled />
        </el-form-item>
        <el-form-item label="本次收款" prop="amount">
          <el-input-number v-model="receiveForm.amount" :min="0.01" :max="currentRow?.totalAmount - currentRow?.receivedAmount" :precision="2" :step="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="收款备注" prop="remark">
          <el-input v-model="receiveForm.remark" type="textarea" :rows="2" placeholder="请输入收款备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="receiveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReceiveSubmit" :loading="receiveLoading">确认收款</el-button>
      </template>
    </el-dialog>

    <!-- 逾期列表对话框 -->
    <el-dialog
      v-model="overdueDialogVisible"
      title="逾期应收账款"
      width="900px"
    >
      <el-table :data="overdueList" border stripe>
        <el-table-column prop="customerName" label="客户名称" width="150" />
        <el-table-column prop="totalAmount" label="应收金额" width="120">
          <template #default="{ row }">
            ¥ {{ row.totalAmount?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="remainingAmount" label="剩余金额" width="120">
          <template #default="{ row }">
            ¥ {{ (row.totalAmount - row.receivedAmount)?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="到期日期" width="120" />
        <el-table-column prop="overdueDays" label="逾期天数" width="100">
          <template #default="{ row }">
            <el-tag type="danger">{{ row.overdueDays }} 天</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Warning } from '@element-plus/icons-vue'
import { getReceivablePage, createReceivable, receivePayment, getOverdueReceivables, deleteReceivable } from '@/api/finance'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// ===== 搜索相关 =====
const searchForm = reactive({
  customerName: '',
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

// ===== 对话框相关 =====
const dialogVisible = ref(false)
const dialogTitle = ref('新增应收账款')
const formRef = ref()
const submitLoading = ref(false)

// ===== 收款对话框相关 =====
const receiveDialogVisible = ref(false)
const receiveFormRef = ref()
const receiveLoading = ref(false)
const currentRow = ref(null)
const receiveForm = reactive({
  amount: 0,
  remark: ''
})
const receiveRules = {
  amount: [{ required: true, message: '请输入收款金额', trigger: 'blur' }]
}

// ===== 逾期列表相关 =====
const overdueDialogVisible = ref(false)
const overdueList = ref([])

// ===== 表单数据 =====
const formData = reactive({
  customerName: '',
  orderNo: '',
  totalAmount: 0,
  dueDate: '',
  remark: ''
})

const formRules = {
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  totalAmount: [{ required: true, message: '请输入应收金额', trigger: 'blur' }],
  dueDate: [{ required: true, message: '请选择到期日期', trigger: 'change' }]
}

// ===== 状态辅助函数 =====
const getStatusType = (status) => {
  const types = { 0: 'danger', 1: 'warning', 2: 'success' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '未结算', 1: '部分结算', 2: '已结算' }
  return texts[status] || '未知'
}

// ===== 数据加载 =====
const loadData = async () => {
  loading.value = true
  try {
    const res = await getReceivablePage({
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// ===== 搜索相关 =====
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.customerName = ''
  searchForm.status = undefined
  handleSearch()
}

// ===== 表单操作 =====
const handleAdd = () => {
  dialogTitle.value = '新增应收账款'
  Object.assign(formData, {
    customerName: '',
    orderNo: '',
    totalAmount: 0,
    dueDate: '',
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
      await createReceivable(formData)
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

const handleView = (row) => {
  ElMessageBox.alert(`
    <p><strong>客户名称：</strong>${row.customerName}</p>
    <p><strong>订单号：</strong>${row.orderNo || '-'}</p>
    <p><strong>应收金额：</strong>¥ ${row.totalAmount?.toFixed(2) || '0.00'}</p>
    <p><strong>已收金额：</strong>¥ ${row.receivedAmount?.toFixed(2) || '0.00'}</p>
    <p><strong>剩余金额：</strong>¥ ${(row.totalAmount - row.receivedAmount)?.toFixed(2) || '0.00'}</p>
    <p><strong>到期日期：</strong>${row.dueDate || '-'}</p>
    <p><strong>备注：</strong>${row.remark || '-'}</p>
  `, '应收账款详情', {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '关闭'
  })
}

const handleReceive = (row) => {
  currentRow.value = row
  receiveForm.amount = row.totalAmount - row.receivedAmount
  receiveForm.remark = ''
  receiveDialogVisible.value = true
}

const handleReceiveSubmit = async () => {
  if (!receiveFormRef.value) return
  await receiveFormRef.value.validate(async (valid) => {
    if (!valid) return
    receiveLoading.value = true
    try {
      await receivePayment(currentRow.value.id, receiveForm.amount)
      ElMessage.success('收款成功')
      receiveDialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error('收款失败')
    } finally {
      receiveLoading.value = false
    }
  })
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该应收账款吗?', '提示', { type: 'warning' })
  try {
    await deleteReceivable(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleShowOverdue = async () => {
  try {
    const res = await getOverdueReceivables()
    overdueList.value = res || []
    if (overdueList.value.length === 0) {
      ElMessage.info('暂无逾期应收账款')
    } else {
      overdueDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取逾期数据失败')
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
})
</script>

<style scoped lang="scss">
.receivable-container {
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
