import { get } from '@/utils/request'

// 健康检查与服务发现
export function getGatewayHealth() {
  return get('/actuator/gateway-health')
}

export function getGatewayStatus() {
  return get('/actuator/gateway-status')
}

export function getServiceStatuses() {
  return get('/gateway/service-statuses')
}
