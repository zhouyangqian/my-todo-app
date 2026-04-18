// api/dict.ts - 字典与系统配置 API 接口模块
// 提供字典类型的增删改查、字典项管理、系统配置管理等功能接口

import { get, post, put, del } from '@/utils/request'

// 字典类型（如：性别、状态、类型等分类）
export interface DictType {
  id: number              // 字典类型唯一标识
  tenantId: number        // 所属租户ID
  name: string            // 字典类型名称（如"性别"）
  code: string            // 字典类型编码（如"gender"）
  description: string     // 描述说明
  status: number          // 状态：1-启用，0-禁用
  createdAt: string       // 创建时间
}

// 字典项（字典类型下的具体选项，如性别下的"男"、"女"）
export interface DictItem {
  id: number              // 字典项唯一标识
  tenantId: number        // 所属租户ID
  typeId: number          // 所属字典类型ID
  label: string           // 显示文本（如"男"）
  value: string           // 实际值（如"1"）
  sort: number            // 排序序号
  status: number          // 状态：1-启用，0-禁用
  remark: string          // 备注说明
}

// 系统配置项
export interface SystemConfig {
  id: number              // 配置唯一标识
  tenantId: number        // 所属租户ID
  configKey: string       // 配置键名
  configValue: string     // 配置值
  configType: string      // 配置类型（如 string、number、json）
  description: string     // 配置描述
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

// ============ 字典类型 API ============

/**
 * 获取字典类型列表
 * @param params 可选的筛选参数（名称、编码、状态）
 */
export function getDictTypeList(params?: {
  name?: string
  code?: string
  status?: number
}): Promise<DictType[]> {
  return get('/dict/types', params)
}

/**
 * 创建新的字典类型
 * @param data 字典类型信息
 */
export function createDictType(data: Partial<DictType>): Promise<DictType> {
  return post('/dict/types', data)
}

/**
 * 更新字典类型信息
 * @param id 字典类型ID
 * @param data 需要更新的字段
 */
export function updateDictType(id: number, data: Partial<DictType>): Promise<DictType> {
  return put(`/dict/types/${id}`, data)
}

/**
 * 删除字典类型
 * @param id 字典类型ID
 */
export function deleteDictType(id: number): Promise<void> {
  return del(`/dict/types/${id}`)
}

// ============ 字典项 API ============

/**
 * 根据字典类型编码获取该类型下的所有字典项
 * 常用于前端下拉框、单选框等组件的数据源加载
 * @param code 字典类型编码（如 "gender"）
 */
export function getDictItemsByCode(code: string): Promise<DictItem[]> {
  return get(`/dict/items/code/${code}`)
}

/**
 * 根据字典类型ID获取该类型下的所有字典项
 * @param typeId 字典类型ID
 */
export function getDictItemsByTypeId(typeId: number): Promise<DictItem[]> {
  return get(`/dict/items/type/${typeId}`)
}

/**
 * 创建新的字典项
 * @param data 字典项信息
 */
export function createDictItem(data: Partial<DictItem>): Promise<DictItem> {
  return post('/dict/items', data)
}

/**
 * 更新字典项信息
 * @param id 字典项ID
 * @param data 需要更新的字段
 */
export function updateDictItem(id: number, data: Partial<DictItem>): Promise<DictItem> {
  return put(`/dict/items/${id}`, data)
}

/**
 * 删除字典项
 * @param id 字典项ID
 */
export function deleteDictItem(id: number): Promise<void> {
  return del(`/dict/items/${id}`)
}

// ============ 系统配置 API ============

/**
 * 获取系统配置列表
 * @param params 可选的筛选参数（配置键名、配置类型）
 */
export function getConfigList(params?: {
  configKey?: string
  configType?: string
}): Promise<SystemConfig[]> {
  return get('/dict/configs', params)
}

/**
 * 根据配置键名获取配置值
 * @param configKey 配置键名
 * @returns 配置值字符串
 */
export function getConfigValue(configKey: string): Promise<string> {
  return get(`/dict/configs/${configKey}`)
}

/**
 * 新增系统配置
 * @param data 配置信息
 */
export function saveConfig(data: Partial<SystemConfig>): Promise<SystemConfig> {
  return post('/dict/configs', data)
}

/**
 * 更新系统配置
 * @param id 配置ID
 * @param data 需要更新的配置字段
 */
export function updateConfig(id: number, data: Partial<SystemConfig>): Promise<SystemConfig> {
  return put(`/dict/configs/${id}`, data)
}

/**
 * 删除系统配置
 * @param id 配置ID
 */
export function deleteConfig(id: number): Promise<void> {
  return del(`/dict/configs/${id}`)
}
