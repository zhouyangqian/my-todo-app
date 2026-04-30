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
