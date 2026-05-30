// api/activities.js - 活动管理接口模块
import { get, post, put, del } from '@/utils/request'

export function getActivityPage(params) { return get('/activities/page', params) }
export function getActivity(id) { return get(`/activities/${id}`) }
export function createActivity(data) { return post('/activities/create', data) }
export function updateActivity(id, data) { return put(`/activities/update/${id}`, data) }
export function deleteActivity(id) { return del(`/activities/delete/${id}`) }

// 活动参与管理
export function getParticipations(activityId) { return get(`/activities/${activityId}/participations`) }
export function joinActivity(activityId, tenantId) { return post(`/activities/${activityId}/join`, { tenantId }) }
export function cancelParticipation(participationId) { return del(`/activities/participations/${participationId}/cancel`) }
