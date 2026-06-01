import { get, del } from '@/utils/request'

// 缓存管理
export function getCacheStats() {
  return get('/gateway/cache/stats')
}

export function evictCache(key) {
  return del(`/gateway/cache/evict/${key}`)
}

export function evictAllCache() {
  return del('/gateway/cache/evict')
}
