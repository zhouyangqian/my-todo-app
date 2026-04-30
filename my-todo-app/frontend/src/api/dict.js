// api/dict.js - 字典与系统配置 API 接口模块
// 提供字典类型的增删改查、字典项管理、系统配置管理等功能接口

import { get, post, put, del } from '@/utils/request'

// ============ 字典类型 API ============

/**
 * 分页查询字典类型
 * @param {Object} params 分页参数和筛选条件
 * @returns {Promise}
 */
export function getDictTypePage(params) {
  return get('/dict/types/get-dict-type-page', params)
}

/**
 * 获取字典类型详情
 * @param {string} id 字典类型ID
 * @returns {Promise}
 */
export function getDictType(id) {
  return get(`/dict/types/get-dict-type/${id}`)
}

/**
 * 创建新的字典类型
 * @param {Object} data 字典类型信息
 * @returns {Promise}
 */
export function createDictType(data) {
  return post('/dict/types/create-dict-type', data)
}

/**
 * 更新字典类型信息
 * @param {string} id 字典类型ID
 * @param {Object} data 需要更新的字段
 * @returns {Promise}
 */
export function updateDictType(id, data) {
  return put(`/dict/types/update-dict-type/${id}`, data)
}

/**
 * 删除字典类型
 * @param {string} id 字典类型ID
 * @returns {Promise}
 */
export function deleteDictType(id) {
  return del(`/dict/types/delete-dict-type/${id}`)
}

// ============ 字典项 API ============

/**
 * 根据字典类型编码获取该类型下的所有字典项
 * @param {string} code 字典类型编码（如 "gender"）
 * @returns {Promise}
 */
export function getDictItemsByCode(code) {
  return get(`/dict/items/code/${code}`)
}

/**
 * 根据字典类型ID获取该类型下的所有字典项
 * @param {string} typeId 字典类型ID
 * @returns {Promise}
 */
export function getDictItemsByTypeId(typeId) {
  return get(`/dict/items/type/${typeId}`)
}

/**
 * 创建新的字典项
 * @param {Object} data 字典项信息
 * @returns {Promise}
 */
export function createDictItem(data) {
  return post('/dict/items/add-dict-item', data)
}

/**
 * 更新字典项信息
 * @param {string} id 字典项ID
 * @param {Object} data 需要更新的字段
 * @returns {Promise}
 */
export function updateDictItem(id, data) {
  return put(`/dict/items/update-dict-item/${id}`, data)
}

/**
 * 删除字典项
 * @param {string} id 字典项ID
 * @returns {Promise}
 */
export function deleteDictItem(id) {
  return del(`/dict/items/delete-dict-item/${id}`)
}

// ============ 系统配置 API ============

/**
 * 获取所有系统配置列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getConfigList(params) {
  return get('/config/list', params)
}

/**
 * 分页查询系统配置
 * @param {Object} params 分页参数
 * @returns {Promise}
 */
export function getConfigPage(params) {
  return get('/config/page', params)
}

/**
 * 根据配置键名获取配置值
 * @param {string} configCode 配置键名
 * @returns {Promise}
 */
export function getConfigValue(configCode) {
  return get(`/config/value/${configCode}`)
}

/**
 * 获取配置详情
 * @param {string} configCode 配置键名
 * @returns {Promise}
 */
export function getConfig(configCode) {
  return get(`/config/${configCode}`)
}

/**
 * 新增系统配置
 * @param {Object} data 配置信息
 * @returns {Promise}
 */
export function saveConfig(data) {
  return post('/config', data)
}

/**
 * 新增系统配置（别名）
 * @param {Object} data 配置信息
 * @returns {Promise}
 */
export function createConfig(data) {
  return saveConfig(data)
}

/**
 * 更新系统配置
 * @param {string} id 配置ID
 * @param {Object} data 需要更新的配置字段
 * @returns {Promise}
 */
export function updateConfig(id, data) {
  return put(`/config/${id}`, data)
}

/**
 * 删除系统配置
 * @param {string} id 配置ID
 * @returns {Promise}
 */
export function deleteConfig(id) {
  return del(`/config/${id}`)
}
