// api/dict.js - 字典与系统配置 API 接口模块
// 提供字典类型的增删改查、字典项管理、系统配置管理等功能接口

import { get, post, put, del } from '@/utils/request'

// ============ 字典类型 API ============

/**
 * 获取字典类型列表
 * @param params 可选的筛选参数（名称、编码、状态）
 */
export function getDictTypeList(params) {
  return get('/dict/types', params)
}

/**
 * 创建新的字典类型
 * @param data 字典类型信息
 */
export function createDictType(data) {
  return post('/dict/types', data)
}

/**
 * 更新字典类型信息
 * @param id 字典类型ID
 * @param data 需要更新的字段
 */
export function updateDictType(id, data) {
  return put(`/dict/types/${id}`, data)
}

/**
 * 删除字典类型
 * @param id 字典类型ID
 */
export function deleteDictType(id) {
  return del(`/dict/types/${id}`)
}

// ============ 字典项 API ============

/**
 * 根据字典类型编码获取该类型下的所有字典项
 * 常用于前端下拉框、单选框等组件的数据源加载
 * @param code 字典类型编码（如 "gender"）
 */
export function getDictItemsByCode(code) {
  return get(`/dict/items/code/${code}`)
}

/**
 * 根据字典类型ID获取该类型下的所有字典项
 * @param typeId 字典类型ID
 */
export function getDictItemsByTypeId(typeId) {
  return get(`/dict/items/type/${typeId}`)
}

/**
 * 创建新的字典项
 * @param data 字典项信息
 */
export function createDictItem(data) {
  return post('/dict/items', data)
}

/**
 * 更新字典项信息
 * @param id 字典项ID
 * @param data 需要更新的字段
 */
export function updateDictItem(id, data) {
  return put(`/dict/items/${id}`, data)
}

/**
 * 删除字典项
 * @param id 字典项ID
 */
export function deleteDictItem(id) {
  return del(`/dict/items/${id}`)
}

// ============ 系统配置 API ============

/**
 * 获取系统配置列表
 * @param params 可选的筛选参数（配置键名、配置类型）
 */
export function getConfigList(params) {
  return get('/dict/configs', params)
}

/**
 * 根据配置键名获取配置值
 * @param configKey 配置键名
 */
export function getConfigValue(configKey) {
  return get(`/dict/configs/${configKey}`)
}

/**
 * 新增系统配置
 * @param data 配置信息
 */
export function saveConfig(data) {
  return post('/dict/configs', data)
}

/**
 * 更新系统配置
 * @param id 配置ID
 * @param data 需要更新的配置字段
 */
export function updateConfig(id, data) {
  return put(`/dict/configs/${id}`, data)
}

/**
 * 删除系统配置
 * @param id 配置ID
 */
export function deleteConfig(id) {
  return del(`/dict/configs/${id}`)
}
