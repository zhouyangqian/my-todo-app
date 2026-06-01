import { get, post, put } from '@/utils/request'

// 熔断器管理
export function getCircuitBreakerStatus() {
  return get('/gateway/circuit-breakers/status')
}

export function resetCircuitBreaker(serviceId) {
  return post(`/gateway/circuit-breakers/${serviceId}/reset`)
}

export function getDegradationConfig() {
  return get('/gateway/degradation/config')
}

export function updateDegradationConfig(data) {
  return put('/gateway/degradation/config', data)
}
