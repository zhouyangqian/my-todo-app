<!-- views/system/role-inheritance/index.vue - 角色继承管理页面
  提供角色继承关系管理功能：
  - 角色选择
  - 继承树显示
  - 设置/移除父角色
  - 有效权限列表（区分继承和自有）
-->
<template>
  <div class="role-inheritance-container">
    <el-row :gutter="20">
      <!-- 左侧：角色选择与继承树 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>角色继承管理</span>
              <el-button type="primary" @click="handleSetParent" :disabled="!selectedRoleId">
                <el-icon><Plus /></el-icon>
                设置父角色
              </el-button>
            </div>
          </template>

          <!-- 角色选择 -->
          <el-select
            v-model="selectedRoleId"
            placeholder="请选择角色"
            clearable
            @change="handleRoleChange"
            style="width: 100%; margin-bottom: 20px"
          >
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="`${role.roleName} (${role.roleCode})`"
              :value="role.id"
            />
          </el-select>

          <!-- 父角色列表 -->
          <div v-if="parentRoles.length > 0" style="margin-bottom: 20px">
            <h4 style="margin-bottom: 10px">直接父角色</h4>
            <el-table :data="parentRoles" border size="small">
              <el-table-column prop="roleName" label="角色名称" />
              <el-table-column prop="roleCode" label="角色编码" />
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button
                    type="danger"
                    link
                    size="small"
                    @click="handleRemoveParent(row)"
                  >
                    移除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 继承树 -->
          <div v-if="inheritanceTree.length > 0">
            <h4 style="margin-bottom: 10px">继承树</h4>
            <el-tree
              :data="inheritanceTree"
              :props="treeProps"
              node-key="roleId"
              default-expand-all
            >
              <template #default="{ data }">
                <span class="tree-node">
                  <span>{{ data.roleName }}</span>
                  <el-tag size="small" type="info" style="margin-left: 8px">
                    {{ data.roleCode }}
                  </el-tag>
                </span>
              </template>
            </el-tree>
          </div>

          <el-empty v-if="!selectedRoleId" description="请选择角色查看继承关系" />
          <el-empty
            v-if="selectedRoleId && parentRoles.length === 0 && inheritanceTree.length === 0"
            description="该角色暂无继承关系"
          />
        </el-card>
      </el-col>

      <!-- 右侧：有效权限 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>有效权限列表</span>
              <el-tag v-if="selectedRoleId" type="info" size="small">
                {{ ownPermissionCount }} 项自有 / {{ inheritedPermissionCount }} 项继承
              </el-tag>
            </div>
          </template>

          <el-table
            :data="effectivePermissions"
            v-loading="permLoading"
            border
            size="small"
          >
            <el-table-column prop="permissionName" label="权限名称" />
            <el-table-column prop="permissionCode" label="权限编码" />
            <el-table-column label="来源" width="100">
              <template #default="{ row }">
                <el-tag
                  :type="row.source === 'own' ? 'success' : 'warning'"
                  size="small"
                >
                  {{ row.source === 'own' ? '自有' : '继承' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!selectedRoleId" description="请选择角色查看权限" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 设置父角色对话框 -->
    <el-dialog
      v-model="parentDialogVisible"
      title="设置父角色"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form label-width="80px">
        <el-form-item label="子角色">
          <el-input :value="selectedRoleName" disabled />
        </el-form-item>
        <el-form-item label="父角色">
          <el-select
            v-model="parentRoleId"
            placeholder="请选择父角色"
            style="width: 100%"
          >
            <el-option
              v-for="role in availableParentRoles"
              :key="role.id"
              :label="`${role.roleName} (${role.roleCode})`"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="parentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveParent" :loading="parentLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getRoleList,
  setRoleParent,
  removeRoleParent,
  getParentRoles,
  getInheritedPermissions,
  getEffectivePermissions,
  getInheritanceTree,
  getRolePermissions
} from '@/api/permission'

// ===== 角色选择 =====
const selectedRoleId = ref()
const roleList = ref([])

/** 当前选中角色的名称 */
const selectedRoleName = computed(() => {
  const role = roleList.value.find(r => r.id === selectedRoleId.value)
  return role ? role.roleName : ''
})

// ===== 父角色 =====
const parentRoles = ref([])

// ===== 继承树 =====
const inheritanceTree = ref([])
const treeProps = {
  children: 'parents',
  label: 'roleName'
}

// ===== 有效权限 =====
const effectivePermissions = ref([])
const permLoading = ref(false)
const ownPermissionCount = ref(0)
const inheritedPermissionCount = ref(0)

// ===== 设置父角色对话框 =====
const parentDialogVisible = ref(false)
const parentRoleId = ref()
const parentLoading = ref(false)

/** 可选的父角色列表（排除自身和已有的父角色） */
const availableParentRoles = computed(() => {
  const parentIds = parentRoles.value.map(r => r.id)
  return roleList.value.filter(r =>
    r.id !== selectedRoleId.value && !parentIds.includes(r.id)
  )
})

/**
 * 加载角色列表
 */
const loadRoleList = async () => {
  try {
    const res = await getRoleList({ page: 1, size: 200 })
    roleList.value = res.records || []
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  }
}

/**
 * 角色选择变化时，重新加载继承信息和权限
 */
const handleRoleChange = async () => {
  if (!selectedRoleId.value) {
    parentRoles.value = []
    inheritanceTree.value = []
    effectivePermissions.value = []
    return
  }
  await Promise.all([
    loadParentRoles(),
    loadInheritanceTree(),
    loadEffectivePermissions()
  ])
}

/**
 * 加载父角色列表
 */
const loadParentRoles = async () => {
  try {
    const res = await getParentRoles(selectedRoleId.value)
    parentRoles.value = res || []
  } catch (error) {
    parentRoles.value = []
  }
}

/**
 * 加载继承树
 */
const loadInheritanceTree = async () => {
  try {
    const res = await getInheritanceTree(selectedRoleId.value)
    inheritanceTree.value = res || []
  } catch (error) {
    inheritanceTree.value = []
  }
}

/**
 * 加载有效权限列表（区分自有和继承）
 */
const loadEffectivePermissions = async () => {
  if (!selectedRoleId.value) return
  permLoading.value = true
  try {
    // 获取自有权限ID列表
    const ownPermIds = await getRolePermissions(selectedRoleId.value)
    const ownIdSet = new Set(ownPermIds || [])

    // 获取继承的权限列表
    const inheritedPerms = await getInheritedPermissions(selectedRoleId.value)
    const inheritedIdSet = new Set((inheritedPerms || []).map(p => p.id))

    // 获取所有有效权限列表
    const allPerms = await getEffectivePermissions(selectedRoleId.value) || []

    // 标记来源
    effectivePermissions.value = allPerms.map(p => ({
      ...p,
      source: ownIdSet.has(p.id) ? 'own' : 'inherited'
    }))

    ownPermissionCount.value = allPerms.filter(p => ownIdSet.has(p.id)).length
    inheritedPermissionCount.value = allPerms.filter(p => !ownIdSet.has(p.id)).length
  } catch (error) {
    effectivePermissions.value = []
    ownPermissionCount.value = 0
    inheritedPermissionCount.value = 0
  } finally {
    permLoading.value = false
  }
}

/**
 * 打开设置父角色对话框
 */
const handleSetParent = () => {
  parentRoleId.value = undefined
  parentDialogVisible.value = true
}

/**
 * 保存父角色设置
 */
const handleSaveParent = async () => {
  if (!parentRoleId.value) {
    ElMessage.warning('请选择父角色')
    return
  }
  parentLoading.value = true
  try {
    await setRoleParent({
      childRoleId: selectedRoleId.value,
      parentRoleId: parentRoleId.value
    })
    ElMessage.success('设置继承关系成功')
    parentDialogVisible.value = false
    // 重新加载继承信息
    await handleRoleChange()
  } catch (error) {
    // 错误已在拦截器中处理，此处仅做日志
    console.error('设置继承失败:', error)
  } finally {
    parentLoading.value = false
  }
}

/**
 * 移除父角色继承关系
 */
const handleRemoveParent = async (row) => {
  await ElMessageBox.confirm(
    `确定要移除 ${row.roleName} 作为父角色的继承关系吗？`,
    '提示',
    { type: 'warning' }
  )
  try {
    await removeRoleParent({
      childRoleId: selectedRoleId.value,
      parentRoleId: row.id
    })
    ElMessage.success('移除继承关系成功')
    await handleRoleChange()
  } catch (error) {
    console.error('移除继承失败:', error)
  }
}

onMounted(() => {
  loadRoleList()
})
</script>

<style scoped lang="scss">
.role-inheritance-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .tree-node {
    display: flex;
    align-items: center;
    font-size: 14px;
  }

  h4 {
    color: #303133;
    font-size: 14px;
  }
}
</style>
