<!-- views/erp/sales-order/index.vue - 销售订单管理页面 -->
<template>
  <div class="sales-order-container">
    <!-- 搜索栏区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable />
        </el-form-item>
        <el-form-item label="客户">
          <el-select v-model="searchForm.customerId" placeholder="请选择客户" clearable filterable style="width: 180px;">
            <el-option
              v-for="customer in customerList"
              :key="customer.id"
              :label="customer.customerName"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="草稿" :value="0" />
            <el-option label="待审核" :value="1" />
            <el-option label="已审核" :value="2" />
            <el-option label="已出库" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
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
            新建订单
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格区域 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单编号" width="150" />
        <el-table-column prop="customerName" label="客户" width="150" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="totalAmount" label="订单金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="deliveredAmount" label="已发货金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.deliveredAmount?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="orderStatusText" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.orderStatus)">{{ row.orderStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderDate" label="订单日期" width="110" />
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="row.orderStatus === 0" type="warning" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.orderStatus === 0" type="success" link @click="handleSubmit(row)">提交</el-button>
            <el-button v-if="row.orderStatus === 1" type="success" link @click="handleApprove(row)">审核</el-button>
            <el-button v-if="row.orderStatus < 3" type="danger" link @click="handleCancel(row)">取消</el-button>
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

    <!-- 创建/编辑订单对话框 -->
    <el-dialog
      v-model="orderDialogVisible"
      :title="isEdit ? '编辑销售订单' : '新建销售订单'"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-form ref="orderFormRef" :model="orderForm" :rules="orderRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="orderForm.customerId" placeholder="请选择客户" filterable style="width: 100%;">
                <el-option
                  v-for="customer in customerList"
                  :key="customer.id"
                  :label="customer.customerName"
                  :value="customer.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select v-model="orderForm.warehouseId" placeholder="请选择仓库" style="width: 100%;">
                <el-option
                  v-for="warehouse in warehouseList"
                  :key="warehouse.id"
                  :label="warehouse.warehouseName"
                  :value="warehouse.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预计发货日期">
              <el-date-picker v-model="orderForm.expectedDate" type="date" placeholder="选择日期" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售员">
              <el-input v-model="orderForm.salesId" placeholder="销售员ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="orderForm.remark" type="textarea" :rows="2" />
        </el-form-item>

        <!-- 订单明细 -->
        <el-divider content-position="left">订单明细</el-divider>
        <el-table :data="orderForm.items" border max-height="300">
          <el-table-column label="商品" width="200">
            <template #default="{ row, $index }">
              <el-select v-model="row.productId" placeholder="选择商品" filterable @change="handleProductChange(row, $index)">
                <el-option
                  v-for="product in productList"
                  :key="product.id"
                  :label="`${product.productName} (${product.productCode})`"
                  :value="product.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="0.01" :precision="2" style="width: 100%;" />
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.price" :min="0" :precision="2" style="width: 100%;" />
            </template>
          </el-table-column>
          <el-table-column label="折扣" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.discountAmount" :min="0" :precision="2" style="width: 100%;" />
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120">
            <template #default="{ row }">
              {{ calculateLineAmount(row) }}
            </template>
          </el-table-column>
          <el-table-column label="备注" width="150">
            <template #default="{ row }">
              <el-input v-model="row.remark" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button type="danger" link @click="handleRemoveItem($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" link @click="handleAddItem" style="margin-top: 10px;">
          <el-icon><Plus /></el-icon>
          添加明细
        </el-button>
      </el-form>
      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleOrderSubmit" :loading="orderLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单编号">{{ currentRow?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ currentRow?.customerName }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ currentRow?.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRow?.orderStatus)">{{ currentRow?.orderStatusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ currentRow?.totalAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="已发货金额">¥{{ currentRow?.deliveredAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="订单日期">{{ currentRow?.orderDate }}</el-descriptions-item>
        <el-descriptions-item label="预计发货日期">{{ currentRow?.expectedDate }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">订单明细</el-divider>
      <el-table :data="currentRow?.items" border>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="quantity" label="数量" width="100" align="right" />
        <el-table-column prop="price" label="单价" width="100" align="right" />
        <el-table-column prop="amount" label="金额" width="100" align="right" />
        <el-table-column prop="deliveredQuantity" label="已发货数量" width="120" align="right" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getSalesOrderPage,
  getSalesOrderDetail,
  createSalesOrder,
  updateSalesOrder,
  submitSalesOrder,
  approveSalesOrder,
  cancelSalesOrder
} from '@/api/erp'
import { getCustomerPage } from '@/api/erp'
import { getWarehouses } from '@/api/erp'
import { getProductPage } from '@/api/erp'

const searchForm = reactive({
  orderNo: '',
  customerId: undefined,
  status: undefined
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const loading = ref(false)
const customerList = ref([])
const warehouseList = ref([])
const productList = ref([])

// 订单表单
const orderDialogVisible = ref(false)
const orderFormRef = ref()
const orderLoading = ref(false)
const isEdit = ref(false)
const currentRow = ref(null)
const orderForm = reactive({
  customerId: undefined,
  warehouseId: undefined,
  expectedDate: null,
  salesId: null,
  remark: '',
  items: []
})

const orderRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }]
}

// 详情对话框
const detailDialogVisible = ref(false)

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'warning', 2: 'success', 3: 'primary', 4: 'success', 5: 'danger' }
  return types[status] || 'info'
}

const calculateLineAmount = (row) => {
  const amount = (row.quantity || 0) * (row.price || 0) - (row.discountAmount || 0)
  return '¥' + amount.toFixed(2)
}

const handleProductChange = (row, index) => {
  const product = productList.value.find(p => p.id === row.productId)
  if (product) {
    row.price = product.salePrice || 0
  }
}

const handleAddItem = () => {
  orderForm.items.push({
    productId: undefined,
    quantity: 1,
    price: 0,
    discountAmount: 0,
    remark: ''
  })
}

const handleRemoveItem = (index) => {
  orderForm.items.splice(index, 1)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSalesOrderPage({
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

const loadCustomers = async () => {
  try {
    const res = await getCustomerPage({ page: 1, size: 1000 })
    customerList.value = res.records || []
  } catch (error) {
    console.error('加载客户列表失败', error)
  }
}

const loadWarehouses = async () => {
  try {
    const res = await getWarehouses()
    warehouseList.value = res || []
  } catch (error) {
    console.error('加载仓库列表失败', error)
  }
}

const loadProducts = async () => {
  try {
    const res = await getProductPage({ page: 1, size: 1000 })
    productList.value = res.records || []
  } catch (error) {
    console.error('加载商品列表失败', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.customerId = undefined
  searchForm.status = undefined
  handleSearch()
}

const handleCreate = () => {
  isEdit.value = false
  Object.assign(orderForm, {
    customerId: undefined,
    warehouseId: undefined,
    expectedDate: null,
    salesId: null,
    remark: '',
    items: []
  })
  orderDialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  try {
    const res = await getSalesOrderDetail(row.id)
    Object.assign(orderForm, {
      customerId: res.customerId,
      warehouseId: res.warehouseId,
      expectedDate: res.expectedDate,
      salesId: res.salesId,
      remark: res.remark,
      items: res.items.map(item => ({
        productId: item.productId,
        quantity: item.quantity,
        price: item.price,
        discountAmount: item.discountAmount,
        remark: item.remark
      }))
    })
    currentRow.value = row
    orderDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载订单详情失败')
  }
}

const handleOrderSubmit = async () => {
  if (!orderFormRef.value) return
  await orderFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (orderForm.items.length === 0) {
      ElMessage.warning('请添加订单明细')
      return
    }
    orderLoading.value = true
    try {
      if (isEdit.value) {
        await updateSalesOrder(currentRow.value.id, orderForm)
        ElMessage.success('更新成功')
      } else {
        await createSalesOrder(orderForm)
        ElMessage.success('创建成功')
      }
      orderDialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      orderLoading.value = false
    }
  })
}

const handleSubmit = async (row) => {
  try {
    await submitSalesOrder(row.id)
    ElMessage.success('提交成功')
    loadData()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const handleApprove = async (row) => {
  try {
    await approveSalesOrder(row.id)
    ElMessage.success('审核成功')
    loadData()
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm('确认取消该订单？', '提示', { type: 'warning' })
  try {
    await cancelSalesOrder(row.id)
    ElMessage.success('取消成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('取消失败')
  }
}

const handleView = async (row) => {
  try {
    const res = await getSalesOrderDetail(row.id)
    currentRow.value = res
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载详情失败')
  }
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
  loadCustomers()
  loadWarehouses()
  loadProducts()
  loadData()
})
</script>

<style scoped lang="scss">
.sales-order-container {
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
