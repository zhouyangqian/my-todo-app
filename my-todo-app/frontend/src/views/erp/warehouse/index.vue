<!-- views/erp/warehouse/index.vue - 仓库管理页面 -->
<template>
  <div class="warehouse-container">
    <!-- 搜索栏区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="仓库名称">
          <el-input v-model="searchForm.warehouseName" placeholder="请输入仓库名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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
          <span>仓库列表</span>
          <el-button type="primary" @click="handleAdd" v-if="userStore.hasPermission('erp:warehouse:create')">
            <el-icon><Plus /></el-icon>
            新增仓库
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="warehouseName" label="仓库名称" width="150" />
        <el-table-column prop="warehouseCode" label="仓库编码" width="120" />
        <el-table-column prop="warehouseType" label="仓库类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getWarehouseTypeText(row.warehouseType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="isDefault" label="默认仓库" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isDefault === 1 ? 'success' : 'info'" size="small">
              {{ row.isDefault === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="220">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)" v-if="userStore.hasPermission('erp:warehouse:update')">编辑</el-button>
            <el-button type="warning" link @click="handleSetDefault(row)" v-if="row.isDefault !== 1 && userStore.hasPermission('erp:warehouse:setDefault')">
              设为默认
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-if="userStore.hasPermission('erp:warehouse:delete')">删除</el-button>
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
        <el-form-item label="仓库名称" prop="warehouseName">
          <el-input v-model="formData.warehouseName" placeholder="请输入仓库名称" />
        </el-form-item>
        <el-form-item label="仓库类型" prop="warehouseType">
          <el-select v-model="formData.warehouseType" placeholder="请选择仓库类型" style="width: 100%;">
            <el-option label="普通仓" :value="1" />
            <el-option label="门店仓" :value="2" />
            <el-option label="虚拟仓" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入仓库地址" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="formData.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
import { getWarehousePage, createWarehouse, updateWarehouse, deleteWarehouse, setDefaultWarehouse } from '@/api/erp'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const searchForm = reactive({
  warehouseName: '',
  status: undefined
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增仓库')
const formRef = ref()
const submitLoading = ref(false)

const formData = reactive({
  warehouseName: '',
  warehouseType: 1,
  address: '',
  contact: '',
  contactPhone: '',
  status: 1,
  remark: ''
})

const formRules = {
  warehouseName: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }],
  warehouseType: [{ required: true, message: '请选择仓库类型', trigger: 'change' }]
}

const getWarehouseTypeText = (type) => {
  const texts = { 1: '普通仓', 2: '门店仓', 3: '虚拟仓' }
  return texts[type] || '未知'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getWarehousePage({
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

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.warehouseName = ''
  searchForm.status = undefined
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增仓库'
  Object.assign(formData, {
    warehouseName: '',
    warehouseType: 1,
    address: '',
    contact: '',
    contactPhone: '',
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑仓库'
  Object.assign(formData, row)
  formData.id = String(row.id)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (formData.id) {
        await updateWarehouse(formData.id, formData)
        ElMessage.success('更新成功')
      } else {
        await createWarehouse(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleSetDefault = async (row) => {
  await ElMessageBox.confirm('确定要设置该仓库为默认仓库吗?', '提示', { type: 'warning' })
  try {
    await setDefaultWarehouse(row.id)
    ElMessage.success('设置成功')
    loadData()
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该仓库吗?', '提示', { type: 'warning' })
  try {
    await deleteWarehouse(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
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
  loadData()
})
</script>

<style scoped lang="scss">
.warehouse-container {
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
