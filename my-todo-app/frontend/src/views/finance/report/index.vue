<!-- views/finance/report/index.vue - 财务报表页面 -->
<template>
  <div class="report-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 资产负债表 -->
      <el-tab-pane label="资产负债表" name="balance">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>资产负债表</span>
              <div>
                <el-date-picker
                  v-model="balanceDate"
                  type="month"
                  placeholder="选择期间"
                  format="YYYY年MM月"
                  value-format="YYYY-MM"
                  style="width: 180px; margin-right: 10px;"
                />
                <el-button type="primary" @click="loadBalanceSheet">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button type="success" @click="handleExport('balance')">
                  <el-icon><Download /></el-icon>
                  导出
                </el-button>
              </div>
            </div>
          </template>

          <el-table
            :data="balanceData"
            v-loading="balanceLoading"
            border
            stripe
            row-key="id"
            :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          >
            <el-table-column prop="itemName" label="项目" min-width="250" />
            <el-table-column prop="lineNo" label="行次" width="80" align="center" />
            <el-table-column prop="currentAmount" label="本期金额" width="180" align="right">
              <template #default="{ row }">
                <span v-if="row.currentAmount != null" :style="{ fontWeight: row.isBold ? 'bold' : 'normal' }">
                  ¥ {{ Number(row.currentAmount).toFixed(2) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="previousAmount" label="上期金额" width="180" align="right">
              <template #default="{ row }">
                <span v-if="row.previousAmount != null" :style="{ fontWeight: row.isBold ? 'bold' : 'normal' }">
                  ¥ {{ Number(row.previousAmount).toFixed(2) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 利润表 -->
      <el-tab-pane label="利润表" name="income">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>利润表</span>
              <div>
                <el-date-picker
                  v-model="incomePeriod"
                  type="month"
                  placeholder="选择期间"
                  format="YYYY年MM月"
                  value-format="YYYY-MM"
                  style="width: 180px; margin-right: 10px;"
                />
                <el-button type="primary" @click="loadIncomeStatement">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button type="success" @click="handleExport('income')">
                  <el-icon><Download /></el-icon>
                  导出
                </el-button>
              </div>
            </div>
          </template>

          <el-table
            :data="incomeData"
            v-loading="incomeLoading"
            border
            stripe
            row-key="id"
            :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          >
            <el-table-column prop="itemName" label="项目" min-width="250" />
            <el-table-column prop="lineNo" label="行次" width="80" align="center" />
            <el-table-column prop="currentAmount" label="本期金额" width="180" align="right">
              <template #default="{ row }">
                <span v-if="row.currentAmount != null" :style="{ fontWeight: row.isBold ? 'bold' : 'normal' }">
                  ¥ {{ Number(row.currentAmount).toFixed(2) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="previousAmount" label="上期金额" width="180" align="right">
              <template #default="{ row }">
                <span v-if="row.previousAmount != null" :style="{ fontWeight: row.isBold ? 'bold' : 'normal' }">
                  ¥ {{ Number(row.previousAmount).toFixed(2) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 现金流量表 -->
      <el-tab-pane label="现金流量表" name="cashflow">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>现金流量表</span>
              <div>
                <el-date-picker
                  v-model="cashFlowPeriod"
                  type="month"
                  placeholder="选择期间"
                  format="YYYY年MM月"
                  value-format="YYYY-MM"
                  style="width: 180px; margin-right: 10px;"
                />
                <el-button type="primary" @click="loadCashFlow">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button type="success" @click="handleExport('cashflow')">
                  <el-icon><Download /></el-icon>
                  导出
                </el-button>
              </div>
            </div>
          </template>

          <el-table
            :data="cashFlowData"
            v-loading="cashFlowLoading"
            border
            stripe
            row-key="id"
            :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          >
            <el-table-column prop="itemName" label="项目" min-width="250" />
            <el-table-column prop="lineNo" label="行次" width="80" align="center" />
            <el-table-column prop="currentAmount" label="本期金额" width="180" align="right">
              <template #default="{ row }">
                <span v-if="row.currentAmount != null" :style="{ fontWeight: row.isBold ? 'bold' : 'normal' }">
                  ¥ {{ Number(row.currentAmount).toFixed(2) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="previousAmount" label="上期金额" width="180" align="right">
              <template #default="{ row }">
                <span v-if="row.previousAmount != null" :style="{ fontWeight: row.isBold ? 'bold' : 'normal' }">
                  ¥ {{ Number(row.previousAmount).toFixed(2) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download } from '@element-plus/icons-vue'
import {
  generateBalanceSheet,
  generateIncomeStatement,
  generateCashFlow
} from '@/api/finance'

// ===== Tab 状态 =====
const activeTab = ref('balance')

// ===== 资产负债表 =====
const balanceDate = ref('')
const balanceData = ref([])
const balanceLoading = ref(false)

const loadBalanceSheet = async () => {
  if (!balanceDate.value) {
    ElMessage.warning('请选择查询期间')
    return
  }
  balanceLoading.value = true
  try {
    const res = await generateBalanceSheet({ asOfDate: balanceDate.value + '-01' })
    balanceData.value = formatReportData(res)
  } catch {
    ElMessage.error('加载资产负债表失败')
  } finally {
    balanceLoading.value = false
  }
}

// ===== 利润表 =====
const incomePeriod = ref('')
const incomeData = ref([])
const incomeLoading = ref(false)

const loadIncomeStatement = async () => {
  if (!incomePeriod.value) {
    ElMessage.warning('请选择查询期间')
    return
  }
  incomeLoading.value = true
  try {
    const [year, month] = incomePeriod.value.split('-')
    const startDate = `${year}-${month}-01`
    const endDate = `${year}-${month}-31`
    const res = await generateIncomeStatement({ startDate, endDate })
    incomeData.value = formatReportData(res)
  } catch {
    ElMessage.error('加载利润表失败')
  } finally {
    incomeLoading.value = false
  }
}

// ===== 现金流量表 =====
const cashFlowPeriod = ref('')
const cashFlowData = ref([])
const cashFlowLoading = ref(false)

const loadCashFlow = async () => {
  if (!cashFlowPeriod.value) {
    ElMessage.warning('请选择查询期间')
    return
  }
  cashFlowLoading.value = true
  try {
    const [year, month] = cashFlowPeriod.value.split('-')
    const startDate = `${year}-${month}-01`
    const endDate = `${year}-${month}-31`
    const res = await generateCashFlow({ startDate, endDate })
    cashFlowData.value = formatReportData(res)
  } catch {
    ElMessage.error('加载现金流量表失败')
  } finally {
    cashFlowLoading.value = false
  }
}

// ===== 报表数据格式化 =====
const formatReportData = (data) => {
  if (!data) return []
  // 如果后端返回的是数组，直接使用
  if (Array.isArray(data)) return data
  // 如果后端返回的是对象包含 items 字段
  if (data.items) return data.items
  // 如果后端返回的是对象包含 records 字段
  if (data.records) return data.records
  return []
}

// ===== 导出 =====
const handleExport = async (type) => {
  const periodMap = {
    balance: balanceDate,
    income: incomePeriod,
    cashflow: cashFlowPeriod
  }
  const period = periodMap[type].value
  if (!period) {
    ElMessage.warning('请先选择查询期间')
    return
  }
  try {
    // 使用后端报表导出接口
    const res = await fetch(`/api/finance/reports/export`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ reportType: type, period }),
      responseType: 'blob'
    })
    if (res.ok) {
      const blob = await res.blob()
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      const typeNames = { balance: '资产负债表', income: '利润表', cashflow: '现金流量表' }
      link.download = `${typeNames[type]}_${period}.xlsx`
      link.click()
      window.URL.revokeObjectURL(url)
      ElMessage.success('导出成功')
    } else {
      ElMessage.error('导出失败')
    }
  } catch {
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  // 默认加载当前月份
  const now = new Date()
  const defaultPeriod = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  balanceDate.value = defaultPeriod
  incomePeriod.value = defaultPeriod
  cashFlowPeriod.value = defaultPeriod
})
</script>

<style scoped lang="scss">
.report-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
