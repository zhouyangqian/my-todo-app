// api/finance.ts - 财务管理 API 接口模块
// 提供应收账款、应付账款、收支记录、银行账户的管理与核销等接口

import { get, post, put, del } from '@/utils/request'

// 应收账款类型定义（客户欠款）
export interface AccountReceivable {
  id: number              // 应收账款唯一标识
  tenantId: number        // 所属租户ID
  customerId: number      // 客户ID
  bizType: string         // 业务类型（如 销售订单等）
  bizId: string           // 关联业务单据ID
  amount: number          // 应收总金额
  paidAmount: number      // 已收金额
  unpaidAmount: number    // 未收金额（= 应收总金额 - 已收金额）
  status: number          // 结清状态：0-未结清, 1-已结清
  dueDate: string         // 到期日期
  createdAt: string       // 创建时间
}

// 应付账款类型定义（欠供应商款项）
export interface AccountPayable {
  id: number              // 应付账款唯一标识
  tenantId: number        // 所属租户ID
  supplierId: number      // 供应商ID
  bizType: string         // 业务类型（如 采购订单等）
  bizId: string           // 关联业务单据ID
  amount: number          // 应付总金额
  paidAmount: number      // 已付金额
  unpaidAmount: number    // 未付金额
  status: number          // 结清状态：0-未结清, 1-已结清
  dueDate: string         // 到期日期
  createdAt: string       // 创建时间
}

// 收支记录类型定义
export interface PaymentRecord {
  id: number              // 记录唯一标识
  tenantId: number        // 所属租户ID
  type: number            // 收支类型：1-收入, 2-支出
  category: string        // 收支分类（如 货款、运费、工资等）
  amount: number          // 金额
  accountId: number       // 关联银行账户ID
  bizType: string         // 业务类型
  bizId: string           // 关联业务单据ID
  relatedId: number       // 关联ID（如应收/应付账款ID）
  remark: string          // 备注
  paymentDate: string     // 收支日期
  createdAt: string       // 创建时间
}

// 银行账户类型定义
export interface BankAccount {
  id: number              // 账户唯一标识
  tenantId: number        // 所属租户ID
  name: string            // 账户名称
  accountNo: string       // 银行账号
  bankName: string        // 开户银行
  bankBranch: string      // 开户支行
  balance: number         // 账户余额
  currency: string        // 币种（如 CNY、USD）
  status: number          // 状态：1-启用，0-禁用
}

// 通用分页查询结果类型
export interface PageResult<T> {
  records: T[]            // 当前页数据列表
  total: number           // 总记录数
  size: number            // 每页条数
  current: number         // 当前页码
  pages: number           // 总页数
}

// ============ 应收账款 API ============

/**
 * 分页查询应收账款列表
 * @param params 分页参数和筛选条件（客户ID、状态、日期范围）
 */
export function getReceivablePage(params: {
  page?: number
  size?: number
  customerId?: number
  status?: number
  startDate?: string
  endDate?: string
}): Promise<PageResult<AccountReceivable>> {
  return get('/finance/receivables', params)
}

/**
 * 根据ID获取应收账款详情
 * @param id 应收账款ID
 */
export function getReceivable(id: number): Promise<AccountReceivable> {
  return get(`/finance/receivables/${id}`)
}

/**
 * 创建新的应收账款记录
 * @param data 应收账款信息
 */
export function createReceivable(data: Partial<AccountReceivable>): Promise<AccountReceivable> {
  return post('/finance/receivables', data)
}

/**
 * 应收账款收款核销（确认收到客户付款，冲减应收金额）
 * @param id 应收账款ID
 * @param data 核销信息（收款金额、银行账户ID、备注）
 */
export function writeOffReceivable(id: number, data: {
  amount: number          // 本次收款金额
  accountId: number       // 收款银行账户ID
  remark?: string         // 备注
}): Promise<void> {
  return post(`/finance/receivables/${id}/writeoff`, data)
}

// ============ 应付账款 API ============

/**
 * 分页查询应付账款列表
 * @param params 分页参数和筛选条件（供应商ID、状态、日期范围）
 */
export function getPayablePage(params: {
  page?: number
  size?: number
  supplierId?: number
  status?: number
  startDate?: string
  endDate?: string
}): Promise<PageResult<AccountPayable>> {
  return get('/finance/payables', params)
}

/**
 * 根据ID获取应付账款详情
 * @param id 应付账款ID
 */
export function getPayable(id: number): Promise<AccountPayable> {
  return get(`/finance/payables/${id}`)
}

/**
 * 创建新的应付账款记录
 * @param data 应付账款信息
 */
export function createPayable(data: Partial<AccountPayable>): Promise<AccountPayable> {
  return post('/finance/payables', data)
}

/**
 * 应付账款付款核销（确认向供应商付款，冲减应付金额）
 * @param id 应付账款ID
 * @param data 核销信息（付款金额、银行账户ID、备注）
 */
export function writeOffPayable(id: number, data: {
  amount: number          // 本次付款金额
  accountId: number       // 付款银行账户ID
  remark?: string         // 备注
}): Promise<void> {
  return post(`/finance/payables/${id}/writeoff`, data)
}

// ============ 收支记录 API ============

/**
 * 分页查询收支记录列表
 * @param params 分页参数和筛选条件（收支类型、分类、日期范围）
 */
export function getPaymentRecordPage(params: {
  page?: number
  size?: number
  type?: number
  category?: string
  startDate?: string
  endDate?: string
}): Promise<PageResult<PaymentRecord>> {
  return get('/finance/records', params)
}

/**
 * 创建新的收支记录
 * @param data 收支记录信息
 */
export function createPaymentRecord(data: Partial<PaymentRecord>): Promise<PaymentRecord> {
  return post('/finance/records', data)
}

/**
 * 获取指定时间范围内的收支统计数据
 * @param params 统计时间范围（开始日期、结束日期）
 * @returns 统计结果（总收入、总支出、净额、按分类汇总）
 */
export function getPaymentStatistics(params: {
  startDate: string
  endDate: string
}): Promise<{
  totalIncome: number                                    // 总收入
  totalExpense: number                                   // 总支出
  netAmount: number                                      // 净额（收入 - 支出）
  byCategory: { category: string; amount: number }[]     // 按分类汇总的收支数据
}> {
  return get('/finance/records/statistics', params)
}

// ============ 银行账户 API ============

/**
 * 获取所有银行账户列表（不分页）
 */
export function getBankAccountList(): Promise<BankAccount[]> {
  return get('/finance/accounts')
}

/**
 * 根据ID获取银行账户详情
 * @param id 银行账户ID
 */
export function getBankAccount(id: number): Promise<BankAccount> {
  return get(`/finance/accounts/${id}`)
}

/**
 * 创建新的银行账户
 * @param data 银行账户信息
 */
export function createBankAccount(data: Partial<BankAccount>): Promise<BankAccount> {
  return post('/finance/accounts', data)
}

/**
 * 更新银行账户信息
 * @param id 银行账户ID
 * @param data 需要更新的账户字段
 */
export function updateBankAccount(id: number, data: Partial<BankAccount>): Promise<BankAccount> {
  return put(`/finance/accounts/${id}`, data)
}

/**
 * 删除银行账户
 * @param id 银行账户ID
 */
export function deleteBankAccount(id: number): Promise<void> {
  return del(`/finance/accounts/${id}`)
}

/**
 * 手动调整银行账户余额（用于银行对账差异调整）
 * @param id 银行账户ID
 * @param data 调整信息（金额、调整类型、备注）
 */
export function adjustBalance(id: number, data: {
  amount: number          // 调整金额
  type: number            // 调整类型：1-增加, 2-减少
  remark?: string         // 调整原因备注
}): Promise<void> {
  return post(`/finance/accounts/${id}/adjust`, data)
}
