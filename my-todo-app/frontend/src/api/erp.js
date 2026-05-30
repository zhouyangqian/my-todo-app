// api/erp.js - 进销存（ERP）管理 API 接口模块
// 提供商品、库存、仓库、供应商、客户的增删改查及库存出入库、调拨等接口

import { get, post, put, del } from '@/utils/request'

// ============ 商品分类 API ============

/**
 * 分页查询商品分类列表
 * @param params 分页参数和筛选条件
 */
export function getProductCategoryPage(params) {
  return get('/erp/product-categories/get-category-page', params)
}

/**
 * 获取所有分类（下拉选择用）
 */
export function getAllProductCategories() {
  return get('/erp/product-categories/get-all-categories')
}

/**
 * 创建商品分类
 * @param data 分类信息
 */
export function createProductCategory(data) {
  return post('/erp/product-categories/create-category', data)
}

/**
 * 更新商品分类
 * @param id 分类ID
 * @param data 分类信息
 */
export function updateProductCategory(id, data) {
  return put(`/erp/product-categories/update-category/${id}`, data)
}

/**
 * 删除商品分类
 * @param id 分类ID
 */
export function deleteProductCategory(id) {
  return del(`/erp/product-categories/delete-category/${id}`)
}

// ============ 商品（产品）API ============

/**
 * 分页查询商品列表
 * @param params 分页参数和筛选条件（名称、SKU编码、分类、状态）
 */
export function getProductPage(params) {
  return get('/erp/products/get-product-page', params)
}

/**
 * 根据ID获取商品详情
 * @param id 商品ID
 */
export function getProduct(id) {
  return get(`/erp/products/get-product/${id}`)
}

/**
 * 创建新商品
 * @param data 商品信息
 */
export function createProduct(data) {
  return post('/erp/products/create-product', data)
}

/**
 * 更新商品信息
 * @param id 商品ID
 * @param data 需要更新的商品字段
 */
export function updateProduct(id, data) {
  return put(`/erp/products/update-product/${id}`, data)
}

/**
 * 删除商品
 * @param id 商品ID
 */
export function deleteProduct(id) {
  return del(`/erp/products/delete-product/${id}`)
}

// ============ 库存 API ============

/**
 * 分页查询库存列表
 * @param params 分页参数和筛选条件（商品ID、仓库ID）
 */
export function getInventoryPage(params) {
  return get('/erp/inventory/get-inventory-page', params)
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
 * @param params 分页参数和筛选条件（仓库ID、商品ID、业务类型）
 */
export function getInventoryFlowPage(params) {
  return get('/erp/inventory/get-flow-page', params)
}

// ============ 仓库 API ============

/**
 * 分页查询仓库列表
 * @param params 分页参数和筛选条件（名称、状态）
 */
export function getWarehousePage(params) {
  return get('/erp/warehouses/get-warehouse-page', params)
}

/**
 * 获取所有仓库列表（不分页，用于下拉选择）
 */
export function getWarehouses() {
  return get('/erp/warehouses/get-all-warehouses')
}

/**
 * 获取默认仓库
 */
export function getDefaultWarehouse() {
  return get('/erp/warehouses/get-default-warehouse')
}

/**
 * 创建新仓库
 * @param data 仓库信息
 */
export function createWarehouse(data) {
  return post('/erp/warehouses/create-warehouse', data)
}

/**
 * 更新仓库信息
 * @param id 仓库ID
 * @param data 需要更新的仓库字段
 */
export function updateWarehouse(id, data) {
  return put(`/erp/warehouses/update-warehouse/${id}`, data)
}

/**
 * 删除仓库
 * @param id 仓库ID
 */
export function deleteWarehouse(id) {
  return del(`/erp/warehouses/delete-warehouse/${id}`)
}

/**
 * 设置默认仓库
 * @param id 仓库ID
 */
export function setDefaultWarehouse(id) {
  return post(`/erp/warehouses/${id}/default`)
}

// ============ 供应商 API ============

/**
 * 分页查询供应商列表
 * @param params 分页参数和筛选条件（名称、编码、状态）
 */
export function getSupplierPage(params) {
  return get('/erp/suppliers/get-supplier-page', params)
}

/**
 * 创建新供应商
 * @param data 供应商信息
 */
export function createSupplier(data) {
  return post('/erp/suppliers/create-supplier', data)
}

/**
 * 更新供应商信息
 * @param id 供应商ID
 * @param data 需要更新的供应商字段
 */
export function updateSupplier(id, data) {
  return put(`/erp/suppliers/update-supplier/${id}`, data)
}

/**
 * 删除供应商
 * @param id 供应商ID
 */
export function deleteSupplier(id) {
  return del(`/erp/suppliers/delete-supplier/${id}`)
}

// ============ 客户 API ============

/**
 * 分页查询客户列表
 * @param params 分页参数和筛选条件（名称、编码、状态）
 */
export function getCustomerPage(params) {
  return get('/erp/customers/get-customer-page', params)
}

/**
 * 创建新客户
 * @param data 客户信息
 */
export function createCustomer(data) {
  return post('/erp/customers/create-customer', data)
}

/**
 * 更新客户信息
 * @param id 客户ID
 * @param data 需要更新的客户字段
 */
export function updateCustomer(id, data) {
  return put(`/erp/customers/update-customer/${id}`, data)
}

/**
 * 删除客户
 * @param id 客户ID
 */
export function deleteCustomer(id) {
  return del(`/erp/customers/delete-customer/${id}`)
}

// ============ 采购订单 API ============

/**
 * 分页查询采购订单列表
 * @param params 分页参数和筛选条件（订单号、供应商、状态）
 */
export function getPurchaseOrderPage(params) {
  return get('/erp/purchase-orders/get-purchase-order-page', params)
}

/**
 * 获取采购订单详情
 * @param id 订单ID
 */
export function getPurchaseOrderDetail(id) {
  return get(`/erp/purchase-orders/get-purchase-order/${id}`)
}

/**
 * 创建采购订单
 * @param data 订单信息
 */
export function createPurchaseOrder(data) {
  return post('/erp/purchase-orders/create-purchase-order', data)
}

/**
 * 更新采购订单
 * @param id 订单ID
 * @param data 订单信息
 */
export function updatePurchaseOrder(id, data) {
  return put(`/erp/purchase-orders/update-purchase-order/${id}`, data)
}

/**
 * 提交采购订单审核
 * @param id 订单ID
 */
export function submitPurchaseOrder(id) {
  return post(`/erp/purchase-orders/submit-for-approval/${id}`)
}

/**
 * 审核采购订单
 * @param id 订单ID
 */
export function approvePurchaseOrder(id) {
  return post(`/erp/purchase-orders/approve-order/${id}`)
}

/**
 * 取消采购订单
 * @param id 订单ID
 */
export function cancelPurchaseOrder(id) {
  return post(`/erp/purchase-orders/cancel-order/${id}`)
}

/**
 * 采购入库
 * @param id 订单ID
 * @param data 入库明细
 */
export function purchaseInbound(id, data) {
  return post(`/erp/purchase-orders/inbound/${id}`, data)
}

// ============ 销售订单 API ============

/**
 * 分页查询销售订单列表
 * @param params 分页参数和筛选条件（订单号、客户、状态）
 */
export function getSalesOrderPage(params) {
  return get('/erp/sales-orders/get-sales-order-page', params)
}

/**
 * 获取销售订单详情
 * @param id 订单ID
 */
export function getSalesOrderDetail(id) {
  return get(`/erp/sales-orders/get-sales-order/${id}`)
}

/**
 * 创建销售订单
 * @param data 订单信息
 */
export function createSalesOrder(data) {
  return post('/erp/sales-orders/create-sales-order', data)
}

/**
 * 更新销售订单
 * @param id 订单ID
 * @param data 订单信息
 */
export function updateSalesOrder(id, data) {
  return put(`/erp/sales-orders/update-sales-order/${id}`, data)
}

/**
 * 提交销售订单审核
 * @param id 订单ID
 */
export function submitSalesOrder(id) {
  return post(`/erp/sales-orders/submit-for-approval/${id}`)
}

/**
 * 审核销售订单
 * @param id 订单ID
 */
export function approveSalesOrder(id) {
  return post(`/erp/sales-orders/approve-order/${id}`)
}

/**
 * 取消销售订单
 * @param id 订单ID
 */
export function cancelSalesOrder(id) {
  return post(`/erp/sales-orders/cancel-order/${id}`)
}

// ============ 销售出库单 API ============

/**
 * 分页查询销售出库单列表
 * @param params 分页参数和筛选条件（出库单号、订单、状态）
 */
export function getSalesShipmentPage(params) {
  return get('/erp/sales-shipments/get-sales-shipment-page', params)
}

/**
 * 获取销售出库单详情
 * @param id 出库单ID
 */
export function getSalesShipmentDetail(id) {
  return get(`/erp/sales-shipments/get-sales-shipment/${id}`)
}

/**
 * 获取订单的可发货商品列表
 * @param orderId 订单ID
 */
export function getShippableItems(orderId) {
  return get(`/erp/sales-shipments/get-shippable-items/${orderId}`)
}

/**
 * 创建销售出库单
 * @param data 出库单信息
 */
export function createSalesShipment(data) {
  return post('/erp/sales-shipments/create-sales-shipment', data)
}

/**
 * 审核销售出库单
 * @param id 出库单ID
 */
export function approveSalesShipment(id) {
  return post(`/erp/sales-shipments/approve-shipment/${id}`)
}

/**
 * 取消销售出库单
 * @param id 出库单ID
 */
export function cancelSalesShipment(id) {
  return post(`/erp/sales-shipments/cancel-shipment/${id}`)
}

// ============ 库存盘点 API ============

export function getInventoryCheckPage(params) {
  return get('/erp/inventory-checks/get-check-page', params)
}

export function getInventoryCheckDetail(id) {
  return get(`/erp/inventory-checks/get-check/${id}`)
}

export function createInventoryCheck(data) {
  return post('/erp/inventory-checks/create-check', data)
}

export function submitInventoryCheck(id, data) {
  return post(`/erp/inventory-checks/submit-check/${id}`, data)
}

export function cancelInventoryCheck(id) {
  return post(`/erp/inventory-checks/cancel-check/${id}`)
}

// ============ 库存预警 API ============

export function getAlertInventories(params) {
  return get('/erp/inventory/get-alert-inventories', params)
}

// ============ 采购退货 API ============

export function getPurchaseReturnPage(params) {
  return get('/erp/purchase-returns/get-return-page', params)
}

export function getPurchaseReturnDetail(id) {
  return get(`/erp/purchase-returns/get-return/${id}`)
}

export function createPurchaseReturn(data) {
  return post('/erp/purchase-returns/create-return', data)
}

export function submitPurchaseReturn(id) {
  return post(`/erp/purchase-returns/submit-for-approval/${id}`)
}

export function approvePurchaseReturn(id) {
  return post(`/erp/purchase-returns/approve-return/${id}`)
}

export function cancelPurchaseReturn(id) {
  return post(`/erp/purchase-returns/cancel-return/${id}`)
}

// ============ 销售退货 API ============

export function getSalesReturnPage(params) {
  return get('/erp/sales-returns/get-return-page', params)
}

export function getSalesReturnDetail(id) {
  return get(`/erp/sales-returns/get-return/${id}`)
}

export function createSalesReturn(data) {
  return post('/erp/sales-returns/create-return', data)
}

export function submitSalesReturn(id) {
  return post(`/erp/sales-returns/submit-for-approval/${id}`)
}

export function approveSalesReturn(id) {
  return post(`/erp/sales-returns/approve-return/${id}`)
}

export function cancelSalesReturn(id) {
  return post(`/erp/sales-returns/cancel-return/${id}`)
}

// ============ 商品促销 API ============

export function getProductPromotionPage(params) {
  return get('/erp/product-promotions/get-promotion-page', params)
}

export function getActivePromotions(productId) {
  return get(`/erp/product-promotions/get-active-promotions/${productId}`)
}

export function createProductPromotion(data) {
  return post('/erp/product-promotions/create-promotion', data)
}

export function updateProductPromotion(id, data) {
  return put(`/erp/product-promotions/update-promotion/${id}`, data)
}

export function deleteProductPromotion(id) {
  return del(`/erp/product-promotions/delete-promotion/${id}`)
}

export function enableProductPromotion(id) {
  return post(`/erp/product-promotions/enable-promotion/${id}`)
}

export function disableProductPromotion(id) {
  return post(`/erp/product-promotions/disable-promotion/${id}`)
}

// ============ 商品价格 API ============

export function getProductPricePage(params) {
  return get('/erp/product-prices/get-price-page', params)
}

export function getPricesByProduct(productId) {
  return get(`/erp/product-prices/get-prices-by-product/${productId}`)
}

export function createProductPrice(data) {
  return post('/erp/product-prices/create-price', data)
}

export function updateProductPrice(id, data) {
  return put(`/erp/product-prices/update-price/${id}`, data)
}

export function deleteProductPrice(id) {
  return del(`/erp/product-prices/delete-price/${id}`)
}

// ============ 报表统计 API ============

export function getDashboard() {
  return get('/erp/reports/dashboard')
}

export function getSalesReport(params) {
  return get('/erp/reports/sales', params)
}

export function getPurchaseReport(params) {
  return get('/erp/reports/purchase', params)
}

export function getInventoryReport(params) {
  return get('/erp/reports/inventory', params)
}

// ============ 系统配置 API ============

export function getConfigPage(params) {
  return get('/erp/configs/get-config-page', params)
}

export function getConfigsByType(configType) {
  return get(`/erp/configs/get-configs-by-type/${configType}`)
}

export function getConfigValue(configKey) {
  return get('/erp/configs/get-config-value', { configKey })
}

export function createConfig(data) {
  return post('/erp/configs/create-config', data)
}

export function updateConfig(id, data) {
  return put(`/erp/configs/update-config/${id}`, data)
}

export function deleteConfig(id) {
  return del(`/erp/configs/delete-config/${id}`)
}

export function batchUpdateConfigs(configType, data) {
  return post(`/erp/configs/batch-update?configType=${configType}`, data)
}
