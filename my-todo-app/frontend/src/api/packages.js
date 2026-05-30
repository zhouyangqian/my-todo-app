// api/packages.js - 套餐管理接口模块
import { get, post, put, del } from '@/utils/request'

// 套餐管理
export function getPackagePage(params) { return get('/dict/package/page', params) }
export function getPackage(id) { return get(`/dict/package/${id}`) }
export function createPackage(data) { return post('/dict/package', data) }
export function updatePackage(id, data) { return put(`/dict/package/${id}`, data) }
export function deletePackage(id) { return del(`/dict/package/${id}`) }

// 租户订阅
export function subscribePackage(packageId, startDate, endDate) {
  return post('/dict/package/subscribe', null, { params: { packageId, startDate, endDate } })
}
export function getSubscriptionPage(params) { return get('/dict/package/subscriptions/page', params) }
