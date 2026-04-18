// main.js - 应用入口文件，负责创建 Vue 实例并注册全局插件（路由、状态管理、UI 框架等）

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import 'nprogress/nprogress.css'

import App from './App.vue'
import router from './router'
import './styles/index.scss'

// 创建 Vue 应用实例
const app = createApp(App)

// 全局注册 Element Plus 所有图标组件，以便在模板中直接使用图标名称
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册 Pinia 状态管理
app.use(createPinia())
// 注册 Vue Router 路由
app.use(router)
// 注册 Element Plus UI 框架，并设置为中文语言包
app.use(ElementPlus, { locale: zhCn })

// 将应用挂载到 HTML 中 id 为 app 的 DOM 元素上
app.mount('#app')
