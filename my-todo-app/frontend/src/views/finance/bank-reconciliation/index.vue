<!-- views/finance/bank-reconciliation/index.vue - 银行对账管理页面 -->
<template>
  <div class="bank-reconciliation-container">
    <!-- 操作区域 -->
    <el-card class="action-card">
      <template #header>
        <div class="card-header">
          <span>银行对账</span>
          <div>
            <el-button type="primary" @click="handleImport">
              <el-icon><Upload /></el-icon>
              导入对账单
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="bankAccountId" label="银行账户ID" width="120" />
        <el-table-column label="对账期间" width="220">
          <template #default="{ row }">
            {{ row.periodStart }} ~ {{ row.periodEnd }}
          </template>
        </el-table-column>
        <el-table-column prop="totalBankAmount" label="银行总额" width="140">
          <template #default="{ row }">
            <span style="color: #409eff; font-weight: bold;">¥ {{ row.totalBankAmount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalSystemAmount" label="系统总额" width="140">
          <template #default="{ row }">
            <span style="color: #67c23a; font-weight: bold;">¥ {{ row.totalSystemAmount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="matchedCount" label="已匹配" width="90" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.matchedCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="unmatchedCount" label="未匹配" width="90" align="center">
          <template #default="{ row }">
            <el-tag type="danger" size="small">{{ row.unmatchedCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '已完成' : '进行中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleAutoMatch(row)">自动匹配</el-button>
            <el-button type="warning" link @click="handleViewUnmatched(row)">未匹配记录</el-button>
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

    <!-- 导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入银行对账单"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="importFormRef" :model="importForm" :rules="importRules" label-width="100px">
        <el-form-item label="银行账户" prop="bankAccountId">
          <el-select v-model="importForm.bankAccountId" placeholder="请选择银行账户" style="width: 100%;">
            <el-option v-for="item in bankAccountOptions" :key="item.id" :label="item.accountName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="对账单文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".csv"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持CSV格式，字段: 交易日期,金额,描述,参考号,交易类型</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImportSubmit" :loading="importLoading">导入</el-button>
      </template>
    </el-dialog>

    <!-- 未匹配记录弹窗 -->
    <el-dialog
      v-model="unmatchedDialogVisible"
      title="未匹配记录"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-table :data="unmatchedRecords" v-loading="unmatchedLoading" border stripe max-height="400">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="transactionDate" label="交易日期" width="120" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.transactionType === 'CREDIT' ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              {{ row.transactionType === 'CREDIT' ? '+' : '-' }}¥ {{ row.amount?.toFixed(2) || '0.00' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="transactionType" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.transactionType === 'CREDIT' ? 'success' : 'danger'" size="small">
              {{ row.transactionType === 'CREDIT' ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="180" />
        <el-table-column prop="referenceNo" label="参考号" width="120" />
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleManualMatch(row)">手动匹配</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 手动匹配对话框 -->
    <el-dialog
      v-model="manualMatchDialogVisible"
      title="手动匹配"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="manualMatchForm" label-width="100px">
        <el-form-item label="银行记录ID">
          <el-input :value="manualMatchForm.bankRecordId" disabled />
        </el-form-item>
        <el-form-item label="系统记录ID">
          <el-input v-model="manualMatchForm.systemRecordId" placeholder="请输入系统收支记录ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="manualMatchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleManualMatchSubmit" :loading="manualMatchLoading">确认匹配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import {
  importBankStatement,
  autoMatchReconciliation,
  manualMatchBankRecord,
  getBankReconciliationPage,
  getUnmatchedRecords,
  getAllBankAccounts
} from '@/api/finance'

// ===== 对账列表 =====
const tableData = ref([])
const loading = ref(false)
const pagination = reactive({ page: 1, size: 10, total: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getBankReconciliationPage({ page: pagination.page, size: pagination.size })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch {
    ElMessage.error('加载对账数据失败')
  } finally {
    loading.value = false
  }
}

// ===== 导入对账单 =====
const importDialogVisible = ref(false)
const importLoading = ref(false)
const importFormRef = ref()
const uploadRef = ref()
const bankAccountOptions = ref([])
const importForm = reactive({
  bankAccountId: null,
  file: null
})
const importRules = {
  bankAccountId: [{ required: true, message: '请选择银行账户', trigger: 'change' }]
}

const handleImport = async () => {
  // 加载银行账户选项
  try {
    const res = await getAllBankAccounts()
    bankAccountOptions.value = res || []
  } catch {
    bankAccountOptions.value = []
  }
  importForm.bankAccountId = null
  importForm.file = null
  importDialogVisible.value = true
}

const handleFileChange = (file) => {
  importForm.file = file.raw
}

const handleFileRemove = () => {
  importForm.file = null
}

const handleImportSubmit = async () => {
  if (!importFormRef.value) return
  await importFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (!importForm.file) {
      ElMessage.warning('请选择对账单文件')
      return
    }
    importLoading.value = true
    try {
      const formData = new FormData()
      formData.append('file', importForm.file)
      formData.append('bankAccountId', importForm.bankAccountId)
      await importBankStatement(formData)
      ElMessage.success('导入成功')
      importDialogVisible.value = false
      loadData()
    } catch {
      ElMessage.error('导入失败')
    } finally {
      importLoading.value = false
    }
  })
}

// ===== 自动匹配 =====
const handleAutoMatch = async (row) => {
  try {
    await ElMessageBox.confirm('确认执行自动匹配？', '提示', { type: 'info' })
    const res = await autoMatchReconciliation(row.id)
    ElMessage.success(`自动匹配完成，新增匹配 ${res || 0} 条记录`)
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('自动匹配失败')
    }
  }
}

// ===== 未匹配记录 =====
const unmatchedDialogVisible = ref(false)
const unmatchedLoading = ref(false)
const unmatchedRecords = ref([])
const currentReconciliationId = ref(null)

const handleViewUnmatched = async (row) => {
  currentReconciliationId.value = row.id
  unmatchedDialogVisible.value = true
  unmatchedLoading.value = true
  try {
    const res = await getUnmatchedRecords(row.id)
    unmatchedRecords.value = res || []
  } catch {
    ElMessage.error('加载未匹配记录失败')
  } finally {
    unmatchedLoading.value = false
  }
}

// ===== 手动匹配 =====
const manualMatchDialogVisible = ref(false)
const manualMatchLoading = ref(false)
const manualMatchForm = reactive({
  bankRecordId: null,
  systemRecordId: null
})

const handleManualMatch = (row) => {
  manualMatchForm.bankRecordId = row.id
  manualMatchForm.systemRecordId = null
  manualMatchDialogVisible.value = true
}

const handleManualMatchSubmit = async () => {
  if (!manualMatchForm.systemRecordId) {
    ElMessage.warning('请输入系统记录ID')
    return
  }
  manualMatchLoading.value = true
  try {
    await manualMatchBankRecord(manualMatchForm)
    ElMessage.success('匹配成功')
    manualMatchDialogVisible.value = false
    // 刷新未匹配记录
    if (currentReconciliationId.value) {
      const res = await getUnmatchedRecords(currentReconciliationId.value)
      unmatchedRecords.value = res || []
    }
    loadData()
  } catch {
    ElMessage.error('匹配失败')
  } finally {
    manualMatchLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.bank-reconciliation-container {
  padding: 20px;

  .action-card {
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
