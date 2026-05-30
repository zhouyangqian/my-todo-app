// api/gateway.js - 网关监控 API 接口模块
// 提供服务健康状态、断路器状态、缓存管理、金丝雀发布、监控指标、降级配置等接口

import { get, post, put, del } from '@/utils/request'

/**
 * 获取所有服务的健康状态
 */
export function getServiceStatuses() {
  return get('/gateway/service-statuses')
}

/**
 * 获取所有服务的断路器状态
 */
export function getCircuitBreakerStatus() {
  return get('/gateway/circuit-breaker-status')
}

/**
 * 获取缓存统计信息
 */
export function getCacheStats() {
  return get('/gateway/cache/stats')
}

/**
 * 清除所有缓存
 */
export function evictAllCache() {
  return del('/gateway/cache/evict')
}

/**
 * 获取API指标
 */
export function getMetrics() {
  return get('/gateway/metrics')
}

/**
 * 获取监控仪表盘数据
 */
export function getDashboardData() {
  return get('/gateway/dashboard')
}

/**
 * 获取最慢API
 */
export function getSlowApis(count = 10) {
  return get('/gateway/slow-apis', { count })
}

/**
 * 获取错误率最高API
 */
export function getErrorApis(count = 10) {
  return get('/gateway/error-apis', { count })
}

// ========== 熔断器管理 ==========

/**
 * 获取所有服务熔断状态
 */
export function getCircuitBreakerStatusV2() {
  return get('/gateway/circuit-breakers/status')
}

/**
 * 重置指定服务的熔断状态
 */
export function resetCircuitBreaker(serviceId) {
  return post(`/gateway/circuit-breakers/${serviceId}/reset`)
}

// ========== 降级配置管理 ==========

/**
 * 获取降级配置
 */
export function getDegradationConfig() {
  return get('/gateway/degradation/config')
}

/**
 * 更新降级配置
 * @param {Object} config - 包含 serviceId 和 fallbackResponse 的配置对象
 */
export function updateDegradationConfig(config) {
  return put('/gateway/degradation/config', config)
}
