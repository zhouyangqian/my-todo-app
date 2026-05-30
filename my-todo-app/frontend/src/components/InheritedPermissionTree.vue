<template>
  <div class="inherited-permission-tree">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="有效权限" name="effective">
        <div v-loading="loading">
          <el-tree
            :data="effectiveTree"
            :props="defaultProps"
            default-expand-all
            node-key="id"
          >
            <template #default="{ data }">
              <span class="tree-node-label">
                <span>{{ data.permissionName }}</span>
                <el-tag
                  v-if="data._source === 'inherited'"
                  size="small"
                  type="warning"
                >
                  继承
                </el-tag>
                <el-tag
                  v-else
                  size="small"
                  type="success"
                >
                  直接
                </el-tag>
                <el-tag v-if="data.permissionCode" size="small" type="info" class="code-tag">
                  {{ data.permissionCode }}
                </el-tag>
              </span>
            </template>
          </el-tree>
          <el-empty v-if="!loading && effectiveTree.length === 0" description="暂无有效权限" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="继承关系" name="inheritance">
        <div v-loading="loading">
          <div v-if="inheritanceTree.length > 0" class="inheritance-list">
            <div
              v-for="item in inheritanceTree"
              :key="item.roleId"
              class="inheritance-node"
            >
              <div class="role-info">
                <el-tag type="primary">{{ item.roleName }}</el-tag>
                <span class="role-code">{{ item.roleCode }}</span>
              </div>
              <div v-if="item.parents && item.parents.length > 0" class="parent-list">
                <div class="parent-label">继承自:</div>
                <div
                  v-for="parent in item.parents"
                  :key="parent.roleId"
                  class="parent-item"
                >
                  <el-tag type="warning">{{ parent.roleName }}</el-tag>
                  <span class="role-code">{{ parent.roleCode }}</span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-if="!loading && inheritanceTree.length === 0" description="暂无继承关系" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getEffectivePermissions, getInheritedPermissions, getInheritanceTree } from '@/api/permission'

const props = defineProps({
  /** 角色ID */
  roleId: {
    type: [Number, String],
    required: true
  }
})

const activeTab = ref('effective')
const loading = ref(false)
const effectiveTree = ref([])
const inheritanceTree = ref([])

const defaultProps = {
  children: 'children',
  label: 'permissionName'
}

/** 加载有效权限（直接 + 继承） */
async function loadEffectivePermissions() {
  if (!props.roleId) return
  loading.value = true
  try {
    const [effectiveRes, inheritedRes] = await Promise.all([
      getEffectivePermissions(props.roleId),
      getInheritedPermissions(props.roleId)
    ])

    const effectiveList = effectiveRes.data || []
    const inheritedList = inheritedRes.data || []

    // 标记继承权限的ID集合
    const inheritedIds = new Set(inheritedList.map(p => p.id))

    // 给每个权限节点标记来源
    const markedList = effectiveList.map(p => ({
      ...p,
      _source: inheritedIds.has(p.id) ? 'inherited' : 'direct'
    }))

    // 标记子节点来源（递归）
    effectiveTree.value = buildSourceTree(markedList, inheritedIds)
  } catch (e) {
    console.error('加载有效权限失败', e)
    effectiveTree.value = []
  } finally {
    loading.value = false
  }
}

/** 递归标记树节点的来源 */
function buildSourceTree(permissions, inheritedIds) {
  return permissions.map(p => ({
    ...p,
    _source: inheritedIds.has(p.id) ? 'inherited' : 'direct',
    children: p.children && p.children.length > 0
      ? buildSourceTree(p.children, inheritedIds)
      : []
  }))
}

/** 加载继承树 */
async function loadInheritanceTree() {
  if (!props.roleId) return
  try {
    const res = await getInheritanceTree(props.roleId)
    inheritanceTree.value = res.data || []
  } catch (e) {
    console.error('加载继承树失败', e)
    inheritanceTree.value = []
  }
}

/** 加载所有数据 */
async function loadAll() {
  await Promise.all([
    loadEffectivePermissions(),
    loadInheritanceTree()
  ])
}

watch(
  () => props.roleId,
  () => {
    loadAll()
  }
)

onMounted(() => {
  loadAll()
})
</script>

<style scoped>
.inherited-permission-tree {
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 12px;
  max-height: 500px;
  overflow-y: auto;
}

.tree-node-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.code-tag {
  font-size: 12px;
}

.inheritance-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.inheritance-node {
  padding: 10px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
}

.role-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.role-code {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.parent-list {
  margin-top: 8px;
  padding-left: 16px;
}

.parent-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 4px;
}

.parent-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}
</style>
