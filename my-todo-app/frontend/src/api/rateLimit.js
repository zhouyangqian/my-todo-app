import { get, post, put, del } from '@/utils/request'

// 限流配置管理
export function getRateLimitConfig() {
  return get('/gateway/rate-limits')
}

export function updateRateLimitConfig(data) {
  return put('/gateway/rate-limits', data)
}
