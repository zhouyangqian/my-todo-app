import { get, post, put, del } from '@/utils/request'

// 外部API管理
export function getExternalApis(params) {
  return get('/gateway/routes', params)
}

export function getExternalApiDetail(id) {
  return get(`/gateway/routes/${id}`)
}

export function testExternalApi(id) {
  return post(`/gateway/external/${id}/test`)
}
