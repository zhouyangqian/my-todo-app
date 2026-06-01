import { get } from '@/utils/request'

// 监控指标
export function getMetrics() {
  return get('/gateway/metrics')
}

export function getDashboard() {
  return get('/gateway/dashboard')
}

export function getTopSlowApis(params) {
  return get('/gateway/slow-apis', params)
}

export function getTopErrorApis(params) {
  return get('/gateway/error-apis', params)
}
