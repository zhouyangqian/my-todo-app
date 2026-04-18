// api/user.ts - 用户管理 API 接口模块
// 提供用户的增删改查、启用/禁用、地址管理等接口

import { get, post, put, del } from '@/utils/request'

// 用户信息类型定义
export interface User {
  id: number              // 用户唯一标识
  tenantId: number        // 所属租户ID（多租户架构）
  username: string        // 登录用户名
  email: string           // 邮箱地址
  phone: string           // 手机号码
  realName: string        // 真实姓名
  avatar: string          // 头像URL
  deptId: number          // 所属部门ID
  status: number          // 状态：1-启用，0-禁用
  createdAt: string       // 创建时间
  updatedAt: string       // 更新时间
}

// 用户收货地址类型定义
export interface UserAddress {
  id: number              // 地址唯一标识
  userId: number          // 所属用户ID
  receiverName: string    // 收件人姓名
  receiverPhone: string   // 收件人手机号
  province: string        // 省份
  city: string            // 城市
  district: string        // 区/县
  detailAddress: string   // 详细地址
  isDefault: number       // 是否为默认地址：1-是，0-否
}

// 通用分页查询结果类型
export interface PageResult<T> {
  records: T[]            // 当前页数据列表
  total: number           // 总记录数
  size: number            // 每页条数
  current: number         // 当前页码
  pages: number           // 总页数
}

/**
 * 分页查询用户列表
 * @param params 查询参数（分页、用户名、姓名、部门、状态等筛选条件）
 * @returns 分页结果，包含用户列表和分页信息
 */
export function getUserPage(params: {
  page?: number
  size?: number
  username?: string
  realName?: string
  deptId?: number
  status?: number
}): Promise<PageResult<User>> {
  return get('/users', params)
}

/**
 * 根据ID获取用户详情
 * @param id 用户ID
 */
export function getUser(id: number): Promise<User> {
  return get(`/users/${id}`)
}

/**
 * 创建新用户
 * @param data 用户信息
 */
export function createUser(data: Partial<User>): Promise<User> {
  return post('/users', data)
}

/**
 * 更新用户信息
 * @param id 用户ID
 * @param data 需要更新的用户字段
 */
export function updateUser(id: number, data: Partial<User>): Promise<User> {
  return put(`/users/${id}`, data)
}

/**
 * 删除用户
 * @param id 用户ID
 */
export function deleteUser(id: number): Promise<void> {
  return del(`/users/${id}`)
}

/**
 * 启用用户（将用户状态设置为启用）
 * @param id 用户ID
 */
export function enableUser(id: number): Promise<void> {
  return post(`/users/${id}/enable`)
}

/**
 * 禁用用户（将用户状态设置为禁用，禁止登录）
 * @param id 用户ID
 */
export function disableUser(id: number): Promise<void> {
  return post(`/users/${id}/disable`)
}

/**
 * 获取用户的所有收货地址
 * @param userId 用户ID
 */
export function getUserAddresses(userId: number): Promise<UserAddress[]> {
  return get(`/users/${userId}/addresses`)
}

/**
 * 为用户添加新的收货地址
 * @param userId 用户ID
 * @param data 地址信息
 */
export function addUserAddress(userId: number, data: Partial<UserAddress>): Promise<UserAddress> {
  return post(`/users/${userId}/addresses`, data)
}

/**
 * 设置用户的默认收货地址
 * @param userId 用户ID
 * @param addressId 地址ID
 */
export function setDefaultAddress(userId: number, addressId: number): Promise<void> {
  return post(`/users/${userId}/addresses/${addressId}/default`)
}
