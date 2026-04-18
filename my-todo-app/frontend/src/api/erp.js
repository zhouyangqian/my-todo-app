// api/erp.js - 进销存（ERP）管理 API 接口模块
// 提供商品、库存、仓库、供应商、客户的增删改查及库存出入库、调拨等接口

import { get, post, put, del } from '@/utils/request'

// ============ 商品（产品）API ============

/**
 * 分页查询商品列表
 * @param params 分页参数和筛选条件（名称、SKU编码、分类、状态）
 */
export function getProductPage(params) {
  return get('/erp/products', params)
}

/**
 * 根据ID获取商品详情
 * @param id 商品ID
 */
export function getProduct(id) {
  return get(`/erp/products/${id}`)
}

/**
 * 创建新商品
 * @param data 商品信息
 */
export function createProduct(data) {
  return post('/erp/products', data)
}

/**
 * 更新商品信息
 * @param id 商品ID
 * @param data 需要更新的商品字段
 */
export function updateProduct(id, data) {
  return put(`/erp/products/${id}`, data)
}

/**
 * 删除商品
 * @param id 商品ID
 */
export function deleteProduct(id) {
  return del(`/erp/products/${id}`)
}

// ============ 库存 API ============

/**
 * 分页查询库存列表
 * @param params 分页参数和筛选条件（商品ID、仓库ID）
 */
export function getInventoryPage(params) {
  return get('/erp/inventory', params)
}

/**
 * 商品入库操作
 * @param data 入库信息（商品ID、仓库ID、数量、业务类型等）
 */
export function inbound(data) {
  return post('/erp/inventory/inbound', data)
}

/**
 * 商品出库操作
 * @param data 出库信息（商品ID、仓库ID、数量、业务类型等）
 */
export function outbound(data) {
  return post('/erp/inventory/outbound', data)
}

/**
 * 库存调拨（将商品从一个仓库转移到另一个仓库）
 * @param data 调拨信息（商品ID、源仓库、目标仓库、数量）
 */
export function transfer(data) {
  return post('/erp/inventory/transfer', data)
}

/**
 * 分页查询库存流水记录（出入库记录）
 * @param params 分页参数和筛选条件（商品ID、仓库ID、流水类型）
 */
export function getInventoryFlowPage(params) {
  return get('/erp/inventory/flow', params)
}

// ============ 仓库 API ============

/**
 * 获取所有仓库列表（不分页）
 */
export function getWarehouseList() {
  return get('/erp/warehouses')
}

/**
 * 创建新仓库
 * @param data 仓库信息
 */
export function createWarehouse(data) {
  return post('/erp/warehouses', data)
}

/**
 * 更新仓库信息
 * @param id 仓库ID
 * @param data 需要更新的仓库字段
 */
export function updateWarehouse(id, data) {
  return put(`/erp/warehouses/${id}`, data)
}

/**
 * 删除仓库
 * @param id 仓库ID
 */
export function deleteWarehouse(id) {
  return del(`/erp/warehouses/${id}`)
}

// ============ 供应商 API ============

/**
 * 分页查询供应商列表
 * @param params 分页参数和筛选条件（名称、编码、状态）
 */
export function getSupplierPage(params) {
  return get('/erp/suppliers', params)
}

/**
 * 创建新供应商
 * @param data 供应商信息
 */
export function createSupplier(data) {
  return post('/erp/suppliers', data)
}

/**
 * 更新供应商信息
 * @param id 供应商ID
 * @param data 需要更新的供应商字段
 */
export function updateSupplier(id, data) {
  return put(`/erp/suppliers/${id}`, data)
}

/**
 * 删除供应商
 * @param id 供应商ID
 */
export function deleteSupplier(id) {
  return del(`/erp/suppliers/${id}`)
}

// ============ 客户 API ============

/**
 * 分页查询客户列表
 * @param params 分页参数和筛选条件（名称、编码、状态）
 */
export function getCustomerPage(params) {
  return get('/erp/customers', params)
}

/**
 * 创建新客户
 * @param data 客户信息
 */
export function createCustomer(data) {
  return post('/erp/customers', data)
}

/**
 * 更新客户信息
 * @param id 客户ID
 * @param data 需要更新的客户字段
 */
export function updateCustomer(id, data) {
  return put(`/erp/customers/${id}`, data)
}

/**
 * 删除客户
 * @param id 客户ID
 */
export function deleteCustomer(id) {
  return del(`/erp/customers/${id}`)
}
