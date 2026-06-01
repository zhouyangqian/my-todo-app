<template>
  <el-button-group v-if="selectedIds.length > 0">
    <el-button type="danger" plain @click="$emit('batch-delete')">
      批量删除 ({{ selectedIds.length }})
    </el-button>
    <el-button type="warning" plain @click="$emit('batch-disable')">
      批量禁用
    </el-button>
    <el-button type="primary" plain @click="$emit('batch-assign-role')">
      分配角色
    </el-button>
  </el-button-group>
</template>

<script setup>
defineProps({
  selectedIds: { type: Array, default: () => [] }
})
defineEmits(['batch-delete', 'batch-disable', 'batch-assign-role'])
</script>
