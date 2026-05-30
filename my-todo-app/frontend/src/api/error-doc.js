// api/error-doc.js - 错误文档接口模块
import { get, post, put, del } from '@/utils/request'

// 错误分类
export function getErrorCategoryPage(params) { return get('/dict/error-doc/category/page', params) }
export function getErrorCategory(id) { return get(`/dict/error-doc/category/${id}`) }
export function createErrorCategory(data) { return post('/dict/error-doc/category', data) }
export function updateErrorCategory(id, data) { return put(`/dict/error-doc/category/${id}`, data) }
export function deleteErrorCategory(id) { return del(`/dict/error-doc/category/${id}`) }

// 错误解决方案
export function getErrorSolutionPage(params) { return get('/dict/error-doc/solution/page', params) }
export function createErrorSolution(data) { return post('/dict/error-doc/solution', data) }
export function updateErrorSolution(id, data) { return put(`/dict/error-doc/solution/${id}`, data) }
export function deleteErrorSolution(id) { return del(`/dict/error-doc/solution/${id}`) }
