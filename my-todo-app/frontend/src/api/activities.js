// api/activities.js - 活动管理接口模块
import { get, post, put, del } from '@/utils/request'

export function getActivityPage(params) { return get('/dict/activity/page', params) }
export function getActivity(id) { return get(`/dict/activity/${id}`) }
export function createActivity(data) { return post('/dict/activity', data) }
export function updateActivity(id, data) { return put(`/dict/activity/${id}`, data) }
export function deleteActivity(id) { return del(`/dict/activity/${id}`) }
