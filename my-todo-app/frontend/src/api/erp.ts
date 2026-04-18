// api/erp.ts - 进销存（ERP）管理 API 接口模块
// 提供商品、库存、仓库、供应商、客户的增删改查及库存出入库、调拨等接口

import { get, post, put, del } from '@/utils/request'

// 产品（商品）类型定义
export interface Product {
  id: number              // 商品唯一标识
  tenantId: number        // 所属租户ID
  sku: string             // 商品编码（SKU）
  name: string            // 商品名称
  categoryId: number      // 所属分类ID
  brand: string           // 品牌
  model: string           // 型号
  unit: string            // 计量单位（如 个、件、台）
  costPrice: number       // 成本价
  salePrice: number       // 销售价
  status: number          // 状态：1-启用，0-禁用
  description: string     // 商品描述
  createdAt: string       // 创建时间
}

// 库存信息类型定义
export interface Inventory {
  id: number              // 库存记录唯一标识
  productId: number       // 商品ID
  warehouseId: number     // 仓库ID
  quantity: number        // 总库存数量
  lockedQuantity: number  // 锁定数量（已分配但未出库）
  availableQuantity: number // 可用数量 = 总数量 - 锁定数量
  productName?: string    // 商品名称（关联查询时填充）
  warehouseName?: string  // 仓库名称（关联查询时填充）
}

// 仓库类型定义
export interface Warehouse {
  id: number              // 仓库唯一标识
  tenantId: number        // 所属租户ID
  name: string            // 仓库名称
  code: string            // 仓库编码
  address: string         // 仓库地址
  managerName: string     // 管理员姓名
  managerPhone: string    // 管理员电话
  status: number          // 状态：1-启用，0-禁用
}

// 供应商类型定义
export interface Supplier {
  id: number              // 供应商唯一标识
  tenantId: number        // 所属租户ID
  name: string            // 供应商名称
  code: string            // 供应商编码
  contactName: string     // 联系人姓名
  contactPhone: string    // 联系人电话
  email: string           // 邮箱地址
  address: string         // 供应商地址
  bankName: string        // 开户银行名称
  bankAccount: string     // 银行账号
  status: number          // 状态：1-启用，0-禁用
}

// 客户类型定义
export interface Customer {
  id: number              // 客户唯一标识
  tenantId: number        // 所属租户ID
  name: string            // 客户名称
  code: string            // 客户编码
  contactName: string     // 联系人姓名
  contactPhone: string    // 联系人电话
  email: string           // 邮箱地址
  address: string         // 客户地址
  creditLimit: number     // 信用额度（赊销上限）
  status: number          // 状态：1-启用，0-禁用
}

// 库存流水（出入库记录）类型定义
export interface InventoryFlow {
  id: number              // 流水记录唯一标识
  productId: number       // 商品ID
  warehouseId: number     // 仓库ID
  flowType: number        // 流水类型：1-入库, 2-出库
  quantity: number        // 变动数量
  beforeQuantity: number  // 变动前库存数量
  afterQuantity: number   // 变动后库存数量
  bizType: string         // 业务类型（如 采购入库、销售出库等）
  bizId: string           // 关联业务单据ID
  remark: string          // 备注
  createdAt: string       // 创建时间
}

// 通用分页查询结果类型
export interface PageResult<T> {
  records: T[]            // 当前页数据列表
  total: number           // 总记录数
  size: number            // 每页条数
  current: number         // 当前页码
  pages: number           // 总页数
}

// ============ 商品（产品）API ============

/**
 * 分页查询商品列表
 * @param params 分页参数和筛选条件（名称、SKU编码、分类、状态）
 * @returns 分页结果
 */
export function getProductPage(params: {
  page?: number
  size?: number
  name?: string
  sku?: string
  categoryId?: number
  status?: number
}): Promise<PageResult<Product>> {
  return get('/erp/products', params)
}

/**
 * 根据ID获取商品详情
 * @param id 商品ID
 */
export function getProduct(id: number): Promise<Product> {
  return get(`/erp/products/${id}`)
}

/**
 * 创建新商品
 * @param data 商品信息
 */
export function createProduct(data: Partial<Product>): Promise<Product> {
  return post('/erp/products', data)
}

/**
 * 更新商品信息
 * @param id 商品ID
 * @param data 需要更新的商品字段
 */
export function updateProduct(id: number, data: Partial<Product>): Promise<Product> {
  return put(`/erp/products/${id}`, data)
}

/**
 * 删除商品
 * @param id 商品ID
 */
export function deleteProduct(id: number): Promise<void> {
  return del(`/erp/products/${id}`)
}

// ============ 库存 API ============

/**
 * 分页查询库存列表
 * @param params 分页参数和筛选条件（商品ID、仓库ID）
 * @returns 分页结果
 */
export function getInventoryPage(params: {
  page?: number
  size?: number
  productId?: number
  warehouseId?: number
}): Promise<PageResult<Inventory>> {
  return get('/erp/inventory', params)
}

/**
 * 商品入库操作
 * @param data 入库信息（商品ID、仓库ID、数量、业务类型等）
 */
export function inbound(data: {
  productId: number        // 商品ID
  warehouseId: number      // 目标仓库ID
  quantity: number         // 入库数量
  bizType: string          // 业务类型（如 purchase-采购入库）
  bizId?: string           // 关联业务单据ID
  remark?: string          // 备注
}): Promise<void> {
  return post('/erp/inventory/inbound', data)
}

/**
 * 商品出库操作
 * @param data 出库信息（商品ID、仓库ID、数量、业务类型等）
 */
export function outbound(data: {
  productId: number        // 商品ID
  warehouseId: number      // 源仓库ID
  quantity: number         // 出库数量
  bizType: string          // 业务类型（如 sale-销售出库）
  bizId?: string           // 关联业务单据ID
  remark?: string          // 备注
}): Promise<void> {
  return post('/erp/inventory/outbound', data)
}

/**
 * 库存调拨（将商品从一个仓库转移到另一个仓库）
 * @param data 调拨信息（商品ID、源仓库、目标仓库、数量）
 */
export function transfer(data: {
  productId: number          // 商品ID
  fromWarehouseId: number    // 源仓库ID
  toWarehouseId: number      // 目标仓库ID
  quantity: number           // 调拨数量
  remark?: string            // 备注
}): Promise<void> {
  return post('/erp/inventory/transfer', data)
}

/**
 * 分页查询库存流水记录（出入库记录）
 * @param params 分页参数和筛选条件（商品ID、仓库ID、流水类型）
 */
export function getInventoryFlowPage(params: {
  page?: number
  size?: number
  productId?: number
  warehouseId?: number
  flowType?: number        // 流水类型：1-入库，2-出库
}): Promise<PageResult<InventoryFlow>> {
  return get('/erp/inventory/flow', params)
}

// ============ 仓库 API ============

/**
 * 获取所有仓库列表（不分页）
 */
export function getWarehouseList(): Promise<Warehouse[]> {
  return get('/erp/warehouses')
}

/**
 * 创建新仓库
 * @param data 仓库信息
 */
export function createWarehouse(data: Partial<Warehouse>): Promise<Warehouse> {
  return post('/erp/warehouses', data)
}

/**
 * 更新仓库信息
 * @param id 仓库ID
 * @param data 需要更新的仓库字段
 */
export function updateWarehouse(id: number, data: Partial<Warehouse>): Promise<Warehouse> {
  return put(`/erp/warehouses/${id}`, data)
}

/**
 * 删除仓库
 * @param id 仓库ID
 */
export function deleteWarehouse(id: number): Promise<void> {
  return del(`/erp/warehouses/${id}`)
}

// ============ 供应商 API ============

/**
 * 分页查询供应商列表
 * @param params 分页参数和筛选条件（名称、编码、状态）
 */
export function getSupplierPage(params: {
  page?: number
  size?: number
  name?: string
  code?: string
  status?: number
}): Promise<PageResult<Supplier>> {
  return get('/erp/suppliers', params)
}

/**
 * 创建新供应商
 * @param data 供应商信息
 */
export function createSupplier(data: Partial<Supplier>): Promise<Supplier> {
  return post('/erp/suppliers', data)
}

/**
 * 更新供应商信息
 * @param id 供应商ID
 * @param data 需要更新的供应商字段
 */
export function updateSupplier(id: number, data: Partial<Supplier>): Promise<Supplier> {
  return put(`/erp/suppliers/${id}`, data)
}

/**
 * 删除供应商
 * @param id 供应商ID
 */
export function deleteSupplier(id: number): Promise<void> {
  return del(`/erp/suppliers/${id}`)
}

// ============ 客户 API ============

/**
 * 分页查询客户列表
 * @param params 分页参数和筛选条件（名称、编码、状态）
 */
export function getCustomerPage(params: {
  page?: number
  size?: number
  name?: string
  code?: string
  status?: number
}): Promise<PageResult<Customer>> {
  return get('/erp/customers', params)
}

/**
 * 创建新客户
 * @param data 客户信息
 */
export function createCustomer(data: Partial<Customer>): Promise<Customer> {
  return post('/erp/customers', data)
}

/**
 * 更新客户信息
 * @param id 客户ID
 * @param data 需要更新的客户字段
 */
export function updateCustomer(id: number, data: Partial<Customer>): Promise<Customer> {
  return put(`/erp/customers/${id}`, data)
}

/**
 * 删除客户
 * @param id 客户ID
 */
export function deleteCustomer(id: number): Promise<void> {
  return del(`/erp/customers/${id}`)
}
