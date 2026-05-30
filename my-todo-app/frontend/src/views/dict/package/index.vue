<!-- views/dict/package/index.vue - SaaS套餐管理页面 -->
<template>
  <div class="package-container">
    <el-row :gutter="20">
      <!-- 左侧: 套餐列表 -->
      <el-col :span="14">
        <el-card class="package-card">
          <template #header>
            <div class="card-header">
              <span>SaaS套餐管理</span>
              <el-button type="primary" size="small" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                新增套餐
              </el-button>
            </div>
          </template>

          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="套餐名称">
              <el-input v-model="searchForm.packageName" placeholder="请输入套餐名称" clearable size="small" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" @click="loadPackages">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button size="small" @click="searchForm.packageName = ''; loadPackages()">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>

          <el-table :data="packageData" v-loading="packageLoading" border stripe>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="packageName" label="套餐名称" width="130" />
            <el-table-column prop="price" label="价格(元/月)" width="110" />
            <el-table-column prop="maxUsers" label="最大用户数" width="100" />
            <el-table-column prop="duration" label="有效期(天)" width="100" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '上架' : '下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="120" show-overflow-tooltip />
            <el-table-column label="操作" fixed="right" width="200">
              <template #default="{ row }">
                <el-button type="info" link size="small" @click="handleViewFeatures(row)">功能</el-button>
                <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧: 订阅列表 -->
      <el-col :span="10">
        <el-card class="subscribe-card">
          <template #header>
            <div class="card-header">
              <span>订阅列表</span>
              <el-button type="primary" size="small" @click="subscribeDialogVisible = true">
                订阅套餐
              </el-button>
            </div>
          </template>

          <el-table :data="subscriptionData" v-loading="subLoading" border stripe max-height="500">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="tenantName" label="租户" width="110" show-overflow-tooltip />
            <el-table-column prop="packageName" label="套餐" width="100" />
            <el-table-column prop="expireTime" label="到期时间" min-width="130" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : row.status === 'EXPIRED' ? 'danger' : 'warning'" size="small">
                  {{ row.status === 'ACTIVE' ? '生效' : row.status === 'EXPIRED' ? '过期' : '待生效' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 套餐新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="套餐名称" prop="packageName">
          <el-input v-model="form.packageName" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="价格(元/月)" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最大用户数" prop="maxUsers">
          <el-input-number v-model="form.maxUsers" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="有效期(天)" prop="duration">
          <el-input-number v-model="form.duration" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 套餐功能对话框 -->
    <el-dialog
      v-model="featureDialogVisible"
      title="套餐功能列表"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-table :data="featureData" v-loading="featureLoading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="featureName" label="功能名称" width="140" />
        <el-table-column prop="featureCode" label="功能编码" width="140" />
        <el-table-column prop="enabled" label="是否启用" width="90">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 订阅对话框 -->
    <el-dialog
      v-model="subscribeDialogVisible"
      title="订阅套餐"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form ref="subFormRef" :model="subForm" :rules="subRules" label-width="100px">
        <el-form-item label="租户ID" prop="tenantId">
          <el-input v-model="subForm.tenantId" placeholder="请输入租户ID" />
        </el-form-item>
        <el-form-item label="套餐" prop="packageId">
          <el-select v-model="subForm.packageId" placeholder="请选择套餐" style="width: 100%">
            <el-option
              v-for="pkg in packageData.filter(p => p.status === 1)"
              :key="pkg.id"
              :label="`${pkg.packageName} - ${pkg.price}元/月`"
              :value="pkg.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="subscribeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubscribe" :loading="subSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getPackages,
  createPackage,
  updatePackage,
  deletePackage,
  getPackageFeatures,
  subscribePackage,
  getSubscriptions
} from '@/api/dict'

// ==================== 套餐列表相关 ====================
const searchForm = reactive({ packageName: '' })
const packageData = ref([])
const packageLoading = ref(false)

const loadPackages = async () => {
  packageLoading.value = true
  try {
    const res = await getPackages(searchForm)
    packageData.value = res?.records || []
  } catch (error) {
    ElMessage.error('加载套餐列表失败')
  } finally {
    packageLoading.value = false
  }
}

// ==================== 套餐对话框 ====================
const dialogVisible = ref(false)
const dialogTitle = ref('新增套餐')
const formRef = ref()
const submitLoading = ref(false)

const form = reactive({
  id: null,
  packageName: '',
  price: 0,
  maxUsers: 10,
  duration: 30,
  description: '',
  status: 1
})

const rules = {
  packageName: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  maxUsers: [{ required: true, message: '请输入最大用户数', trigger: 'blur' }]
}

const handleAdd = () => {
  dialogTitle.value = '新增套餐'
  Object.assign(form, {
    id: null, packageName: '', price: 0, maxUsers: 10,
    duration: 30, description: '', status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑套餐'
  Object.assign(form, row)
  form.id = row.id
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (form.id) {
        await updatePackage(form.id, { ...form })
        ElMessage.success('更新成功')
      } else {
        await createPackage({ ...form })
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadPackages()
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定要删除套餐"${row.packageName}"吗？`, '提示', { type: 'warning' })
  try {
    await deletePackage(row.id)
    ElMessage.success('删除成功')
    loadPackages()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// ==================== 套餐功能 ====================
const featureDialogVisible = ref(false)
const featureData = ref([])
const featureLoading = ref(false)

const handleViewFeatures = async (row) => {
  featureDialogVisible.value = true
  featureLoading.value = true
  try {
    const res = await getPackageFeatures(row.id)
    featureData.value = res || []
  } catch (error) {
    ElMessage.error('加载功能列表失败')
  } finally {
    featureLoading.value = false
  }
}

// ==================== 订阅相关 ====================
const subscriptionData = ref([])
const subLoading = ref(false)
const subscribeDialogVisible = ref(false)
const subFormRef = ref()
const subSubmitLoading = ref(false)
const subForm = reactive({ tenantId: '', packageId: null })
const subRules = {
  tenantId: [{ required: true, message: '请输入租户ID', trigger: 'blur' }],
  packageId: [{ required: true, message: '请选择套餐', trigger: 'change' }]
}

const loadSubscriptions = async () => {
  subLoading.value = true
  try {
    const res = await getSubscriptions({})
    subscriptionData.value = res?.records || []
  } catch (error) {
    ElMessage.error('加载订阅列表失败')
  } finally {
    subLoading.value = false
  }
}

const handleSubscribe = async () => {
  if (!subFormRef.value) return
  await subFormRef.value.validate(async (valid) => {
    if (!valid) return
    subSubmitLoading.value = true
    try {
      await subscribePackage({ ...subForm })
      ElMessage.success('订阅成功')
      subscribeDialogVisible.value = false
      loadSubscriptions()
    } catch (error) {
      ElMessage.error('订阅失败')
    } finally {
      subSubmitLoading.value = false
    }
  })
}

// ==================== 初始化 ====================
onMounted(() => {
  loadPackages()
  loadSubscriptions()
})
</script>

<style scoped lang="scss">
.package-container {
  padding: 20px;

  .package-card,
  .subscribe-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .search-form {
      margin-bottom: 12px;
    }
  }
}
</style>
