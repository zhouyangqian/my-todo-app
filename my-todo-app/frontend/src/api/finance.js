// api/finance.js - 财务管理 API 接口模块
// 提供应收账款、应付账款、收支记录、银行账户的管理与核销等接口

import { get, post, put, del } from '@/utils/request'

// ============ 应收账款 API ============

/**
 * 分页查询应收账款列表
 * @param params 分页参数和筛选条件（客户ID、状态、日期范围）
 */
export function getReceivablePage(params) {
  return get('/finance/receivables', params)
}

/**
 * 根据ID获取应收账款详情
 * @param id 应收账款ID
 */
export function getReceivable(id) {
  return get(`/finance/receivables/${id}`)
}

/**
 * 创建新的应收账款记录
 * @param data 应收账款信息
 */
export function createReceivable(data) {
  return post('/finance/receivables', data)
}

/**
 * 应收账款收款核销（确认收到客户付款，冲减应收金额）
 * @param id 应收账款ID
 * @param data 核销信息（收款金额、银行账户ID、备注）
 */
export function writeOffReceivable(id, data) {
  return post(`/finance/receivables/${id}/writeoff`, data)
}

// ============ 应付账款 API ============

/**
 * 分页查询应付账款列表
 * @param params 分页参数和筛选条件（供应商ID、状态、日期范围）
 */
export function getPayablePage(params) {
  return get('/finance/payables', params)
}

/**
 * 根据ID获取应付账款详情
 * @param id 应付账款ID
 */
export function getPayable(id) {
  return get(`/finance/payables/${id}`)
}

/**
 * 创建新的应付账款记录
 * @param data 应付账款信息
 */
export function createPayable(data) {
  return post('/finance/payables', data)
}

/**
 * 应付账款付款核销（确认向供应商付款，冲减应付金额）
 * @param id 应付账款ID
 * @param data 核销信息（付款金额、银行账户ID、备注）
 */
export function writeOffPayable(id, data) {
  return post(`/finance/payables/${id}/writeoff`, data)
}

// ============ 收支记录 API ============

/**
 * 分页查询收支记录列表
 * @param params 分页参数和筛选条件（收支类型、分类、日期范围）
 */
export function getPaymentRecordPage(params) {
  return get('/finance/records', params)
}

/**
 * 创建新的收支记录
 * @param data 收支记录信息
 */
export function createPaymentRecord(data) {
  return post('/finance/records', data)
}

/**
 * 获取指定时间范围内的收支统计数据
 * @param params 统计时间范围（开始日期、结束日期）
 */
export function getPaymentStatistics(params) {
  return get('/finance/records/statistics', params)
}

// ============ 银行账户 API ============

/**
 * 获取所有银行账户列表（不分页）
 */
export function getBankAccountList() {
  return get('/finance/accounts')
}

/**
 * 根据ID获取银行账户详情
 * @param id 银行账户ID
 */
export function getBankAccount(id) {
  return get(`/finance/accounts/${id}`)
}

/**
 * 创建新的银行账户
 * @param data 银行账户信息
 */
export function createBankAccount(data) {
  return post('/finance/accounts', data)
}

/**
 * 更新银行账户信息
 * @param id 银行账户ID
 * @param data 需要更新的账户字段
 */
export function updateBankAccount(id, data) {
  return put(`/finance/accounts/${id}`, data)
}

/**
 * 删除银行账户
 * @param id 银行账户ID
 */
export function deleteBankAccount(id) {
  return del(`/finance/accounts/${id}`)
}

/**
 * 手动调整银行账户余额（用于银行对账差异调整）
 * @param id 银行账户ID
 * @param data 调整信息（金额、调整类型、备注）
 */
export function adjustBalance(id, data) {
  return post(`/finance/accounts/${id}/adjust`, data)
}
