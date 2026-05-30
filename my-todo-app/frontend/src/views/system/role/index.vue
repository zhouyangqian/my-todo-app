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
          <el-button v-if="userStore.hasPermission('system:role:create')" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增角色
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
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
            <el-button v-if="userStore.hasPermission('system:role:update')" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="userStore.hasPermission('system:role:assignPerm')" type="warning" link @click="handleAssignPermission(row)">
              分配权限
            </el-button>
            <el-button v-if="userStore.hasPermission('system:role:delete')" type="danger" link @click="handleDelete(row)">删除</el-button>
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
        :props="{ label: 'permissionName', children: 'children' }"
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

<script setup>
// 角色管理页面逻辑：角色的增删改查、权限树加载与分配
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getRoleList,
  createRole,
  updateRole,
  deleteRole,
  getPermissionTree,
  getRolePermissions,
  assignRolePermissions,
  clearUserPermissionCache
} from '@/api/permission'

// 用户状态管理
const userStore = useUserStore()

// ===== 搜索相关 =====

// 搜索表单数据
const searchForm = reactive({
  name: '',                                  // 按角色名称筛选
  code: '',                                  // 按角色编码筛选
  status: undefined                          // 按状态筛选
})

// ===== 分页相关 =====

// 分页参数
const pagination = reactive({
  page: 1,       // 当前页码
  size: 10,      // 每页条数
  total: 0       // 总记录数
})

// ===== 表格相关 =====

// 角色表格数据列表
const tableData = ref([])
// 表格加载状态
const loading = ref(false)

// ===== 对话框相关 =====

// 角色编辑对话框是否可见
const dialogVisible = ref(false)
// 对话框标题
const dialogTitle = ref('新增角色')
// 表单引用
const formRef = ref()
// 提交按钮加载状态
const submitLoading = ref(false)

// 角色表单数据
const formData = reactive({
  id: undefined,                         // 角色ID（编辑时有值）
  name: '',                              // 角色名称
  code: '',                              // 角色编码
  description: '',                       // 角色描述
  status: 1                              // 状态（默认启用）
})

// 表单验证规则：名称必填，编码只允许字母和下划线
const formRules = {
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
const permissionTree = ref([])
// 当前角色已选中的权限ID列表（用于树的默认选中）
const checkedPermissionIds = ref([])
// 当前正在分配权限的角色ID
const currentRoleId = ref()
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
    const res = await getRoleList({
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
const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  formData.id = String(row.id)
  formData.name = row.roleName || row.name   // 后端返回的是 roleName
  formData.code = row.roleCode || row.code   // 后端返回的是 roleCode
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
      // 字段映射：前端字段 -> 后端字段
      const submitData = {
        roleCode: formData.code,      // code -> roleCode
        roleName: formData.name,      // name -> roleName
        description: formData.description,
        status: formData.status
      }

      if (formData.id) {
        // 有 ID 表示编辑模式
        await updateRole(formData.id, submitData)
        ElMessage.success('更新成功')
      } else {
        // 无 ID 表示新增模式
        await createRole(submitData)
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
const handleDelete = async (row) => {
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
 * 从权限树中收集所有非叶子节点的 ID（即有 children 的节点）
 * el-tree 的 setCheckedKeys 只能传叶子节点 ID，传父节点 ID 会导致所有子节点被勾选
 * @param nodes 权限树节点列表
 * @returns 非叶子节点 ID 集合
 */
const collectNonLeafIds = (nodes) => {
  const ids = new Set()
  const traverse = (list) => {
    for (const node of list) {
      if (node.children && node.children.length > 0) {
        ids.add(node.id)
        traverse(node.children)
      }
    }
  }
  traverse(nodes)
  return ids
}

/**
 * 打开权限分配对话框
 * 1. 首次打开时加载权限树
 * 2. 获取该角色当前已分配的权限ID（仅叶子节点）
 * @param row 当前行角色数据
 */
const handleAssignPermission = async (row) => {
  currentRoleId.value = row.id
  // 首次打开时加载权限树（后续使用缓存）
  if (permissionTree.value.length === 0) {
    await loadPermissionTree()
  }
  // 先打开对话框，等DOM更新后设置选中状态
  permissionDialogVisible.value = true
  // 获取该角色已拥有的权限ID
  let permissionIds = []
  try {
    permissionIds = await getRolePermissions(row.id)
  } catch (error) {
    permissionIds = []
  }
  // 过滤掉父节点 ID，只保留叶子节点，避免 el-tree 自动勾选所有子节点
  const nonLeafIds = collectNonLeafIds(permissionTree.value)
  const leafIds = permissionIds.filter(id => !nonLeafIds.has(id))
  checkedPermissionIds.value = leafIds
  // 等树渲染完成后手动设置选中状态
  setTimeout(() => {
    if (treeRef.value) {
      treeRef.value.setCheckedKeys(leafIds)
    }
  }, 100)
}

/**
 * 保存权限分配
 * 获取树中所有勾选的权限节点ID，调用接口保存
 */
const handleSavePermission = async () => {
  if (!currentRoleId.value || !treeRef.value) return
  permissionLoading.value = true
  try {
    // 获取所有被勾选的权限节点ID + 半选状态的父节点ID
    const checkedKeys = treeRef.value.getCheckedKeys(false)
    const halfCheckedKeys = treeRef.value.getHalfCheckedKeys()
    const allKeys = [...checkedKeys, ...halfCheckedKeys]
    await assignRolePermissions(currentRoleId.value, allKeys)
    ElMessage.success('保存成功')
    permissionDialogVisible.value = false
    // 清除当前用户的权限缓存，确保下次请求获取最新数据
    await clearUserPermissionCache(userStore.userInfo?.userId)
    // 刷新当前用户的权限（角色权限变更后需要重新加载）
    await userStore.refreshPermissions()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    permissionLoading.value = false
  }
}

/**
 * 每页条数变化处理
 */
const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

/**
 * 页码变化处理
 */
const handlePageChange = (page) => {
  pagination.page = page
  loadData()
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

    .pagination {
      margin-top: 20px;
      justify-content: flex-end;
    }
  }
}
</style>
