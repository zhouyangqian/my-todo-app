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
          <el-input v-model="searchForm.userName" placeholder="请输入用户名" clearable />
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
          <div class="card-header-left">
            <span>用户列表</span>
            <el-button v-if="userStore.hasPermission('system:user:create')" type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增用户
            </el-button>
            <!-- 批量操作按钮 -->
            <el-button
              v-if="userStore.hasPermission('system:user:disable')"
              type="warning"
              :disabled="selectedRows.length === 0"
              @click="handleBatchDisable"
            >
              批量禁用 ({{ selectedRows.length }})
            </el-button>
            <el-button
              v-if="userStore.hasPermission('system:user:delete')"
              type="danger"
              :disabled="selectedRows.length === 0"
              @click="handleBatchDelete"
            >
              批量删除 ({{ selectedRows.length }})
            </el-button>
            <el-button
              v-if="userStore.hasPermission('system:user:assignToUser')"
              type="info"
              :disabled="selectedRows.length === 0"
              @click="handleBatchAssignRoles"
            >
              批量分配角色 ({{ selectedRows.length }})
            </el-button>
          </div>
          <div class="card-header-right">
            <el-button v-if="userStore.hasPermission('system:user:import')" @click="handleDownloadTemplate">
              下载模板
            </el-button>
            <el-button v-if="userStore.hasPermission('system:user:import')" type="success" @click="importDialogVisible = true">
              导入
            </el-button>
            <el-button v-if="userStore.hasPermission('system:user:export')" type="warning" @click="handleExport">
              导出
            </el-button>
          </div>
        </div>
      </template>

      <el-table ref="tableRef" :data="tableData" v-loading="loading" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="userName" label="用户名" width="120" />
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
        <el-table-column label="操作" fixed="right" width="380">
          <template #default="{ row }">
            <el-button v-if="userStore.hasPermission('system:user:update')" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="userStore.hasPermission('user:reset-password') || userStore.hasRole('admin')"
              type="success"
              link
              @click="handleResetPassword(row)"
            >
              设置密码
            </el-button>
            <el-button v-if="userStore.hasPermission('system:user:assignToUser')" type="warning" link @click="handleAssignRole(row)">分配角色</el-button>
            <el-button
              v-if="userStore.hasPermission('system:user:enable')"
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button v-if="userStore.hasPermission('system:user:delete')" type="danger" link @click="handleDelete(row)">删除</el-button>
            <el-button
              v-if="userStore.hasPermission('system:user:kick')"
              type="danger"
              link
              @click="handleKick(row)"
            >
              踢出
            </el-button>
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
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="formData.userName" placeholder="请输入用户名" @input="onUsernameInput" />
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
          <el-input :model-value="passwordForm.userName" disabled />
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

    <!-- 导入用户对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入用户"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleImportFileChange"
        :on-exceed="() => ElMessage.warning('只能上传一个文件')"
        drag
      >
        <el-icon style="font-size: 40px; color: #c0c4cc;"><Upload /></el-icon>
        <div style="margin-top: 10px;">将Excel文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="form-tip">仅支持 .xlsx / .xls 格式，单次最多导入1000条数据</div>
        </template>
      </el-upload>
      <!-- 导入结果展示 -->
      <div v-if="importResult" style="margin-top: 16px;">
        <el-alert
          :title="`导入完成：成功 ${importResult.successCount} 条，失败 ${importResult.failCount} 条，共 ${importResult.totalCount} 条`"
          :type="importResult.failCount > 0 ? 'warning' : 'success'"
          show-icon
          :closable="false"
        />
        <el-table v-if="importResult.failures && importResult.failures.length > 0" :data="importResult.failures" border stripe style="margin-top: 10px; max-height: 200px; overflow-y: auto;">
          <el-table-column prop="rowNum" label="行号" width="80" />
          <el-table-column prop="userName" label="用户名" width="120" />
          <el-table-column prop="reason" label="失败原因" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="handleImportDialogClose">关闭</el-button>
        <el-button type="primary" @click="handleImportSubmit" :loading="importLoading" :disabled="!importFile">
          开始导入
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量分配角色对话框 -->
    <el-dialog
      v-model="batchRoleDialogVisible"
      title="批量分配角色"
      width="500px"
      :close-on-click-modal="false"
    >
      <p style="margin-bottom: 12px; color: #606266;">已选择 {{ selectedRows.length }} 个用户</p>
      <el-checkbox-group v-model="batchRoleIds">
        <div v-for="role in allRoles" :key="role.id" style="margin-bottom: 10px;">
          <el-checkbox :value="String(role.id)">
            {{ role.roleName }}（{{ role.roleCode }}）
          </el-checkbox>
        </div>
      </el-checkbox-group>
      <el-empty v-if="allRoles.length === 0" description="暂无可用角色" />
      <template #footer>
        <el-button @click="batchRoleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchSaveRoles" :loading="batchRoleLoading">
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
import { Search, Refresh, Plus, Upload } from '@element-plus/icons-vue'
import { getUserPage, createUser, updateUser, deleteUser, enableUser, disableUser, batchDisableUsers, batchDeleteUsers, batchAssignRoles, downloadImportTemplate, importUsers, exportUsers } from '@/api/user'
import { getRoleList, getUserRoles, assignUserRoles, clearUserPermissionCache } from '@/api/permission'
import { resetPassword, kickUser } from '@/api/auth'
import { useUserStore } from '@/stores/user'

// 用户状态管理
const userStore = useUserStore()

// ===== 搜索相关 =====

// 搜索表单数据
const searchForm = reactive({
  userName: '',                              // 按用户名筛选
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
// 表格引用，用于多选操作
const tableRef = ref()
// 当前选中行
const selectedRows = ref([])

// ===== 批量操作相关 =====

// 批量分配角色对话框是否可见
const batchRoleDialogVisible = ref(false)
// 批量分配角色选中的角色ID列表
const batchRoleIds = ref([])
// 批量分配角色加载状态
const batchRoleLoading = ref(false)

// ===== 导入导出相关 =====

// 导入对话框是否可见
const importDialogVisible = ref(false)
// 导入文件
const importFile = ref(null)
// 导入加载状态
const importLoading = ref(false)
// 导入结果
const importResult = ref(null)
// 上传组件引用
const uploadRef = ref()

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
  userName: '',                          // 用户名
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
  userName: '',
  defaultPassword: '',
  newPassword: ''
})
// 密码保存按钮加载状态
const passwordLoading = ref(false)

// 用户名是否被手动修改过（控制拼音自动生成）
const usernameManuallyEdited = ref(false)

// 表单验证规则
const formRules = {
  userName: [
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
  formData.userName = py.map(s => s.charAt(0).toUpperCase() + s.slice(1)).join('')
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
    tableData.value = res.records || []    // 填充表格数据
    pagination.total = res.total || 0      // 更新总记录数
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
  searchForm.userName = ''
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
  formData.userName = ''
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
  formData.userName = row.userName
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
 * 踢出用户（强制下线，需二次确认）
 * @param row 当前行的用户数据
 */
const handleKick = async (row) => {
  await ElMessageBox.confirm(`确定要将用户「${row.userName}」强制下线吗？该用户的所有设备将被踢出。`, '踢出确认', {
    type: 'warning',
    confirmButtonText: '确定踢出',
    cancelButtonText: '取消'
  })
  try {
    await kickUser(row.id)
    ElMessage.success('已将用户踢出')
  } catch (error) {
    ElMessage.error('踢出失败')
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
  passwordForm.userName = row.userName
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
    // 清除目标用户的权限缓存，确保下次请求获取最新数据
    await clearUserPermissionCache(currentUserId.value)
    // 如果修改的是自己的角色，刷新当前用户的权限
    if (String(currentUserId.value) === String(userStore.userInfo?.userId)) {
      await userStore.refreshPermissions()
    }
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

// ===== 多选与批量操作 =====

/**
 * 表格选中行变化处理
 */
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

/**
 * 批量禁用用户
 */
const handleBatchDisable = async () => {
  const userIds = selectedRows.value.map(row => row.id)
  await ElMessageBox.confirm(`确定要批量禁用 ${userIds.length} 个用户吗?`, '批量禁用', { type: 'warning' })
  try {
    const result = await batchDisableUsers({ userIds })
    if (result.failCount > 0) {
      ElMessage.warning(`批量禁用完成：成功${result.successCount}个，失败${result.failCount}个`)
    } else {
      ElMessage.success(`批量禁用成功：${result.successCount}个`)
    }
    tableRef.value?.clearSelection()
    loadData()
  } catch (error) {
    ElMessage.error('批量禁用失败')
  }
}

/**
 * 批量删除用户
 */
const handleBatchDelete = async () => {
  const userIds = selectedRows.value.map(row => row.id)
  await ElMessageBox.confirm(`确定要批量删除 ${userIds.length} 个用户吗？删除后不可恢复。`, '批量删除', { type: 'warning' })
  try {
    const result = await batchDeleteUsers({ userIds })
    if (result.failCount > 0) {
      ElMessage.warning(`批量删除完成：成功${result.successCount}个，失败${result.failCount}个`)
    } else {
      ElMessage.success(`批量删除成功：${result.successCount}个`)
    }
    tableRef.value?.clearSelection()
    loadData()
  } catch (error) {
    ElMessage.error('批量删除失败')
  }
}

/**
 * 打开批量分配角色对话框
 */
const handleBatchAssignRoles = async () => {
  if (allRoles.value.length === 0) {
    try {
      const res = await getRoleList({ page: 1, size: 999 })
      allRoles.value = res.records || []
    } catch (error) {
      ElMessage.error('加载角色列表失败')
      return
    }
  }
  batchRoleIds.value = []
  batchRoleDialogVisible.value = true
}

/**
 * 保存批量分配角色
 */
const handleBatchSaveRoles = async () => {
  if (batchRoleIds.value.length === 0) {
    ElMessage.warning('请至少选择一个角色')
    return
  }
  const userIds = selectedRows.value.map(row => row.id)
  batchRoleLoading.value = true
  try {
    const result = await batchAssignRoles({ userIds, roleIds: batchRoleIds.value.map(Number) })
    if (result.failCount > 0) {
      ElMessage.warning(`批量分配完成：成功${result.successCount}个，失败${result.failCount}个`)
    } else {
      ElMessage.success(`批量分配成功：${result.successCount}个`)
    }
    batchRoleDialogVisible.value = false
    tableRef.value?.clearSelection()
    loadData()
  } catch (error) {
    ElMessage.error('批量分配角色失败')
  } finally {
    batchRoleLoading.value = false
  }
}

// ===== 导入导出 =====

/**
 * 下载导入模板
 */
const handleDownloadTemplate = async () => {
  try {
    const response = await downloadImportTemplate()
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'user_import_template.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (error) {
    ElMessage.error('模板下载失败')
  }
}

/**
 * 导入文件变化处理
 */
const handleImportFileChange = (file) => {
  importFile.value = file.raw
}

/**
 * 提交导入
 */
const handleImportSubmit = async () => {
  if (!importFile.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  importLoading.value = true
  try {
    const result = await importUsers(importFile.value)
    importResult.value = result
    if (result.failCount === 0) {
      ElMessage.success(`导入成功：${result.successCount}条`)
    } else {
      ElMessage.warning(`导入完成：成功${result.successCount}条，失败${result.failCount}条`)
    }
    loadData()
  } catch (error) {
    ElMessage.error('导入失败')
  } finally {
    importLoading.value = false
  }
}

/**
 * 关闭导入对话框时重置状态
 */
const handleImportDialogClose = () => {
  importDialogVisible.value = false
  importFile.value = null
  importResult.value = null
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

/**
 * 导出用户列表
 */
const handleExport = async () => {
  try {
    const response = await exportUsers({
      userName: searchForm.userName || undefined,
      realName: searchForm.realName || undefined,
      status: searchForm.status
    })
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'users_export.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
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
      flex-wrap: wrap;
      gap: 10px;

      .card-header-left {
        display: flex;
        align-items: center;
        gap: 10px;
        flex-wrap: wrap;
      }

      .card-header-right {
        display: flex;
        align-items: center;
        gap: 8px;
      }
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
