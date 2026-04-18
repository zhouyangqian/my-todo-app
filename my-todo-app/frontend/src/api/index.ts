// api/index.ts - API 模块统一导出入口
// 将所有子模块的接口和类型统一导出，方便外部通过 @/api 直接引用

export * from './auth'        // 认证相关接口（登录、登出、Token 刷新等）
export * from './user'        // 用户管理接口（用户 CRUD、地址管理等）
export * from './permission'  // 权限管理接口（角色、权限、分配等）
export * from './erp'         // 进销存接口（商品、库存、仓库、供应商、客户）
export * from './finance'     // 财务管理接口（应收、应付、收支记录、银行账户）
export * from './dict'        // 字典管理接口（字典类型、字典项、系统配置）
