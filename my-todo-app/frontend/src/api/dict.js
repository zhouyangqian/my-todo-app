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

// ============ 参数分类 API ============

/**
 * 分页查询参数分类
 * @param {Object} params 分页参数和筛选条件
 * @returns {Promise}
 */
export function getParameterCategoryPage(params) {
  return get('/parameter-categories/get-category-page', params)
}

/**
 * 获取所有启用的参数分类（下拉选择用）
 * @returns {Promise}
 */
export function getAllParameterCategories() {
  return get('/parameter-categories/get-all-categories')
}

/**
 * 创建参数分类
 * @param {Object} data 分类信息
 * @returns {Promise}
 */
export function createParameterCategory(data) {
  return post('/parameter-categories/create-category', data)
}

/**
 * 更新参数分类
 * @param {string} id 分类ID
 * @param {Object} data 分类信息
 * @returns {Promise}
 */
export function updateParameterCategory(id, data) {
  return put(`/parameter-categories/update-category/${id}`, data)
}

/**
 * 删除参数分类
 * @param {string} id 分类ID
 * @returns {Promise}
 */
export function deleteParameterCategory(id) {
  return del(`/parameter-categories/delete-category/${id}`)
}

// ============ 参数字典 API ============

/**
 * 分页查询参数字典
 * @param {Object} params 分页参数和筛选条件
 * @returns {Promise}
 */
export function getParameterDictionaryPage(params) {
  return get('/parameter-dictionaries/get-dictionary-page', params)
}

/**
 * 创建参数字典
 * @param {Object} data 字典信息
 * @returns {Promise}
 */
export function createParameterDictionary(data) {
  return post('/parameter-dictionaries/create-dictionary', data)
}

/**
 * 更新参数字典
 * @param {string} id 字典ID
 * @param {Object} data 字典信息
 * @returns {Promise}
 */
export function updateParameterDictionary(id, data) {
  return put(`/parameter-dictionaries/update-dictionary/${id}`, data)
}

/**
 * 删除参数字典
 * @param {string} id 字典ID
 * @returns {Promise}
 */
export function deleteParameterDictionary(id) {
  return del(`/parameter-dictionaries/delete-dictionary/${id}`)
}

/**
 * 按编码查询参数
 * @param {string} code 参数编码
 * @returns {Promise}
 */
export function getParameterByCode(code) {
  return get('/parameter-dictionaries/get-by-code', { code })
}

// ============ 参数项 API ============

/**
 * 查询参数项列表
 * @param {string} dictionaryId 参数字典ID
 * @returns {Promise}
 */
export function getParameterItems(dictionaryId) {
  return get(`/parameter-items/get-items/${dictionaryId}`)
}

/**
 * 创建参数项
 * @param {Object} data 参数项信息
 * @returns {Promise}
 */
export function createParameterItem(data) {
  return post('/parameter-items/create-item', data)
}

/**
 * 更新参数项
 * @param {string} id 参数项ID
 * @param {Object} data 参数项信息
 * @returns {Promise}
 */
export function updateParameterItem(id, data) {
  return put(`/parameter-items/update-item/${id}`, data)
}

/**
 * 删除参数项
 * @param {string} id 参数项ID
 * @returns {Promise}
 */
export function deleteParameterItem(id) {
  return del(`/parameter-items/delete-item/${id}`)
}

/**
 * 批量保存参数项
 * @param {string} dictionaryId 参数字典ID
 * @param {Array} data 参数项列表
 * @returns {Promise}
 */
export function batchSaveParameterItems(dictionaryId, data) {
  return post('/parameter-items/batch-save', { dictionaryId, items: data })
}

// ============ API市场 API (M05) ============

/**
 * 分页查询API列表
 * @param {Object} params 分页参数和筛选条件
 * @returns {Promise}
 */
export function getApiList(params) {
  return get('/api-market/list', params)
}

/**
 * 获取API详情
 * @param {string} id API ID
 * @returns {Promise}
 */
export function getApiDetail(id) {
  return get(`/api-market/detail/${id}`)
}

/**
 * 创建API
 * @param {Object} data API信息
 * @returns {Promise}
 */
export function createApi(data) {
  return post('/api-market/create', data)
}

/**
 * 更新API
 * @param {string} id API ID
 * @param {Object} data API信息
 * @returns {Promise}
 */
export function updateApi(id, data) {
  return put(`/api-market/update/${id}`, data)
}

/**
 * 删除API
 * @param {string} id API ID
 * @returns {Promise}
 */
export function deleteApi(id) {
  return del(`/api-market/delete/${id}`)
}

/**
 * 订阅API
 * @param {Object} data 订阅信息（含 apiId）
 * @returns {Promise}
 */
export function subscribeApi(data) {
  return post('/api-market/subscribe', data)
}

/**
 * 取消订阅API
 * @param {string} id 订阅ID
 * @returns {Promise}
 */
export function unsubscribeApi(id) {
  return del(`/api-market/unsubscribe/${id}`)
}

/**
 * 查询API使用统计
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getApiUsage(params) {
  return get('/api-market/usage', params)
}

/**
 * 记录API调用
 * @param {Object} data 调用记录
 * @returns {Promise}
 */
export function recordApiUsage(data) {
  return post('/api-market/record-usage', data)
}

// ============ 追踪管理 API (M11) ============

/**
 * 查询追踪配置列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getTraceConfigs(params) {
  return get('/trace/configs/page', params)
}

/**
 * 创建追踪配置
 * @param {Object} data 配置信息
 * @returns {Promise}
 */
export function createTraceConfig(data) {
  return post('/trace/configs/create', data)
}

/**
 * 更新追踪配置
 * @param {string} id 配置ID
 * @param {Object} data 配置信息
 * @returns {Promise}
 */
export function updateTraceConfig(id, data) {
  return put(`/trace/configs/update/${id}`, data)
}

/**
 * 删除追踪配置
 * @param {string} id 配置ID
 * @returns {Promise}
 */
export function deleteTraceConfig(id) {
  return del(`/trace/configs/delete/${id}`)
}

/**
 * 查询追踪告警列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getTraceAlerts(params) {
  return get('/trace/alerts/page', params)
}

/**
 * 确认告警
 * @param {string} id 告警ID
 * @returns {Promise}
 */
export function acknowledgeAlert(id) {
  return put(`/trace/alerts/acknowledge/${id}`)
}

// ============ 第三方API API (M04) ============

/**
 * 查询第三方API列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getThirdPartyApis(params) {
  return get('/third-party/page', params)
}

/**
 * 创建第三方API
 * @param {Object} data API信息
 * @returns {Promise}
 */
export function createThirdPartyApi(data) {
  return post('/third-party/create', data)
}

/**
 * 更新第三方API
 * @param {string} id API ID
 * @param {Object} data API信息
 * @returns {Promise}
 */
export function updateThirdPartyApi(id, data) {
  return put(`/third-party/update/${id}`, data)
}

/**
 * 删除第三方API
 * @param {string} id API ID
 * @returns {Promise}
 */
export function deleteThirdPartyApi(id) {
  return del(`/third-party/delete/${id}`)
}

/**
 * 查询API密钥列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getApiKeys(params) {
  return get('/third-party/call-logs', params)
}

/**
 * 创建API密钥
 * @param {Object} data 密钥信息
 * @returns {Promise}
 */
export function createApiKey(data) {
  return post('/third-party/create', data)
}

/**
 * 删除API密钥
 * @param {string} id 密钥ID
 * @returns {Promise}
 */
export function deleteApiKey(id) {
  return del(`/third-party/delete/${id}`)
}

/**
 * 查询调用日志
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getCallLogs(params) {
  return get('/third-party/call-logs/page', params)
}

/**
 * 检查API健康状态
 * @param {string} id API ID
 * @returns {Promise}
 */
export function checkApiHealth(id) {
  return get(`/third-party/health-check/${id}`)
}

// ============ SaaS套餐 API (M07) ============

/**
 * 查询套餐列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getPackages(params) {
  return get('/dict/package/page', params)
}

/**
 * 创建套餐
 * @param {Object} data 套餐信息
 * @returns {Promise}
 */
export function createPackage(data) {
  return post('/dict/package', data)
}

/**
 * 更新套餐
 * @param {string} id 套餐ID
 * @param {Object} data 套餐信息
 * @returns {Promise}
 */
export function updatePackage(id, data) {
  return put(`/dict/package/${id}`, data)
}

/**
 * 删除套餐
 * @param {string} id 套餐ID
 * @returns {Promise}
 */
export function deletePackage(id) {
  return del(`/dict/package/${id}`)
}

/**
 * 查询套餐功能列表
 * @param {string} packageId 套餐ID
 * @returns {Promise}
 */
export function getPackageFeatures(packageId) {
  return get(`/dict/package/${packageId}`)
}

/**
 * 订阅套餐
 * @param {Object} data 订阅信息（含 packageId）
 * @returns {Promise}
 */
export function subscribePackage(data) {
  return post('/dict/package/subscribe', data)
}

/**
 * 查询订阅列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getSubscriptions(params) {
  return get('/dict/package/subscriptions/page', params)
}

// ============ 营销活动 API (M08) ============

/**
 * 分页查询活动列表
 * @param {Object} params 分页参数和筛选条件
 * @returns {Promise}
 */
export function getActivities(params) {
  return get('/dict/activity/page', params)
}

/**
 * 创建活动
 * @param {Object} data 活动信息
 * @returns {Promise}
 */
export function createActivity(data) {
  return post('/dict/activity', data)
}

/**
 * 更新活动
 * @param {string} id 活动ID
 * @param {Object} data 活动信息
 * @returns {Promise}
 */
export function updateActivity(id, data) {
  return put(`/dict/activity/${id}`, data)
}

/**
 * 删除活动
 * @param {string} id 活动ID
 * @returns {Promise}
 */
export function deleteActivity(id) {
  return del(`/dict/activity/${id}`)
}

/**
 * 激活/上架活动
 * @param {string} id 活动ID
 * @returns {Promise}
 */
export function activateActivity(id) {
  return put(`/dict/activity/${id}`, { status: 1 })
}

// ============ 代码生成 API (M12) ============

/**
 * 查询代码模板列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getTemplates(params) {
  return get('/dict/codegen/template/page', params)
}

/**
 * 创建代码模板
 * @param {Object} data 模板信息
 * @returns {Promise}
 */
export function createTemplate(data) {
  return post('/dict/codegen/template', data)
}

/**
 * 更新代码模板
 * @param {string} id 模板ID
 * @param {Object} data 模板信息
 * @returns {Promise}
 */
export function updateTemplate(id, data) {
  return put(`/dict/codegen/template/${id}`, data)
}

/**
 * 删除代码模板
 * @param {string} id 模板ID
 * @returns {Promise}
 */
export function deleteTemplate(id) {
  return del(`/dict/codegen/template/${id}`)
}

/**
 * 执行代码生成
 * @param {Object} data 生成参数
 * @returns {Promise}
 */
export function generateCode(data) {
  return post('/dict/codegen/generate', data)
}

/**
 * 查询代码生成历史
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getGenHistory(params) {
  return get('/dict/codegen/history/page', params)
}

/**
 * 查询数据库表列表
 * @returns {Promise}
 */
export function listDbTables() {
  return get('/dict/codegen/tables')
}

/**
 * 查询表字段信息
 * @param {string} schema 数据库名
 * @param {string} tableName 表名
 * @returns {Promise}
 */
export function listDbColumns(schema, tableName) {
  return get('/dict/codegen/columns', { schema, tableName })
}

/**
 * 批量生成代码
 * @param {Object} data 生成参数
 * @returns {Promise}
 */
export function generateCodeBatch(data) {
  return post('/dict/codegen/generate/batch', data)
}

/**
 * 获取生成历史详情
 * @param {number} id 历史记录ID
 * @returns {Promise}
 */
export function getGenHistoryDetail(id) {
  return get(`/dict/codegen/history/${id}`)
}

// ============ 错误文档 API (M10) ============

/**
 * 查询错误分类列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getErrorCategories(params) {
  return get('/dict/error-doc/category/page', params)
}

/**
 * 创建错误分类
 * @param {Object} data 分类信息
 * @returns {Promise}
 */
export function createErrorCategory(data) {
  return post('/dict/error-doc/category', data)
}

/**
 * 更新错误分类
 * @param {string} id 分类ID
 * @param {Object} data 分类信息
 * @returns {Promise}
 */
export function updateErrorCategory(id, data) {
  return put(`/dict/error-doc/category/${id}`, data)
}

/**
 * 删除错误分类
 * @param {string} id 分类ID
 * @returns {Promise}
 */
export function deleteErrorCategory(id) {
  return del(`/dict/error-doc/category/${id}`)
}

/**
 * 查询错误解决方案列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getErrorSolutions(params) {
  return get('/dict/error-doc/solution/page', params)
}

/**
 * 创建错误解决方案
 * @param {Object} data 解决方案信息
 * @returns {Promise}
 */
export function createErrorSolution(data) {
  return post('/dict/error-doc/solution', data)
}

/**
 * 搜索错误（按错误码或关键词）
 * @param {Object} params 搜索参数
 * @returns {Promise}
 */
export function searchError(params) {
  return get('/dict/error-doc/search', params)
}
