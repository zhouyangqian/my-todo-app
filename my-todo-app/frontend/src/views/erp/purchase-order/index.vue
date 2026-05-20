<!-- views/erp/purchase-order/index.vue - 采购订单管理页面 -->
<template>
  <div class="purchase-order-container">
    <!-- 搜索栏区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable />
        </el-form-item>
        <el-form-item label="供应商">
          <el-select v-model="searchForm.supplierId" placeholder="请选择供应商" clearable filterable style="width: 180px;">
            <el-option
              v-for="supplier in supplierList"
              :key="supplier.id"
              :label="supplier.supplierName"
              :value="supplier.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="草稿" :value="0" />
            <el-option label="待审核" :value="1" />
            <el-option label="已审核" :value="2" />
            <el-option label="已入库" :value="3" />
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
          <el-button type="success" @click="handleCreate" v-if="userStore.hasPermission('erp:purchaseOrder:create')">
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
        <el-table-column prop="supplierName" label="供应商" width="150" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="totalAmount" label="订单金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="paidAmount" label="实付金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.paidAmount?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="orderStatusText" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.orderStatus)">{{ row.orderStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderDate" label="订单日期" width="110" />
        <el-table-column label="操作" fixed="right" width="320">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="row.orderStatus === 0 && userStore.hasPermission('erp:purchaseOrder:update')" type="warning" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.orderStatus === 0 && userStore.hasPermission('erp:purchaseOrder:submit')" type="success" link @click="handleSubmit(row)">提交</el-button>
            <el-button v-if="row.orderStatus === 1 && userStore.hasPermission('erp:purchaseOrder:approve')" type="success" link @click="handleApprove(row)">审核</el-button>
            <el-button v-if="row.orderStatus === 2 && userStore.hasPermission('erp:inventory:inbound')" type="primary" link @click="handleInbound(row)">入库</el-button>
            <el-button v-if="row.orderStatus < 3 && userStore.hasPermission('erp:purchaseOrder:cancel')" type="danger" link @click="handleCancel(row)">取消</el-button>
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
      :title="isEdit ? '编辑采购订单' : '新建采购订单'"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-form ref="orderFormRef" :model="orderForm" :rules="orderRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="orderForm.supplierId" placeholder="请选择供应商" filterable style="width: 100%;">
                <el-option
                  v-for="supplier in supplierList"
                  :key="supplier.id"
                  :label="supplier.supplierName"
                  :value="supplier.id"
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
            <el-form-item label="预计到货日期">
              <el-date-picker v-model="orderForm.expectedDate" type="date" placeholder="选择日期" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经手人">
              <el-input-number v-model="orderForm.handlerId" placeholder="经手人ID" :min="1" style="width: 100%;" />
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
        <el-descriptions-item label="供应商">{{ currentRow?.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ currentRow?.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRow?.orderStatus)">{{ currentRow?.orderStatusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ currentRow?.totalAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">¥{{ currentRow?.paidAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="订单日期">{{ currentRow?.orderDate }}</el-descriptions-item>
        <el-descriptions-item label="预计到货日期">{{ currentRow?.expectedDate }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">订单明细</el-divider>
      <el-table :data="currentRow?.items" border>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="quantity" label="数量" width="100" align="right" />
        <el-table-column prop="price" label="单价" width="100" align="right" />
        <el-table-column prop="amount" label="金额" width="100" align="right" />
        <el-table-column prop="receivedQuantity" label="已入库数量" width="120" align="right" />
      </el-table>
    </el-dialog>

    <!-- 入库对话框 -->
    <el-dialog
      v-model="inboundDialogVisible"
      title="采购入库"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="2" border style="margin-bottom: 20px;">
        <el-descriptions-item label="订单编号">{{ inboundOrder?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ inboundOrder?.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ inboundOrder?.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ inboundOrder?.totalAmount?.toFixed(2) }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="inboundItems" border>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="quantity" label="订单数量" width="100" align="right" />
        <el-table-column prop="receivedQuantity" label="已入库数量" width="110" align="right" />
        <el-table-column prop="receivableQuantity" label="可入库数量" width="110" align="right" />
        <el-table-column label="本次入库数量" width="140">
          <template #default="{ row }">
            <el-input-number
              v-model="row.inboundQuantity"
              :min="0"
              :max="row.receivableQuantity"
              :precision="2"
              size="small"
              style="width: 100%;"
            />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="inboundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleInboundSubmit" :loading="inboundLoading">确认入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
import {
  getPurchaseOrderPage,
  getPurchaseOrderDetail,
  createPurchaseOrder,
  updatePurchaseOrder,
  submitPurchaseOrder,
  approvePurchaseOrder,
  cancelPurchaseOrder,
  purchaseInbound,
  getSupplierPage,
  getWarehouses,
  getProductPage
} from '@/api/erp'

const searchForm = reactive({
  orderNo: '',
  supplierId: undefined,
  status: undefined
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const loading = ref(false)
const supplierList = ref([])
const warehouseList = ref([])
const productList = ref([])

// 订单表单
const orderDialogVisible = ref(false)
const orderFormRef = ref()
const orderLoading = ref(false)
const isEdit = ref(false)
const currentRow = ref(null)
const orderForm = reactive({
  supplierId: undefined,
  warehouseId: undefined,
  expectedDate: null,
  handlerId: null,
  remark: '',
  items: []
})

const orderRules = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }]
}

// 详情对话框
const detailDialogVisible = ref(false)

// 入库对话框
const inboundDialogVisible = ref(false)
const inboundLoading = ref(false)
const inboundOrder = ref(null)
const inboundItems = ref([])

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
    row.price = product.costPrice || 0
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
    const res = await getPurchaseOrderPage({
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

const loadSuppliers = async () => {
  try {
    const res = await getSupplierPage({ page: 1, size: 1000 })
    supplierList.value = res.records || []
  } catch (error) {
    console.error('加载供应商列表失败', error)
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
  searchForm.supplierId = undefined
  searchForm.status = undefined
  handleSearch()
}

const handleCreate = () => {
  isEdit.value = false
  Object.assign(orderForm, {
    supplierId: undefined,
    warehouseId: undefined,
    expectedDate: null,
    handlerId: null,
    remark: '',
    items: []
  })
  orderDialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  try {
    const res = await getPurchaseOrderDetail(row.id)
    Object.assign(orderForm, {
      supplierId: res.supplierId,
      warehouseId: res.warehouseId,
      expectedDate: res.expectedDate,
      handlerId: res.handlerId,
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
        await updatePurchaseOrder(currentRow.value.id, orderForm)
        ElMessage.success('更新成功')
      } else {
        await createPurchaseOrder(orderForm)
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
    await submitPurchaseOrder(row.id)
    ElMessage.success('提交成功')
    loadData()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const handleApprove = async (row) => {
  try {
    await approvePurchaseOrder(row.id)
    ElMessage.success('审核成功')
    loadData()
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm('确认取消该订单？', '提示', { type: 'warning' })
  try {
    await cancelPurchaseOrder(row.id)
    ElMessage.success('取消成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('取消失败')
  }
}

const handleInbound = async (row) => {
  try {
    const res = await getPurchaseOrderDetail(row.id)
    inboundOrder.value = res
    inboundItems.value = (res.items || []).map(item => ({
      itemId: item.id,
      productName: item.productName,
      productCode: item.productCode,
      quantity: item.quantity,
      receivedQuantity: item.receivedQuantity || 0,
      receivableQuantity: (item.quantity || 0) - (item.receivedQuantity || 0),
      inboundQuantity: (item.quantity || 0) - (item.receivedQuantity || 0)
    }))
    inboundDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载订单详情失败')
  }
}

const handleInboundSubmit = async () => {
  const validItems = inboundItems.value.filter(item => item.inboundQuantity > 0)
  if (validItems.length === 0) {
    ElMessage.warning('请填写入库数量')
    return
  }
  inboundLoading.value = true
  try {
    await purchaseInbound(inboundOrder.value.id, {
      items: validItems.map(item => ({
        itemId: item.itemId,
        quantity: item.inboundQuantity
      }))
    })
    ElMessage.success('入库成功')
    inboundDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('入库失败')
  } finally {
    inboundLoading.value = false
  }
}

const handleView = async (row) => {
  try {
    const res = await getPurchaseOrderDetail(row.id)
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
  loadSuppliers()
  loadWarehouses()
  loadProducts()
  loadData()
})
</script>

<style scoped lang="scss">
.purchase-order-container {
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
