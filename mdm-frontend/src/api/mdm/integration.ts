import request from '@/utils/request'
import type {
  AjaxResult,
  TableDataInfo,
  MdmApp,
  MdmReceiveApi,
  MdmQueryApi,
  MdmDistributeApi,
  MdmDistributeLog,
  MdmReceiveLog,
  MdmQueryLog
} from '@/types'

// ===== 应用管理 =====

export function listApp(query?: Partial<MdmApp> & { pageNum?: number; pageSize?: number }): Promise<TableDataInfo<MdmApp[]>> {
  return request({
    url: '/mdm/integration/app/list',
    method: 'get',
    params: query
  })
}

export function getApp(appId: number): Promise<AjaxResult<MdmApp>> {
  return request({
    url: '/mdm/integration/app/' + appId,
    method: 'get'
  })
}

export function addApp(data: MdmApp): Promise<AjaxResult<MdmApp>> {
  return request({
    url: '/mdm/integration/app',
    method: 'post',
    data
  })
}

export function editApp(data: MdmApp): Promise<AjaxResult> {
  return request({
    url: '/mdm/integration/app',
    method: 'put',
    data
  })
}

export function delApp(appIds: number[] | string): Promise<AjaxResult> {
  return request({
    url: '/mdm/integration/app/' + appIds,
    method: 'delete'
  })
}

/** 重置密钥（返回新 secret，仅展示一次） */
export function resetSecret(appId: number): Promise<AjaxResult<string>> {
  return request({
    url: '/mdm/integration/app/reset/' + appId,
    method: 'put'
  })
}

// ===== 接收接口管理 =====

export function listReceive(query?: Partial<MdmReceiveApi> & { pageNum?: number; pageSize?: number }): Promise<TableDataInfo<MdmReceiveApi[]>> {
  return request({
    url: '/mdm/integration/receive/list',
    method: 'get',
    params: query
  })
}

export function getReceive(id: number): Promise<AjaxResult<MdmReceiveApi>> {
  return request({
    url: '/mdm/integration/receive/' + id,
    method: 'get'
  })
}

export function addReceive(data: MdmReceiveApi): Promise<AjaxResult> {
  return request({
    url: '/mdm/integration/receive',
    method: 'post',
    data
  })
}

export function editReceive(data: MdmReceiveApi): Promise<AjaxResult> {
  return request({
    url: '/mdm/integration/receive',
    method: 'put',
    data
  })
}

export function delReceive(ids: number[] | string): Promise<AjaxResult> {
  return request({
    url: '/mdm/integration/receive/' + ids,
    method: 'delete'
  })
}

// ===== 查询接口管理 =====

export function listQuery(query?: Partial<MdmQueryApi> & { pageNum?: number; pageSize?: number }): Promise<TableDataInfo<MdmQueryApi[]>> {
  return request({
    url: '/mdm/integration/query/list',
    method: 'get',
    params: query
  })
}

export function getQuery(id: number): Promise<AjaxResult<MdmQueryApi>> {
  return request({
    url: '/mdm/integration/query/' + id,
    method: 'get'
  })
}

export function addQuery(data: MdmQueryApi): Promise<AjaxResult> {
  return request({
    url: '/mdm/integration/query',
    method: 'post',
    data
  })
}

export function editQuery(data: MdmQueryApi): Promise<AjaxResult> {
  return request({
    url: '/mdm/integration/query',
    method: 'put',
    data
  })
}

export function delQuery(ids: number[] | string): Promise<AjaxResult> {
  return request({
    url: '/mdm/integration/query/' + ids,
    method: 'delete'
  })
}

// ===== 分发管理 =====

export function listDist(query?: Partial<MdmDistributeApi> & { pageNum?: number; pageSize?: number }): Promise<TableDataInfo<MdmDistributeApi[]>> {
  return request({
    url: '/mdm/integration/distribute/config/list',
    method: 'get',
    params: query
  })
}

export function getDist(distId: number): Promise<AjaxResult<MdmDistributeApi>> {
  return request({
    url: '/mdm/integration/distribute/config/' + distId,
    method: 'get'
  })
}

export function addDist(data: MdmDistributeApi): Promise<AjaxResult> {
  return request({
    url: '/mdm/integration/distribute/config',
    method: 'post',
    data
  })
}

export function editDist(data: MdmDistributeApi): Promise<AjaxResult> {
  return request({
    url: '/mdm/integration/distribute/config',
    method: 'put',
    data
  })
}

export function delDist(distIds: number[] | string): Promise<AjaxResult> {
  return request({
    url: '/mdm/integration/distribute/config/' + distIds,
    method: 'delete'
  })
}

/** 分发监控（MQ 通道状态/成功率） */
export function getDistributeMonitor(): Promise<AjaxResult<Record<string, unknown>>> {
  return request({
    url: '/mdm/integration/distribute/monitor',
    method: 'get'
  })
}

// ===== 集成日志 =====

export function listReceiveLog(query?: Partial<MdmReceiveLog> & { pageNum?: number; pageSize?: number }): Promise<TableDataInfo<MdmReceiveLog[]>> {
  return request({
    url: '/mdm/integration/log/receive/list',
    method: 'get',
    params: query
  })
}

export function listQueryLog(query?: Partial<MdmQueryLog> & { pageNum?: number; pageSize?: number }): Promise<TableDataInfo<MdmQueryLog[]>> {
  return request({
    url: '/mdm/integration/log/query/list',
    method: 'get',
    params: query
  })
}

export function listDistributeLog(query?: Partial<MdmDistributeLog> & { pageNum?: number; pageSize?: number }): Promise<TableDataInfo<MdmDistributeLog[]>> {
  return request({
    url: '/mdm/integration/log/distribute/list',
    method: 'get',
    params: query
  })
}

/** 分发日志失败重推 */
export function retryDistributeLog(recordId: number): Promise<AjaxResult> {
  return request({
    url: '/mdm/integration/log/distribute/retry/' + recordId,
    method: 'put'
  })
}

/** 手动清理日志（type: receive|query|distribute，删除截止时间前日志） */
export function cleanLog(type: string, beforeTime: string): Promise<AjaxResult> {
  return request({
    url: '/mdm/integration/log/clean',
    method: 'delete',
    params: { type, beforeTime }
  })
}
