// api/codegen.js - 代码生成接口模块
import { get, post, put, del } from '@/utils/request'

// 代码模板
export function getTemplatePage(params) { return get('/dict/codegen/template/page', params) }
export function getTemplate(id) { return get(`/dict/codegen/template/${id}`) }
export function createTemplate(data) { return post('/dict/codegen/template', data) }
export function updateTemplate(id, data) { return put(`/dict/codegen/template/${id}`, data) }
export function deleteTemplate(id) { return del(`/dict/codegen/template/${id}`) }

// 代码生成
export function generateCode(data) { return post('/dict/codegen/generate', data) }
export function getGenHistoryPage(params) { return get('/dict/codegen/history/page', params) }
