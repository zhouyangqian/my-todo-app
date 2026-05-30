// api/third-party.js - 第三方API接口模块
import { get, post, put, del } from '@/utils/request'

export function getThirdPartyApiPage(params) { return get('/third-party/page', params) }
export function getThirdPartyApi(id) { return get(`/third-party/${id}`) }
export function createThirdPartyApi(data) { return post('/third-party/create', data) }
export function updateThirdPartyApi(id, data) { return put(`/third-party/update/${id}`, data) }
export function deleteThirdPartyApi(id) { return del(`/third-party/delete/${id}`) }
export function getCallLogPage(params) { return get('/third-party/call-logs/page', params) }
export function healthCheckApi(id) { return get(`/third-party/health-check/${id}`) }
