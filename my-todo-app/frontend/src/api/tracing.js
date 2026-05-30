// api/tracing.js - 分布式追踪配置接口模块
import { get, post, put, del } from '@/utils/request'

// 追踪配置
export function getTraceConfigPage(params) { return get('/trace/configs/page', params) }
export function createTraceConfig(data) { return post('/trace/configs/create', data) }
export function updateTraceConfig(id, data) { return put(`/trace/configs/update/${id}`, data) }
export function deleteTraceConfig(id) { return del(`/trace/configs/delete/${id}`) }

// 追踪告警
export function getTraceAlertPage(params) { return get('/trace/alerts/page', params) }
export function createTraceAlert(data) { return post('/trace/alerts/create', data) }
export function updateTraceAlert(id, data) { return put(`/trace/alerts/update/${id}`, data) }
export function deleteTraceAlert(id) { return del(`/trace/alerts/delete/${id}`) }
