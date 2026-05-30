<template>
  <div class="permission-select">
    <el-tree
      ref="treeRef"
      :data="treeData"
      show-checkbox
      node-key="id"
      :default-checked-keys="checkedKeys"
      :props="defaultProps"
      :loading="loading"
      check-strictly
      @check="handleCheck"
    >
      <template #default="{ node, data }">
        <span class="tree-node-label">
          <span>{{ data.permissionName }}</span>
          <el-tag v-if="data.permissionCode" size="small" type="info" class="code-tag">
            {{ data.permissionCode }}
          </el-tag>
        </span>
      </template>
    </el-tree>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getPermissionTree } from '@/api/permission'

const props = defineProps({
  /** 当前角色已有的权限ID列表 */
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const treeRef = ref(null)
const treeData = ref([])
const loading = ref(false)
const checkedKeys = ref([...props.modelValue])

const defaultProps = {
  children: 'children',
  label: 'permissionName'
}

/** 加载权限树 */
async function loadTree() {
  loading.value = true
  try {
    const res = await getPermissionTree()
    treeData.value = res.data || []
  } catch (e) {
    console.error('加载权限树失败', e)
    treeData.value = []
  } finally {
    loading.value = false
  }
}

/** 节点选中变化 */
function handleCheck() {
  const checkedNodes = treeRef.value.getCheckedNodes(false, true)
  const ids = checkedNodes.map(node => node.id)
  emit('update:modelValue', ids)
}

/** 外部 modelValue 变化时同步到树 */
watch(
  () => props.modelValue,
  (val) => {
    checkedKeys.value = [...val]
  },
  { deep: true }
)

onMounted(() => {
  loadTree()
})
</script>

<style scoped>
.permission-select {
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.tree-node-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.code-tag {
  font-size: 12px;
}
</style>
