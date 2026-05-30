// api/finance.js - 财务管理 API 接口模块
// 提供应收账款、应付账款、收支记录、银行账户的管理与核销等接口

import { get, post, put, del } from '@/utils/request'

// ============ 应收账款 API ============

/**
 * 分页查询应收账款列表
 * @param {Object} params 分页参数和筛选条件（客户ID、状态）
 * @returns {Promise}
 */
export function getReceivablePage(params) {
  return get('/finance/receivables/get-receivable-page', params)
}

/**
 * 获取逾期应收账款列表
 * @returns {Promise}
 */
export function getOverdueReceivables() {
  return get('/finance/receivables/get-overdue-receivables')
}

/**
 * 创建新的应收账款记录
 * @param {Object} data 应收账款信息
 * @returns {Promise}
 */
export function createReceivable(data) {
  return post('/finance/receivables/create-receivable', data)
}

/**
 * 应收账款收款确认
 * @param {string} id 应收账款ID
 * @param {number} amount 收款金额
 * @returns {Promise}
 */
export function receivePayment(id, amount) {
  return post(`/finance/receivables/receive-payment/${id}`, { amount })
}

// ============ 应付账款 API ============

/**
 * 分页查询应付账款列表
 * @param {Object} params 分页参数和筛选条件（供应商ID、状态）
 * @returns {Promise}
 */
export function getPayablePage(params) {
  return get('/finance/payables/get-payable-page', params)
}

/**
 * 获取逾期应付账款列表
 * @returns {Promise}
 */
export function getOverduePayables() {
  return get('/finance/payables/get-overdue-payables')
}

/**
 * 创建新的应付账款记录
 * @param {Object} data 应付账款信息
 * @returns {Promise}
 */
export function createPayable(data) {
  return post('/finance/payables/create-payable', data)
}

/**
 * 应付账款付款确认
 * @param {string} id 应付账款ID
 * @param {number} amount 付款金额
 * @returns {Promise}
 */
export function makePayment(id, amount) {
  return post(`/finance/payables/make-payment/${id}`, { amount })
}

// ============ 收支记录 API ============

/**
 * 分页查询收支记录列表
 * @param {Object} params 分页参数和筛选条件（收支类型、业务类型、日期范围）
 * @returns {Promise}
 */
export function getPaymentRecordPage(params) {
  return get('/finance/records/get-record-page', params)
}

/**
 * 创建新的收支记录
 * @param {Object} data 收支记录信息
 * @returns {Promise}
 */
export function createPaymentRecord(data) {
  return post('/finance/records/create-record', data)
}

/**
 * 审核收支记录
 * @param {string} id 收支记录ID
 * @returns {Promise}
 */
export function approveRecord(id) {
  return post(`/finance/records/approve-record/${id}`)
}

/**
 * 取消收支记录
 * @param {string} id 收支记录ID
 * @returns {Promise}
 */
export function cancelRecord(id) {
  return post(`/finance/records/cancel-record/${id}`)
}

// ============ 银行账户 API ============

/**
 * 分页查询银行账户列表
 * @param {Object} params 分页参数和筛选条件
 * @returns {Promise}
 */
export function getBankAccountPage(params) {
  return get('/finance/bank-accounts/get-bank-account-page', params)
}

/**
 * 获取所有银行账户列表（不分页，用于下拉选择）
 * @returns {Promise}
 */
export function getAllBankAccounts() {
  return get('/finance/bank-accounts/get-all-bank-accounts')
}

/**
 * 创建新的银行账户
 * @param {Object} data 银行账户信息
 * @returns {Promise}
 */
export function createBankAccount(data) {
  return post('/finance/bank-accounts/create-bank-account', data)
}

/**
 * 更新银行账户信息
 * @param {string} id 银行账户ID
 * @param {Object} data 需要更新的账户字段
 * @returns {Promise}
 */
export function updateBankAccount(id, data) {
  return put(`/finance/bank-accounts/update-bank-account/${id}`, data)
}

/**
 * 删除银行账户
 * @param {string} id 银行账户ID
 * @returns {Promise}
 */
export function deleteBankAccount(id) {
  return del(`/finance/bank-accounts/delete-bank-account/${id}`)
}

// ============ 发票管理 API ============

/**
 * 分页查询发票列表
 * @param {Object} params 分页参数和筛选条件（发票类型、方向、状态）
 * @returns {Promise}
 */
export function getInvoicePage(params) {
  return get('/finance/invoices/get-invoice-page', params)
}

/**
 * 获取发票详情
 * @param {string} id 发票ID
 * @returns {Promise}
 */
export function getInvoice(id) {
  return get(`/finance/invoices/get-invoice/${id}`)
}

/**
 * 创建发票
 * @param {Object} data 发票信息
 * @returns {Promise}
 */
export function createInvoice(data) {
  return post('/finance/invoices/create-invoice', data)
}

/**
 * 更新发票
 * @param {string} id 发票ID
 * @param {Object} data 发票信息
 * @returns {Promise}
 */
export function updateInvoice(id, data) {
  return put(`/finance/invoices/update-invoice/${id}`, data)
}

/**
 * 删除发票
 * @param {string} id 发票ID
 * @returns {Promise}
 */
export function deleteInvoice(id) {
  return del(`/finance/invoices/delete-invoice/${id}`)
}

/**
 * 作废发票
 * @param {string} id 发票ID
 * @returns {Promise}
 */
export function voidInvoice(id) {
  return post(`/finance/invoices/void-invoice/${id}`)
}

/**
 * 获取发票统计信息
 * @returns {Promise}
 */
export function getInvoiceStatistics() {
  return get('/finance/invoices/get-statistics')
}

// ============ 成本核算 API ============

/**
 * 分页查询成本配置列表
 * @param {Object} params 分页参数
 * @returns {Promise}
 */
export function getCostConfigPage(params) {
  return get('/finance/cost/get-config-page', params)
}

/**
 * 设置商品成本核算方法
 * @param {Object} params productId, costMethod
 * @returns {Promise}
 */
export function setCostMethod(params) {
  return post('/finance/cost/set-cost-method', params)
}

/**
 * 计算商品出库成本
 * @param {Object} params productId, quantity, warehouseId
 * @returns {Promise}
 */
export function calculateOutboundCost(params) {
  return get('/finance/cost/calculate-outbound-cost', params)
}

/**
 * 分页查询成本历史
 * @param {Object} params 分页参数和筛选条件
 * @returns {Promise}
 */
export function getCostHistoryPage(params) {
  return get('/finance/cost/get-cost-history-page', params)
}

// ============ 财务报表 API ============

/**
 * 生成利润表
 * @param {Object} params startDate, endDate
 * @returns {Promise}
 */
export function generateIncomeStatement(params) {
  return post('/finance/reports/generate-income-statement', params)
}

/**
 * 生成资产负债表
 * @param {Object} params asOfDate
 * @returns {Promise}
 */
export function generateBalanceSheet(params) {
  return post('/finance/reports/generate-balance-sheet', params)
}

/**
 * 生成现金流量表
 * @param {Object} params startDate, endDate
 * @returns {Promise}
 */
export function generateCashFlow(params) {
  return post('/finance/reports/generate-cash-flow', params)
}

/**
 * 获取报表列表
 * @param {Object} params reportType (可选)
 * @returns {Promise}
 */
export function getReportList(params) {
  return get('/finance/reports/get-report-list', params)
}

/**
 * 锁定报表
 * @param {string} id 报表ID
 * @returns {Promise}
 */
export function lockReport(id) {
  return post(`/finance/reports/lock-report/${id}`)
}

// ============ 银行对账 API ============

/**
 * 导入银行对账单
 * @param {FormData} data 包含file和bankAccountId的FormData
 * @returns {Promise}
 */
export function importBankStatement(data) {
  return post('/finance/bank-reconciliation/import', data)
}

/**
 * 自动匹配对账
 * @param {string} id 对账记录ID
 * @returns {Promise}
 */
export function autoMatchReconciliation(id) {
  return post(`/finance/bank-reconciliation/auto-match/${id}`)
}

/**
 * 手动匹配银行记录
 * @param {Object} data bankRecordId, systemRecordId
 * @returns {Promise}
 */
export function manualMatchBankRecord(data) {
  return post('/finance/bank-reconciliation/manual-match', data)
}

/**
 * 分页查询银行对账列表
 * @param {Object} params 分页参数
 * @returns {Promise}
 */
export function getBankReconciliationPage(params) {
  return get('/finance/bank-reconciliation/page', params)
}

/**
 * 获取未匹配记录
 * @param {string} id 对账记录ID
 * @returns {Promise}
 */
export function getUnmatchedRecords(id) {
  return get(`/finance/bank-reconciliation/unmatched/${id}`)
}

/**
 * 取消匹配
 * @param {string} bankRecordId 银行记录ID
 * @returns {Promise}
 */
export function unmatchBankRecord(bankRecordId) {
  return post(`/finance/bank-reconciliation/unmatch/${bankRecordId}`)
}

// ============ 预算管理 API ============

/**
 * 分页查询预算列表
 * @param {Object} params 分页参数和筛选条件
 * @returns {Promise}
 */
export function getBudgetPage(params) {
  return get('/finance/budgets/page', params)
}

/**
 * 创建预算
 * @param {Object} data 预算信息
 * @returns {Promise}
 */
export function createBudget(data) {
  return post('/finance/budgets/create', data)
}

/**
 * 更新预算
 * @param {string} id 预算ID
 * @param {Object} data 预算信息
 * @returns {Promise}
 */
export function updateBudget(id, data) {
  return put(`/finance/budgets/update/${id}`, data)
}

/**
 * 审批预算
 * @param {string} id 预算ID
 * @returns {Promise}
 */
export function approveBudget(id) {
  return post(`/finance/budgets/approve/${id}`)
}

/**
 * 删除预算
 * @param {string} id 预算ID
 * @returns {Promise}
 */
export function deleteBudget(id) {
  return del(`/finance/budgets/delete/${id}`)
}

/**
 * 获取预算执行情况
 * @param {string} id 预算ID
 * @returns {Promise}
 */
export function getBudgetExecution(id) {
  return get(`/finance/budgets/execution/${id}`)
}
