<!-- views/system/user/index.vue - 用户管理页面
  提供用户的搜索、新增、编辑、删除、启用/禁用等功能
  包含搜索栏、数据表格（带分页）、新增/编辑对话框
-->
<template>
  <div class="user-container">
    <!-- 搜索栏区域：支持按用户名、姓名、状态筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入姓名" clearable />
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

    <!-- 用户数据表格区域 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件：支持切换每页条数和页码 -->
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

    <!-- 新增/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// 用户管理页面逻辑：搜索、分页、增删改查、启用/禁用用户
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getUserPage, createUser, updateUser, deleteUser, enableUser, disableUser } from '@/api/user'

// ===== 搜索相关 =====

// 搜索表单数据
const searchForm = reactive({
  username: '',                              // 按用户名筛选
  realName: '',                              // 按姓名筛选
  status: undefined                          // 按状态筛选（1-启用，0-禁用）
})

// ===== 分页相关 =====

// 分页参数
const pagination = reactive({
  page: 1,       // 当前页码
  size: 10,      // 每页条数
  total: 0       // 总记录数
})

// ===== 表格相关 =====

// 用户表格数据列表
const tableData = ref([])
// 表格加载状态
const loading = ref(false)

// ===== 对话框相关 =====

// 对话框是否可见
const dialogVisible = ref(false)
// 对话框标题（新增/编辑）
const dialogTitle = ref('新增用户')
// 表单引用，用于触发表单验证
const formRef = ref()
// 提交按钮加载状态，防止重复提交
const submitLoading = ref(false)

// 表单数据（新增/编辑共用）
const formData = reactive({
  id: undefined,                         // 用户ID（编辑时有值，string类型）
  username: '',                          // 用户名
  realName: '',                          // 真实姓名
  email: '',                             // 邮箱
  phone: '',                             // 手机号
  status: 1                              // 状态（默认启用）
})

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

/**
 * 加载用户列表数据
 * 根据当前分页参数和搜索条件从后端获取数据
 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserPage({
      page: pagination.page,
      size: pagination.size,
      ...searchForm   // 展开搜索条件
    })
    tableData.value = res.records    // 填充表格数据
    pagination.total = res.total     // 更新总记录数
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索按钮处理：重置页码到第一页后加载数据
 */
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

/**
 * 重置按钮处理：清空搜索条件后重新搜索
 */
const handleReset = () => {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.status = undefined
  handleSearch()
}

/**
 * 新增按钮处理：重置表单数据并打开对话框
 */
const handleAdd = () => {
  dialogTitle.value = '新增用户'
  formData.id = undefined
  formData.username = ''
  formData.realName = ''
  formData.email = ''
  formData.phone = ''
  formData.status = 1
  dialogVisible.value = true
}

/**
 * 编辑按钮处理：将当前行数据填充到表单并打开对话框
 * @param {Object} row 当前行的用户数据
 */
const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  formData.id = String(row.id)  // 确保id是字符串类型
  formData.username = row.username
  formData.realName = row.realName
  formData.email = row.email
  formData.phone = row.phone
  formData.status = row.status
  dialogVisible.value = true
}

/**
 * 表单提交处理：验证通过后根据是否有 ID 判断是创建还是更新
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (formData.id) {
        // 有 ID 表示编辑模式，调用更新接口
        await updateUser(formData.id, formData)
        ElMessage.success('更新成功')
      } else {
        // 无 ID 表示新增模式，调用创建接口
        await createUser(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()  // 提交成功后重新加载列表数据
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

/**
 * 切换用户启用/禁用状态
 * @param row 当前行的用户数据
 */
const handleToggleStatus = async (row) => {
  // 根据当前状态确定操作文本（启用 -> 禁用 / 禁用 -> 启用）
  const action = row.status === 1 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定要${action}该用户吗?`, '提示', {
    type: 'warning'
  })
  try {
    if (row.status === 1) {
      await disableUser(row.id)   // 当前启用 -> 调用禁用接口
    } else {
      await enableUser(row.id)    // 当前禁用 -> 调用启用接口
    }
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (error) {
    ElMessage.error(`${action}失败`)
  }
}

/**
 * 删除用户（需二次确认）
 * @param row 当前行的用户数据
 */
const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该用户吗?', '提示', {
    type: 'warning'
  })
  try {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

/**
 * 每页条数变化处理
 * @param size 新的每页条数
 */
const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

/**
 * 页码变化处理
 * @param page 新的页码
 */
const handlePageChange = (page) => {
  pagination.page = page
  loadData()
}

// 页面挂载时加载第一页数据
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.user-container {
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
