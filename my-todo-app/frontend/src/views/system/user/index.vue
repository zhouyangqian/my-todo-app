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
        <el-table-column label="操作" fixed="right" width="320">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleResetPassword(row)">设置密码</el-button>
            <el-button type="warning" link @click="handleAssignRole(row)">分配角色</el-button>
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
          <el-input v-model="formData.username" placeholder="请输入用户名" @input="onUsernameInput" />
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

    <!-- 分配角色对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="分配角色"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-checkbox-group v-model="checkedRoleIds">
        <div v-for="role in allRoles" :key="role.id" style="margin-bottom: 10px;">
          <el-checkbox :value="String(role.id)">
            {{ role.roleName }}（{{ role.roleCode }}）
          </el-checkbox>
        </div>
      </el-checkbox-group>
      <el-empty v-if="allRoles.length === 0" description="暂无可用角色" />
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRoles" :loading="roleLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 设置密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="设置密码"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="用户">
          <el-input :model-value="passwordForm.username" disabled />
        </el-form-item>
        <el-form-item label="默认密码">
          <el-input :model-value="passwordForm.defaultPassword" disabled>
            <template #append>
              <el-button @click="handleCopyDefault">复制</el-button>
            </template>
          </el-input>
          <div class="form-tip">规则：姓名 + 用户总数</div>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" placeholder="可修改为自定义密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePassword" :loading="passwordLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// 用户管理页面逻辑：搜索、分页、增删改查、启用/禁用用户
import { ref, reactive, onMounted, watch } from 'vue'
import { pinyin } from 'pinyin-pro'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getUserPage, createUser, updateUser, deleteUser, enableUser, disableUser } from '@/api/user'
import { getRoleList, getUserRoles, assignUserRoles } from '@/api/permission'
import { resetPassword } from '@/api/auth'

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

// ===== 角色分配相关 =====

// 角色分配对话框是否可见
const roleDialogVisible = ref(false)
// 所有角色列表（用于勾选）
const allRoles = ref([])
// 当前用户已分配的角色ID列表
const checkedRoleIds = ref([])
// 当前正在分配角色的用户ID
const currentUserId = ref()
// 角色分配按钮加载状态
const roleLoading = ref(false)

// ===== 设置密码相关 =====

// 设置密码对话框是否可见
const passwordDialogVisible = ref(false)
// 密码表单数据
const passwordForm = reactive({
  userId: undefined,
  username: '',
  defaultPassword: '',
  newPassword: ''
})
// 密码保存按钮加载状态
const passwordLoading = ref(false)

// 用户名是否被手动修改过（控制拼音自动生成）
const usernameManuallyEdited = ref(false)

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

// 监听姓名变化，自动生成拼音用户名（仅新增模式且未手动修改时）
watch(() => formData.realName, (val) => {
  if (!val || formData.id || usernameManuallyEdited.value) return
  const py = pinyin(val, { toneType: 'none', type: 'array' })
  formData.username = py.map(s => s.charAt(0).toUpperCase() + s.slice(1)).join('')
})

// 用户手动输入用户名时标记，后续不再自动覆盖
const onUsernameInput = () => {
  usernameManuallyEdited.value = true
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
  usernameManuallyEdited.value = false
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
  usernameManuallyEdited.value = true  // 编辑模式下不自动生成
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
 * 打开设置密码对话框
 * 默认密码规则：姓名全拼 + 用户总数
 * @param {Object} row 当前行用户数据
 */
const handleResetPassword = (row) => {
  const namePinyin = row.realName
    ? pinyin(row.realName, { toneType: 'none', type: 'array' }).join('')
    : ''
  const defaultPwd = namePinyin + pagination.total
  passwordForm.userId = row.id
  passwordForm.username = row.username
  passwordForm.defaultPassword = defaultPwd
  passwordForm.newPassword = defaultPwd
  passwordDialogVisible.value = true
}

/**
 * 复制默认密码到剪贴板
 */
const handleCopyDefault = () => {
  navigator.clipboard.writeText(passwordForm.defaultPassword)
  ElMessage.success('已复制到剪贴板')
}

/**
 * 保存密码（调用重置密码接口）
 */
const handleSavePassword = async () => {
  if (!passwordForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  passwordLoading.value = true
  try {
    await resetPassword({
      userId: passwordForm.userId,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码设置成功')
    passwordDialogVisible.value = false
  } catch (error) {
    ElMessage.error('密码设置失败')
  } finally {
    passwordLoading.value = false
  }
}

/**
 * 打开角色分配对话框
 * 1. 首次打开时加载所有角色列表
 * 2. 获取该用户当前已分配的角色ID
 * @param {Object} row 当前行用户数据
 */
const handleAssignRole = async (row) => {
  currentUserId.value = row.id
  // 首次打开时加载角色列表（后续使用缓存）
  if (allRoles.value.length === 0) {
    try {
      const res = await getRoleList({ page: 1, size: 999 })
      allRoles.value = res.records || []
    } catch (error) {
      ElMessage.error('加载角色列表失败')
      return
    }
  }
  // 获取该用户已拥有的角色（后端返回完整Role对象，需提取ID）
  let roleIds = []
  try {
    const roles = await getUserRoles(row.id)
    roleIds = (roles || []).map(r => String(r.id))
  } catch (error) {
    roleIds = []
  }
  checkedRoleIds.value = roleIds
  roleDialogVisible.value = true
}

/**
 * 保存角色分配
 * 获取所有勾选的角色ID，调用接口保存
 */
const handleSaveRoles = async () => {
  if (!currentUserId.value) return
  roleLoading.value = true
  try {
    await assignUserRoles(currentUserId.value, checkedRoleIds.value)
    ElMessage.success('保存成功')
    roleDialogVisible.value = false
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    roleLoading.value = false
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

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}
</style>
