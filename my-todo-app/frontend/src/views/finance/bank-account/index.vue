<!-- views/finance/bank-account/index.vue - 银行账户管理页面 -->
<template>
  <div class="bank-account-container">
    <!-- 搜索栏区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="账户名称">
          <el-input v-model="searchForm.accountName" placeholder="请输入账户名称" clearable />
        </el-form-item>
        <el-form-item label="账户类型">
          <el-select v-model="searchForm.accountType" placeholder="请选择" clearable>
            <el-option label="现金账户" :value="1" />
            <el-option label="银行账户" :value="2" />
            <el-option label="支付宝" :value="3" />
            <el-option label="微信" :value="4" />
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
          <span>银行账户列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增账户
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="accountName" label="账户名称" width="150" />
        <el-table-column prop="accountType" label="账户类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getAccountTypeText(row.accountType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="accountNo" label="账号" width="200" />
        <el-table-column prop="balance" label="余额" width="150">
          <template #default="{ row }">
            <span :style="{ color: row.balance >= 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              ¥ {{ row.balance?.toFixed(2) || '0.00' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="currency" label="币种" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="info" link @click="handleAdjust(row)">调账</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
        <el-form-item label="账户名称" prop="accountName">
          <el-input v-model="formData.accountName" placeholder="请输入账户名称" />
        </el-form-item>
        <el-form-item label="账户类型" prop="accountType">
          <el-select v-model="formData.accountType" placeholder="请选择账户类型" style="width: 100%;">
            <el-option label="现金账户" :value="1" />
            <el-option label="银行账户" :value="2" />
            <el-option label="支付宝" :value="3" />
            <el-option label="微信" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="账号" prop="accountNo">
          <el-input v-model="formData.accountNo" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="开户行" prop="bankName">
          <el-input v-model="formData.bankName" placeholder="请输入开户行名称" />
        </el-form-item>
        <el-form-item label="币种" prop="currency">
          <el-select v-model="formData.currency" placeholder="请选择币种" style="width: 100%;">
            <el-option label="CNY - 人民币" value="CNY" />
            <el-option label="USD - 美元" value="USD" />
            <el-option label="EUR - 欧元" value="EUR" />
            <el-option label="HKD - 港币" value="HKD" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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

    <!-- 调账对话框 -->
    <el-dialog
      v-model="adjustDialogVisible"
      title="余额调整"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="100px">
        <el-form-item label="当前余额">
          <el-input :value="`¥ ${currentRow?.balance?.toFixed(2) || '0.00'}`" disabled />
        </el-form-item>
        <el-form-item label="调整类型" prop="adjustType">
          <el-radio-group v-model="adjustForm.adjustType">
            <el-radio :value="1">增加</el-radio>
            <el-radio :value="2">减少</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调整金额" prop="amount">
          <el-input-number v-model="adjustForm.amount" :min="0.01" :precision="2" :step="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input v-model="adjustForm.reason" type="textarea" :rows="3" placeholder="请输入调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdjustSubmit" :loading="adjustLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getBankAccountPage, createBankAccount, updateBankAccount, deleteBankAccount } from '@/api/finance'

// ===== 搜索相关 =====
const searchForm = reactive({
  accountName: '',
  accountType: undefined
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
const dialogTitle = ref('新增银行账户')
const formRef = ref()
const submitLoading = ref(false)

// ===== 调账对话框相关 =====
const adjustDialogVisible = ref(false)
const adjustFormRef = ref()
const adjustLoading = ref(false)
const currentRow = ref(null)
const adjustForm = reactive({
  adjustType: 1,
  amount: 0,
  reason: ''
})
const adjustRules = {
  adjustType: [{ required: true, message: '请选择调整类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入调整金额', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
}

// ===== 表单数据 =====
const formData = reactive({
  accountName: '',
  accountType: 2,
  accountNo: '',
  bankName: '',
  currency: 'CNY',
  status: 1,
  remark: ''
})

const formRules = {
  accountName: [{ required: true, message: '请输入账户名称', trigger: 'blur' }],
  accountType: [{ required: true, message: '请选择账户类型', trigger: 'change' }],
  accountNo: [{ required: true, message: '请输入账号', trigger: 'blur' }]
}

// ===== 辅助函数 =====
const getAccountTypeText = (type) => {
  const texts = { 1: '现金账户', 2: '银行账户', 3: '支付宝', 4: '微信' }
  return texts[type] || '未知'
}

// ===== 数据加载 =====
const loadData = async () => {
  loading.value = true
  try {
    const res = await getBankAccountPage({
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
  searchForm.accountName = ''
  searchForm.accountType = undefined
  handleSearch()
}

// ===== 表单操作 =====
const handleAdd = () => {
  dialogTitle.value = '新增银行账户'
  Object.assign(formData, {
    accountName: '',
    accountType: 2,
    accountNo: '',
    bankName: '',
    currency: 'CNY',
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑银行账户'
  Object.assign(formData, row)
  formData.id = String(row.id)  // 确保id是字符串类型
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (formData.id) {
        await updateBankAccount(formData.id, formData)
        ElMessage.success('更新成功')
      } else {
        await createBankAccount(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error(formData.id ? '更新失败' : '创建失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleAdjust = (row) => {
  currentRow.value = row
  adjustForm.adjustType = 1
  adjustForm.amount = 0
  adjustForm.reason = ''
  adjustDialogVisible.value = true
}

const handleAdjustSubmit = async () => {
  if (!adjustFormRef.value) return
  await adjustFormRef.value.validate(async (valid) => {
    if (!valid) return
    adjustLoading.value = true
    try {
      // await adjustBalance(currentRow.value.id, adjustForm)
      ElMessage.success('调账成功')
      adjustDialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error('调账失败')
    } finally {
      adjustLoading.value = false
    }
  })
}

const handleDelete = async (row) => {
  if (row.balance !== 0) {
    ElMessage.warning('只能删除余额为零的账户')
    return
  }
  await ElMessageBox.confirm('确定要删除该银行账户吗?', '提示', { type: 'warning' })
  try {
    await deleteBankAccount(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
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
.bank-account-container {
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
