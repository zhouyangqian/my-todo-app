// stores/index.js - Pinia 状态管理入口文件
// 创建 Pinia 实例并统一导出所有 store 模块

import { createPinia } from 'pinia'

// 创建 Pinia 状态管理实例
const pinia = createPinia()

export default pinia

// 导出所有 store 模块，方便外部通过 @/stores 直接引用
export * from './user'       // 用户状态管理（登录信息、权限、角色等）
