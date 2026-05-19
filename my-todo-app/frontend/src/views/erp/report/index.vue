<!-- views/erp/report/index.vue - 报表统计页面 -->
<template>
  <div class="report-container">
    <!-- Dashboard卡片 -->
    <el-row :gutter="20" class="dashboard-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header><span>今日销售额</span></template>
          <div class="card-value">¥{{ dashboard.todaySales?.toFixed(2) || '0.00' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header><span>本月销售额</span></template>
          <div class="card-value">¥{{ dashboard.monthSales?.toFixed(2) || '0.00' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header><span>今日采购额</span></template>
          <div class="card-value">¥{{ dashboard.todayPurchases?.toFixed(2) || '0.00' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header><span>本月采购额</span></template>
          <div class="card-value">¥{{ dashboard.monthPurchases?.toFixed(2) || '0.00' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard-cards">
      <el-col :span="4">
        <el-card shadow="hover">
          <template #header><span>库存种类</span></template>
          <div class="card-value">{{ dashboard.inventoryProductCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <template #header><span>库存预警</span></template>
          <div class="card-value warning">{{ dashboard.alertProductCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <template #header><span>待审采购单</span></template>
          <div class="card-value">{{ dashboard.pendingPurchaseOrders || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <template #header><span>待审销售单</span></template>
          <div class="card-value">{{ dashboard.pendingSalesOrders || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <template #header><span>近7天销售</span></template>
          <div class="card-value">¥{{ weekSalesTotal.toFixed(2) }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <template #header><span>近7天采购</span></template>
          <div class="card-value">¥{{ weekPurchaseTotal.toFixed(2) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 报表选择 -->
    <el-card class="report-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="销售报表" name="sales">
          <el-form :inline="true" class="report-form">
            <el-form-item label="日期范围">
              <el-date-picker v-model="salesDateRange" type="daterange" range-separator="至"
                start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadSalesReport">查询</el-button>
            </el-form-item>
          </el-form>
          <el-row :gutter="20" v-if="salesReport.summary">
            <el-col :span="6">
              <el-statistic title="总销售额" :value="salesReport.summary.totalAmount" :precision="2" prefix="¥" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="订单数" :value="salesReport.summary.totalOrders" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="商品数量" :value="salesReport.summary.totalQuantity" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="平均订单金额" :value="salesReport.summary.avgOrderAmount" :precision="2" prefix="¥" />
            </el-col>
          </el-row>
          <el-divider content-position="left">销售趋势</el-divider>
          <el-table :data="salesReport.dailyStats" border>
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="orderCount" label="订单数" width="100" align="right" />
            <el-table-column prop="amount" label="销售额" width="150" align="right">
              <template #default="{ row }">¥{{ row.amount?.toFixed(2) }}</template>
            </el-table-column>
          </el-table>
          <el-divider content-position="left">商品排行</el-divider>
          <el-table :data="salesReport.productStats" border>
            <el-table-column type="index" label="排名" width="80" />
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="productCode" label="商品编码" width="120" />
            <el-table-column prop="quantity" label="销售数量" width="100" align="right" />
            <el-table-column prop="amount" label="销售金额" width="150" align="right">
              <template #default="{ row }">¥{{ row.amount?.toFixed(2) }}</template>
            </el-table-column>
          </el-table>
          <el-divider content-position="left">客户排行</el-divider>
          <el-table :data="salesReport.customerStats" border>
            <el-table-column type="index" label="排名" width="80" />
            <el-table-column prop="customerName" label="客户名称" />
            <el-table-column prop="orderCount" label="订单数" width="100" align="right" />
            <el-table-column prop="amount" label="销售金额" width="150" align="right">
              <template #default="{ row }">¥{{ row.amount?.toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="采购报表" name="purchase">
          <el-form :inline="true" class="report-form">
            <el-form-item label="日期范围">
              <el-date-picker v-model="purchaseDateRange" type="daterange" range-separator="至"
                start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadPurchaseReport">查询</el-button>
            </el-form-item>
          </el-form>
          <el-row :gutter="20" v-if="purchaseReport.summary">
            <el-col :span="6">
              <el-statistic title="总采购额" :value="purchaseReport.summary.totalAmount" :precision="2" prefix="¥" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="订单数" :value="purchaseReport.summary.totalOrders" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="商品数量" :value="purchaseReport.summary.totalQuantity" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="平均订单金额" :value="purchaseReport.summary.avgOrderAmount" :precision="2" prefix="¥" />
            </el-col>
          </el-row>
          <el-divider content-position="left">采购趋势</el-divider>
          <el-table :data="purchaseReport.dailyStats" border>
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="orderCount" label="订单数" width="100" align="right" />
            <el-table-column prop="amount" label="采购额" width="150" align="right">
              <template #default="{ row }">¥{{ row.amount?.toFixed(2) }}</template>
            </el-table-column>
          </el-table>
          <el-divider content-position="left">供应商排行</el-divider>
          <el-table :data="purchaseReport.supplierStats" border>
            <el-table-column type="index" label="排名" width="80" />
            <el-table-column prop="supplierName" label="供应商名称" />
            <el-table-column prop="orderCount" label="订单数" width="100" align="right" />
            <el-table-column prop="amount" label="采购金额" width="150" align="right">
              <template #default="{ row }">¥{{ row.amount?.toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="库存报表" name="inventory">
          <el-form :inline="true" class="report-form">
            <el-form-item label="仓库">
              <el-select v-model="inventoryWarehouseId" placeholder="全部仓库" clearable style="width: 180px;">
                <el-option v-for="w in warehouseList" :key="w.id" :label="w.warehouseName" :value="w.id" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadInventoryReport">查询</el-button>
            </el-form-item>
          </el-form>
          <el-row :gutter="20" v-if="inventoryReport.summary">
            <el-col :span="6">
              <el-statistic title="商品种类" :value="inventoryReport.summary.totalProducts" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="库存总量" :value="inventoryReport.summary.totalQuantity" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="库存总值" :value="inventoryReport.summary.totalValue" :precision="2" prefix="¥" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="预警商品" :value="inventoryReport.summary.alertCount" />
            </el-col>
          </el-row>
          <el-divider content-position="left">库存明细</el-divider>
          <el-table :data="inventoryReport.details" border max-height="500">
            <el-table-column prop="warehouseName" label="仓库" width="120" />
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="productCode" label="商品编码" width="120" />
            <el-table-column prop="quantity" label="库存数量" width="100" align="right" />
            <el-table-column prop="costPrice" label="成本价" width="100" align="right">
              <template #default="{ row }">¥{{ row.costPrice?.toFixed(4) }}</template>
            </el-table-column>
            <el-table-column prop="totalValue" label="库存金额" width="120" align="right">
              <template #default="{ row }">¥{{ row.totalValue?.toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDashboard, getSalesReport, getPurchaseReport, getInventoryReport, getWarehouses } from '@/api/erp'

const activeTab = ref('sales')
const dashboard = ref({})
const warehouseList = ref([])

// 销售报表
const salesDateRange = ref([])
const salesReport = ref({})

// 采购报表
const purchaseDateRange = ref([])
const purchaseReport = ref({})

// 库存报表
const inventoryWarehouseId = ref(null)
const inventoryReport = ref({})

const weekSalesTotal = computed(() =>
  dashboard.value.salesTrend?.reduce((sum, item) => sum + (item.amount || 0), 0) || 0
)
const weekPurchaseTotal = computed(() =>
  dashboard.value.purchaseTrend?.reduce((sum, item) => sum + (item.amount || 0), 0) || 0
)

const loadDashboard = async () => {
  try { dashboard.value = await getDashboard() } catch { ElMessage.error('加载Dashboard失败') }
}

const loadSalesReport = async () => {
  if (!salesDateRange.value || salesDateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  try {
    salesReport.value = await getSalesReport({
      startDate: salesDateRange.value[0],
      endDate: salesDateRange.value[1]
    })
  } catch { ElMessage.error('加载销售报表失败') }
}

const loadPurchaseReport = async () => {
  if (!purchaseDateRange.value || purchaseDateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  try {
    purchaseReport.value = await getPurchaseReport({
      startDate: purchaseDateRange.value[0],
      endDate: purchaseDateRange.value[1]
    })
  } catch { ElMessage.error('加载采购报表失败') }
}

const loadInventoryReport = async () => {
  try {
    inventoryReport.value = await getInventoryReport({
      warehouseId: inventoryWarehouseId.value
    })
  } catch { ElMessage.error('加载库存报表失败') }
}

const loadWarehouses = async () => {
  try { warehouseList.value = await getWarehouses() || [] } catch {}
}

onMounted(() => {
  loadDashboard()
  loadWarehouses()
  // 默认加载近30天报表
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 30)
  salesDateRange.value = [start.toISOString().split('T')[0], end.toISOString().split('T')[0]]
  purchaseDateRange.value = [...salesDateRange.value]
})
</script>

<style scoped lang="scss">
.report-container {
  padding: 20px;

  .dashboard-cards {
    margin-bottom: 20px;

    .card-value {
      font-size: 24px;
      font-weight: bold;
      color: #303133;
      text-align: center;

      &.warning {
        color: #e6a23c;
      }
    }
  }

  .report-card {
    .report-form {
      margin-bottom: 20px;
    }

    .el-row {
      margin-bottom: 20px;
    }
  }
}
</style>
