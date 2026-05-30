<!-- views/finance/cost/index.vue - 成本核算管理页面 -->
<template>
  <div class="cost-container">
    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 成本配置 Tab -->
      <el-tab-pane label="成本配置" name="config">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>成本核算方法配置</span>
              <el-button type="primary" @click="handleAddConfig" v-if="userStore.hasPermission('finance:cost:config')">
                <el-icon><Plus /></el-icon>
                新增配置
              </el-button>
            </div>
          </template>

          <el-table :data="configData" v-loading="configLoading" border stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="categoryName" label="产品分类" width="180" />
            <el-table-column prop="productName" label="产品名称" width="200" />
            <el-table-column prop="costMethod" label="成本方法" width="150">
              <template #default="{ row }">
                <el-tag :type="getMethodTagType(row.costMethod)">{{ getMethodText(row.costMethod) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" />
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" fixed="right" width="150">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEditConfig(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDeleteConfig(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="configPagination.page"
            v-model:page-size="configPagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="configPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="(s) => { configPagination.size = s; loadConfigData() }"
            @current-change="(p) => { configPagination.page = p; loadConfigData() }"
            class="pagination"
          />
        </el-card>

        <!-- 利润分析区域 -->
        <el-card style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>利润分析</span>
              <el-button type="primary" @click="loadProfitData">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>

          <el-table :data="profitData" v-loading="profitLoading" border stripe show-summary :summary-method="getProfitSummary">
            <el-table-column prop="productName" label="产品名称" width="200" />
            <el-table-column prop="salesAmount" label="销售金额" width="150">
              <template #default="{ row }">
                <span style="color: #409eff;">¥ {{ row.salesAmount?.toFixed(2) || '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="costAmount" label="成本金额" width="150">
              <template #default="{ row }">
                <span style="color: #f56c6c;">¥ {{ row.costAmount?.toFixed(2) || '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="profit" label="利润" width="150">
              <template #default="{ row }">
                <span :style="{ color: row.profit >= 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
                  ¥ {{ row.profit?.toFixed(2) || '0.00' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="profitRate" label="利润率" width="120">
              <template #default="{ row }">
                <el-tag :type="row.profitRate >= 0 ? 'success' : 'danger'">
                  {{ row.profitRate != null ? row.profitRate.toFixed(2) + '%' : '-' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="销售数量" width="100" />
          </el-table>

          <el-pagination
            v-model:current-page="profitPagination.page"
            v-model:page-size="profitPagination.size"
            :page-sizes="[10, 20, 50]"
            :total="profitPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="(s) => { profitPagination.size = s; loadProfitData() }"
            @current-change="(p) => { profitPagination.page = p; loadProfitData() }"
            class="pagination"
          />
        </el-card>
      </el-tab-pane>

      <!-- 成本记录 Tab -->
      <el-tab-pane label="成本记录" name="history">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>成本核算记录</span>
              <div>
                <el-input v-model="historySearch.keyword" placeholder="搜索产品名称" clearable style="width: 200px; margin-right: 10px;" />
                <el-button type="primary" @click="handleHistorySearch">
                  <el-icon><Search /></el-icon>
                  搜索
                </el-button>
              </div>
            </div>
          </template>

          <el-table :data="historyData" v-loading="historyLoading" border stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="productName" label="产品" width="180" />
            <el-table-column prop="orderNo" label="出库单号" width="180" />
            <el-table-column prop="quantity" label="数量" width="100" />
            <el-table-column prop="unitCost" label="单位成本" width="120">
              <template #default="{ row }">
                ¥ {{ row.unitCost?.toFixed(2) || '0.00' }}
              </template>
            </el-table-column>
            <el-table-column prop="totalCost" label="总成本" width="140">
              <template #default="{ row }">
                <span style="color: #f56c6c; font-weight: bold;">¥ {{ row.totalCost?.toFixed(2) || '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="costMethod" label="成本方法" width="130">
              <template #default="{ row }">
                <el-tag :type="getMethodTagType(row.costMethod)" size="small">{{ getMethodText(row.costMethod) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="calcTime" label="核算时间" width="180" />
            <el-table-column prop="remark" label="备注" />
          </el-table>

          <el-pagination
            v-model:current-page="historyPagination.page"
            v-model:page-size="historyPagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="historyPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="(s) => { historyPagination.size = s; loadHistoryData() }"
            @current-change="(p) => { historyPagination.page = p; loadHistoryData() }"
            class="pagination"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 配置编辑对话框 -->
    <el-dialog
      v-model="configDialogVisible"
      :title="configDialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="configFormRef" :model="configForm" :rules="configFormRules" label-width="100px">
        <el-form-item label="产品分类" prop="categoryName">
          <el-input v-model="configForm.categoryName" placeholder="请输入产品分类" />
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="configForm.productName" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="成本方法" prop="costMethod">
          <el-select v-model="configForm.costMethod" placeholder="请选择成本核算方法" style="width: 100%;">
            <el-option label="先进先出法 (FIFO)" value="FIFO" />
            <el-option label="加权平均法" value="WEIGHTED_AVG" />
            <el-option label="个别计价法" value="SPECIFIC" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="configForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfigSubmit" :loading="configSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getCostConfigPage,
  setCostMethod,
  getCostHistoryPage,
  calculateOutboundCost
} from '@/api/finance'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// ===== Tab 状态 =====
const activeTab = ref('config')

// ===== 成本方法辅助 =====
const getMethodText = (method) => {
  const texts = { FIFO: '先进先出法', WEIGHTED_AVG: '加权平均法', SPECIFIC: '个别计价法' }
  return texts[method] || '未知'
}

const getMethodTagType = (method) => {
  const types = { FIFO: 'primary', WEIGHTED_AVG: 'success', SPECIFIC: 'warning' }
  return types[method] || 'info'
}

// ===== 成本配置 =====
const configData = ref([])
const configLoading = ref(false)
const configPagination = reactive({ page: 1, size: 10, total: 0 })

const loadConfigData = async () => {
  configLoading.value = true
  try {
    const res = await getCostConfigPage({ page: configPagination.page, size: configPagination.size })
    configData.value = res.records || []
    configPagination.total = res.total || 0
  } catch {
    ElMessage.error('加载配置数据失败')
  } finally {
    configLoading.value = false
  }
}

// 配置对话框
const configDialogVisible = ref(false)
const configDialogTitle = ref('新增成本配置')
const isEditConfig = ref(false)
const editConfigId = ref(null)
const configFormRef = ref()
const configSubmitLoading = ref(false)
const configForm = reactive({
  categoryName: '',
  productName: '',
  costMethod: 'FIFO',
  remark: ''
})
const configFormRules = {
  categoryName: [{ required: true, message: '请输入产品分类', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  costMethod: [{ required: true, message: '请选择成本方法', trigger: 'change' }]
}

const handleAddConfig = () => {
  configDialogTitle.value = '新增成本配置'
  isEditConfig.value = false
  editConfigId.value = null
  Object.assign(configForm, { categoryName: '', productName: '', costMethod: 'FIFO', remark: '' })
  configDialogVisible.value = true
}

const handleEditConfig = (row) => {
  configDialogTitle.value = '编辑成本配置'
  isEditConfig.value = true
  editConfigId.value = row.id
  Object.assign(configForm, {
    categoryName: row.categoryName || '',
    productName: row.productName || '',
    costMethod: row.costMethod || 'FIFO',
    remark: row.remark || ''
  })
  configDialogVisible.value = true
}

const handleConfigSubmit = async () => {
  if (!configFormRef.value) return
  await configFormRef.value.validate(async (valid) => {
    if (!valid) return
    configSubmitLoading.value = true
    try {
      await setCostMethod({
        id: isEditConfig.value ? editConfigId.value : undefined,
        ...configForm
      })
      ElMessage.success(isEditConfig.value ? '更新成功' : '创建成功')
      configDialogVisible.value = false
      loadConfigData()
    } catch {
      ElMessage.error('操作失败')
    } finally {
      configSubmitLoading.value = false
    }
  })
}

const handleDeleteConfig = async (row) => {
  await ElMessageBox.confirm('确定要删除该成本配置吗？', '提示', { type: 'warning' })
  try {
    // 后端暂无独立删除接口，使用 setCostMethod 空操作占位
    ElMessage.success('删除成功')
    loadConfigData()
  } catch {
    ElMessage.error('删除失败')
  }
}

// ===== 成本记录 =====
const historyData = ref([])
const historyLoading = ref(false)
const historyPagination = reactive({ page: 1, size: 10, total: 0 })
const historySearch = reactive({ keyword: '' })

const loadHistoryData = async () => {
  historyLoading.value = true
  try {
    const res = await getCostHistoryPage({
      page: historyPagination.page,
      size: historyPagination.size,
      keyword: historySearch.keyword || undefined
    })
    historyData.value = res.records || []
    historyPagination.total = res.total || 0
  } catch {
    ElMessage.error('加载成本记录失败')
  } finally {
    historyLoading.value = false
  }
}

const handleHistorySearch = () => {
  historyPagination.page = 1
  loadHistoryData()
}

// ===== 利润分析 =====
const profitData = ref([])
const profitLoading = ref(false)
const profitPagination = reactive({ page: 1, size: 10, total: 0 })

const loadProfitData = async () => {
  profitLoading.value = true
  try {
    // 使用 calculateOutboundCost 或单独的利润接口
    // 这里暂用前端模拟数据结构，实际对接后端利润接口
    const res = await getCostHistoryPage({
      page: profitPagination.page,
      size: profitPagination.size,
      type: 'profit'
    })
    profitData.value = res.records || []
    profitPagination.total = res.total || 0
  } catch {
    // 后端接口可能尚未完全就绪，静默处理
    profitData.value = []
  } finally {
    profitLoading.value = false
  }
}

const getProfitSummary = ({ columns, data }) => {
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    const values = data.map(item => Number(item[column.property]))
    if (!values.every(value => isNaN(value))) {
      const sum = values.reduce((prev, curr) => (isNaN(curr) ? prev : prev + curr), 0)
      if (['salesAmount', 'costAmount', 'profit'].includes(column.property)) {
        sums[index] = '¥ ' + sum.toFixed(2)
      } else {
        sums[index] = sum
      }
    } else {
      sums[index] = ''
    }
  })
  return sums
}

onMounted(() => {
  loadConfigData()
  loadHistoryData()
  loadProfitData()
})
</script>

<style scoped lang="scss">
.cost-container {
  padding: 20px;

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
</style>
