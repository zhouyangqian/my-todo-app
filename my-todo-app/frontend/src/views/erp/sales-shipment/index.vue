<!-- views/erp/sales-shipment/index.vue - 销售出库单管理页面 -->
<template>
  <div class="sales-shipment-container">
    <!-- 搜索栏区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="出库单号">
          <el-input v-model="searchForm.shipmentNo" placeholder="请输入出库单号" clearable />
        </el-form-item>
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable />
        </el-form-item>
        <el-form-item label="出库状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="草稿" :value="0" />
            <el-option label="待审核" :value="1" />
            <el-option label="已出库" :value="2" />
            <el-option label="已取消" :value="3" />
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
          <el-button type="success" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建出库单
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格区域 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="shipmentNo" label="出库单号" width="150" />
        <el-table-column prop="orderNo" label="订单编号" width="150" />
        <el-table-column prop="customerName" label="客户" width="150" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="receivedAmount" label="出库金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.receivedAmount?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="shipmentStatusText" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.shipmentStatus)">{{ row.shipmentStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="shipmentDate" label="出库日期" width="110" />
        <el-table-column label="操作" fixed="right" width="260">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="row.shipmentStatus === 0" type="warning" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.shipmentStatus < 2" type="success" link @click="handleApprove(row)">审核出库</el-button>
            <el-button v-if="row.shipmentStatus < 2" type="danger" link @click="handleCancel(row)">取消</el-button>
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

    <!-- 选择订单对话框 -->
    <el-dialog
      v-model="selectOrderDialogVisible"
      title="选择销售订单"
      width="800px"
    >
      <el-table :data="orderList" border @row-click="handleSelectOrder" style="cursor: pointer;">
        <el-table-column prop="orderNo" label="订单编号" width="150" />
        <el-table-column prop="customerName" label="客户" width="150" />
        <el-table-column prop="totalAmount" label="订单金额" width="120" align="right" />
        <el-table-column prop="orderStatusText" label="状态" width="100" />
        <el-table-column prop="orderDate" label="订单日期" width="110" />
      </el-table>
    </el-dialog>

    <!-- 创建出库单对话框 -->
    <el-dialog
      v-model="shipmentDialogVisible"
      title="新建销售出库单"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="2" border v-if="selectedOrder">
        <el-descriptions-item label="订单编号">{{ selectedOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ selectedOrder.customerName }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ selectedOrder.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ selectedOrder.totalAmount?.toFixed(2) }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">可发货商品</el-divider>
      <el-table :data="shippableItems" border max-height="300">
        <el-table-column label="商品名称" prop="productName" width="200" />
        <el-table-column label="商品编码" prop="productCode" width="120" />
        <el-table-column label="订单数量" prop="quantity" width="100" align="right" />
        <el-table-column label="已发货数量" prop="deliveredQuantity" width="120" align="right" />
        <el-table-column label="可发货数量" prop="shippableQuantity" width="120" align="right" />
        <el-table-column label="本次发货数量" width="150">
          <template #default="{ row }">
            <el-input-number
              v-model="row.shipQuantity"
              :min="0"
              :max="row.shippableQuantity"
              :precision="2"
              style="width: 100%;"
            />
          </template>
        </el-table-column>
        <el-table-column label="备注" width="150">
          <template #default="{ row }">
            <el-input v-model="row.remark" />
          </template>
        </el-table-column>
      </el-table>
      <el-form style="margin-top: 20px;">
        <el-form-item label="出库日期">
          <el-date-picker v-model="shipmentDate" type="date" placeholder="选择日期" style="width: 200px;" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="remark" type="textarea" :rows="2" style="width: 400px;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipmentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleShipmentSubmit" :loading="shipmentLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="出库单详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="出库单号">{{ currentRow?.shipmentNo }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ currentRow?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ currentRow?.customerName }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ currentRow?.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRow?.shipmentStatus)">{{ currentRow?.shipmentStatusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="出库金额">¥{{ currentRow?.receivedAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="出库日期">{{ currentRow?.shipmentDate }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">出库明细</el-divider>
      <el-table :data="currentRow?.items" border>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="quantity" label="数量" width="100" align="right" />
        <el-table-column prop="price" label="单价" width="100" align="right" />
        <el-table-column prop="amount" label="金额" width="100" align="right" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getSalesShipmentPage,
  getSalesShipmentDetail,
  getShippableItems,
  createSalesShipment,
  approveSalesShipment,
  cancelSalesShipment
} from '@/api/erp'
import { getSalesOrderPage } from '@/api/erp'

const searchForm = reactive({
  shipmentNo: '',
  orderNo: '',
  status: undefined
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const loading = ref(false)

// 选择订单
const selectOrderDialogVisible = ref(false)
const orderList = ref([])

// 创建出库单
const shipmentDialogVisible = ref(false)
const shipmentLoading = ref(false)
const selectedOrder = ref(null)
const shippableItems = ref([])
const shipmentDate = ref(new Date())
const remark = ref('')

// 详情
const detailDialogVisible = ref(false)
const currentRow = ref(null)

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSalesShipmentPage({
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

const loadOrders = async () => {
  try {
    const res = await getSalesOrderPage({
      page: 1,
      size: 100,
      status: '2' // 已审核
    })
    orderList.value = res.records?.filter(o => o.orderStatus === 2) || []
  } catch (error) {
    console.error('加载订单列表失败', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.shipmentNo = ''
  searchForm.orderNo = ''
  searchForm.status = undefined
  handleSearch()
}

const handleCreate = async () => {
  await loadOrders()
  if (orderList.value.length === 0) {
    ElMessage.warning('没有可发货的订单')
    return
  }
  selectOrderDialogVisible.value = true
}

const handleSelectOrder = async (row) => {
  selectOrderDialogVisible.value = false
  selectedOrder.value = row
  try {
    const items = await getShippableItems(row.id)
    shippableItems.value = items.map(item => ({
      ...item,
      shipQuantity: item.shippableQuantity,
      remark: ''
    }))
  } catch (error) {
    ElMessage.error('获取可发货商品失败')
    return
  }
  shipmentDate.value = new Date()
  remark.value = ''
  shipmentDialogVisible.value = true
}

const handleShipmentSubmit = async () => {
  const itemsToShip = shippableItems.value
    .filter(item => item.shipQuantity > 0)
    .map(item => ({
      orderItemId: item.id,
      quantity: item.shipQuantity,
      remark: item.remark
    }))

  if (itemsToShip.length === 0) {
    ElMessage.warning('请选择要发货的商品')
    return
  }

  shipmentLoading.value = true
  try {
    await createSalesShipment({
      orderId: selectedOrder.value.id,
      shipmentDate: shipmentDate.value,
      remark: remark.value,
      items: itemsToShip
    })
    ElMessage.success('创建成功')
    shipmentDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('创建失败')
  } finally {
    shipmentLoading.value = false
  }
}

const handleApprove = async (row) => {
  await ElMessageBox.confirm(
    '审核出库单将扣减库存并生成应收账款，确认继续？',
    '提示',
    { type: 'warning' }
  )
  try {
    await approveSalesShipment(row.id)
    ElMessage.success('审核成功，库存已扣减，应收账款已生成')
    loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('审核失败')
  }
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm('确认取消该出库单？', '提示', { type: 'warning' })
  try {
    await cancelSalesShipment(row.id)
    ElMessage.success('取消成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('取消失败')
  }
}

const handleView = async (row) => {
  try {
    const res = await getSalesShipmentDetail(row.id)
    currentRow.value = res
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载详情失败')
  }
}

const handleEdit = (row) => {
  ElMessage.info('编辑功能开发中')
}

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
.sales-shipment-container {
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
    .pagination {
      margin-top: 20px;
      justify-content: flex-end;
    }
  }
}
</style>
