import request from '@/utils/request'
import type { AjaxResult, TableDataInfo } from '@/types'

// ===== 流程定义 =====

/** 查询流程定义列表 */
export function listProcessDefinitions(): Promise<AjaxResult<Array<{
  id: string; key: string; name: string; version: number; deploymentId: string; suspended: boolean; resourceName: string
}>>> {
  return request({ url: '/mdm/audit/flowable/definitions', method: 'get' })
}

/** 部署流程 */
export function deployProcess(name: string, bpmnXml: string): Promise<AjaxResult<string>> {
  return request({ url: '/mdm/audit/flowable/deploy', method: 'post', data: { name, bpmnXml } })
}

/** 删除流程定义 */
export function deleteProcessDefinition(deploymentId: string): Promise<AjaxResult> {
  return request({ url: '/mdm/audit/flowable/definition/' + deploymentId, method: 'delete' })
}

/** 挂起流程定义 */
export function suspendProcessDefinition(definitionId: string): Promise<AjaxResult> {
  return request({ url: '/mdm/audit/flowable/definition/' + definitionId + '/suspend', method: 'put' })
}

/** 激活流程定义 */
export function activateProcessDefinition(definitionId: string): Promise<AjaxResult> {
  return request({ url: '/mdm/audit/flowable/definition/' + definitionId + '/activate', method: 'put' })
}

// ===== 审核任务 =====

/** 查询待办 */
export function listTodoTasks(query?: { assignee?: string; pageNum?: number; pageSize?: number }): Promise<TableDataInfo<Array<Record<string, unknown>>>> {
  return request({ url: '/mdm/audit/flowable/todo', method: 'get', params: query })
}

/** 查询已办 */
export function listDoneTasks(query?: { assignee?: string; pageNum?: number; pageSize?: number }): Promise<TableDataInfo<Array<Record<string, unknown>>>> {
  return request({ url: '/mdm/audit/flowable/done', method: 'get', params: query })
}

/** 任务详情 */
export function getTaskDetail(taskId: string): Promise<AjaxResult<Record<string, unknown>>> {
  return request({ url: '/mdm/audit/flowable/task/' + taskId, method: 'get' })
}

/** 审批通过 */
export function approveTask(taskId: string, comment: string): Promise<AjaxResult> {
  return request({ url: '/mdm/audit/flowable/task/' + taskId + '/approve', method: 'put', data: { comment } })
}

/** 驳回 */
export function rejectTask(taskId: string, comment: string): Promise<AjaxResult> {
  return request({ url: '/mdm/audit/flowable/task/' + taskId + '/reject', method: 'put', data: { comment } })
}

/** 退回 */
export function backTask(taskId: string, comment: string): Promise<AjaxResult> {
  return request({ url: '/mdm/audit/flowable/task/' + taskId + '/back', method: 'put', data: { comment } })
}

/** 转办 */
export function delegateTask(taskId: string, targetUser: string): Promise<AjaxResult> {
  return request({ url: '/mdm/audit/flowable/task/' + taskId + '/delegate', method: 'put', data: { targetUser } })
}

/** 加签 */
export function addSignTask(taskId: string, targetUser: string): Promise<AjaxResult> {
  return request({ url: '/mdm/audit/flowable/task/' + taskId + '/add-sign', method: 'put', data: { targetUser } })
}

/** 流程进度 */
export function getProcessProgress(processInstanceId: string): Promise<AjaxResult<Array<{ id: string; name: string; assignee: string; status: string }>>> {
  return request({ url: '/mdm/audit/flowable/progress/' + processInstanceId, method: 'get' })
}