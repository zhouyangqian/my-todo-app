<!-- views/system/role/index.vue - 角色管理页面
  提供角色的搜索、新增、编辑、删除、分配权限等功能
  包含搜索栏、数据表格、新增/编辑对话框、权限分配对话框
-->
<template>
  <div class="role-container">
    <!-- 搜索栏区域：支持按角色名称、编码、状态筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.name" placeholder="请输入角色名称" clearable />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="searchForm.code" placeholder="请输入角色编码" clearable />
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

    <!-- 角色数据表格区域 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>角色列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增角色
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" width="150" />
        <el-table-column prop="code" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleAssignPermission(row)">
              分配权限
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑角色对话框 -->
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
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
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

    <!-- 分配权限对话框：使用树形组件展示权限层级，支持勾选 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="分配权限"
      width="500px"
      :close-on-click-modal="false"
    >
      <!-- 权限树组件，以 checkbox 形式展示，默认展开所有节点 -->
      <el-tree
        ref="treeRef"
        :data="permissionTree"
        :props="{ label: 'name', children: 'children' }"
        show-checkbox
        node-key="id"
        default-expand-all
        :default-checked-keys="checkedPermissionIds"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePermission" :loading="permissionLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
// 角色管理页面逻辑：角色的增删改查、权限树加载与分配
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getRoleList,
  createRole,
  updateRole,
  deleteRole,
  getPermissionTree,
  getRolePermissions,
  assignRolePermissions,
  type Role,
  type Permission
} from '@/api/permission'

// ===== 搜索相关 =====

// 搜索表单数据
const searchForm = reactive({
  name: '',                                  // 按角色名称筛选
  code: '',                                  // 按角色编码筛选
  status: undefined as number | undefined    // 按状态筛选
})

// ===== 表格相关 =====

// 角色表格数据列表
const tableData = ref<Role[]>([])
// 表格加载状态
const loading = ref(false)

// ===== 对话框相关 =====

// 角色编辑对话框是否可见
const dialogVisible = ref(false)
// 对话框标题
const dialogTitle = ref('新增角色')
// 表单引用
const formRef = ref<FormInstance>()
// 提交按钮加载状态
const submitLoading = ref(false)

// 角色表单数据
const formData = reactive({
  id: undefined as number | undefined,  // 角色ID（编辑时有值）
  name: '',                              // 角色名称
  code: '',                              // 角色编码
  description: '',                       // 角色描述
  status: 1                              // 状态（默认启用）
})

// 表单验证规则：名称必填，编码只允许字母和下划线
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z_]+$/, message: '只能包含字母和下划线', trigger: 'blur' }
  ]
}

// ===== 权限分配相关 =====

// 权限分配对话框是否可见
const permissionDialogVisible = ref(false)
// 权限树数据（完整的权限层级结构）
const permissionTree = ref<Permission[]>([])
// 当前角色已选中的权限ID列表（用于树的默认选中）
const checkedPermissionIds = ref<number[]>([])
// 当前正在分配权限的角色ID
const currentRoleId = ref<number>()
// 权限树组件引用
const treeRef = ref()
// 权限保存按钮加载状态
const permissionLoading = ref(false)

/**
 * 加载角色列表数据
 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getRoleList(searchForm)
    tableData.value = res
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载完整的权限树数据（首次打开权限分配时调用）
 */
const loadPermissionTree = async () => {
  try {
    const res = await getPermissionTree()
    permissionTree.value = res
  } catch (error) {
    ElMessage.error('加载权限树失败')
  }
}

/**
 * 搜索按钮处理：重新加载数据
 */
const handleSearch = () => {
  loadData()
}

/**
 * 重置按钮处理：清空搜索条件后重新搜索
 */
const handleReset = () => {
  searchForm.name = ''
  searchForm.code = ''
  searchForm.status = undefined
  handleSearch()
}

/**
 * 新增角色：重置表单并打开对话框
 */
const handleAdd = () => {
  dialogTitle.value = '新增角色'
  formData.id = undefined
  formData.name = ''
  formData.code = ''
  formData.description = ''
  formData.status = 1
  dialogVisible.value = true
}

/**
 * 编辑角色：将当前行数据填充到表单
 * @param row 当前行角色数据
 */
const handleEdit = (row: Role) => {
  dialogTitle.value = '编辑角色'
  formData.id = row.id
  formData.name = row.name
  formData.code = row.code
  formData.description = row.description
  formData.status = row.status
  dialogVisible.value = true
}

/**
 * 表单提交处理：验证后根据 ID 判断新增或更新
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (formData.id) {
        // 有 ID 表示编辑模式
        await updateRole(formData.id, formData)
        ElMessage.success('更新成功')
      } else {
        // 无 ID 表示新增模式
        await createRole(formData)
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

/**
 * 删除角色（需二次确认）
 * @param row 当前行角色数据
 */
const handleDelete = async (row: Role) => {
  await ElMessageBox.confirm('确定要删除该角色吗?', '提示', {
    type: 'warning'
  })
  try {
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

/**
 * 打开权限分配对话框
 * 1. 首次打开时加载权限树
 * 2. 获取该角色当前已分配的权限ID
 * @param row 当前行角色数据
 */
const handleAssignPermission = async (row: Role) => {
  currentRoleId.value = row.id
  // 首次打开时加载权限树（后续使用缓存）
  if (permissionTree.value.length === 0) {
    await loadPermissionTree()
  }
  // 获取该角色已拥有的权限ID，设置为树的默认选中项
  try {
    const permissionIds = await getRolePermissions(row.id)
    checkedPermissionIds.value = permissionIds
  } catch (error) {
    checkedPermissionIds.value = []
  }
  permissionDialogVisible.value = true
}

/**
 * 保存权限分配
 * 获取树中所有勾选的权限节点ID，调用接口保存
 */
const handleSavePermission = async () => {
  if (!currentRoleId.value || !treeRef.value) return
  permissionLoading.value = true
  try {
    // 获取所有被勾选的权限节点ID（不包括半选的父节点）
    const checkedKeys = treeRef.value.getCheckedKeys(false) as number[]
    await assignRolePermissions(currentRoleId.value, checkedKeys)
    ElMessage.success('保存成功')
    permissionDialogVisible.value = false
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    permissionLoading.value = false
  }
}

// 页面挂载时加载角色列表
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.role-container {
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
  }
}
</style>
