<!-- App.vue - 应用根组件，作为整个单页应用的入口，承载路由视图 -->
<template>
  <!-- 路由视图容器，根据当前路由路径渲染对应的页面组件 -->
  <router-view />
</template>

<script setup>
// 应用根组件 - 无需额外逻辑，仅作为路由出口
</script>

<style>
/* 根元素占满整个视口 */
#app {
  width: 100%;
  height: 100%;
}
</style>
