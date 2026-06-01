import { get, put } from '@/utils/request'

// 灰度发布管理
export function getCanaryConfig() {
  return get('/gateway/routes')
}

export function updateCanaryConfig(data) {
  return put('/gateway/canary/config', data)
}

export function getCanaryStatus() {
  return get('/gateway/circuit-breaker-status')
}
