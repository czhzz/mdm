import request from '@/utils/request'
import type { AjaxResult, TableDataInfo, MdmAuditFlow, MdmAuditTask } from '@/types'

// ===== 审核流程配置 =====

/** 按对象查询审核流程配置 */
export function getAuditFlow(objectId: number): Promise<AjaxResult<MdmAuditFlow>> {
  return request({
    url: '/mdm/audit/flow/' + objectId,
    method: 'get'
  })
}

/** 保存审核流程配置（启用/禁用 + 审核角色） */
export function saveAuditFlow(data: MdmAuditFlow): Promise<AjaxResult> {
  return request({
    url: '/mdm/audit/flow',
    method: 'put',
    data
  })
}

// ===== 审核任务 =====

/** 查询审核任务列表 */
export function listAuditTask(
  query?: Partial<MdmAuditTask> & { pageNum?: number; pageSize?: number }
): Promise<TableDataInfo<MdmAuditTask[]>> {
  return request({
    url: '/mdm/audit/task/list',
    method: 'get',
    params: query
  })
}

/** 提交审核 */
export function submitAudit(data: Record<string, any>): Promise<AjaxResult> {
  return request({
    url: '/mdm/audit/task/submit',
    method: 'post',
    data
  })
}

/** 审核通过 */
export function approveAudit(taskId: number): Promise<AjaxResult> {
  return request({
    url: '/mdm/audit/task/approve/' + taskId,
    method: 'put'
  })
}

/** 审核驳回 */
export function rejectAudit(taskId: number, reason: string): Promise<AjaxResult> {
  return request({
    url: '/mdm/audit/task/reject/' + taskId,
    method: 'put',
    data: { reason }
  })
}