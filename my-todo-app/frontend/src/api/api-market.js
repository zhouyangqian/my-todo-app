// api/api-market.js - API市场接口模块
import { get, post, put, del } from '@/utils/request'

// API定义
export function getApiDefinitionPage(params) { return get('/api-market/definitions/page', params) }
export function getApiDefinition(id) { return get(`/api-market/definitions/${id}`) }
export function createApiDefinition(data) { return post('/api-market/definitions/create', data) }
export function updateApiDefinition(id, data) { return put(`/api-market/definitions/update/${id}`, data) }
export function deleteApiDefinition(id) { return del(`/api-market/definitions/delete/${id}`) }

// API订阅
export function getApiSubscriptionPage(params) { return get('/api-market/subscriptions/page', params) }
export function subscribeApi(apiId, subscriberName, callLimit) {
  return post('/api-market/subscriptions/subscribe', null, { params: { apiId, subscriberName, callLimit } })
}
export function unsubscribeApi(id) { return del(`/api-market/subscriptions/${id}`) }

// 使用记录
export function getUsageRecordPage(params) { return get('/api-market/usage/records/page', params) }
export function getUsageStats(params) { return get('/api-market/usage/stats', params) }
