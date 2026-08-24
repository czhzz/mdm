import request from '@/utils/request'
import type { AjaxResult, TableDataInfo, MdmApp, MdmDistribution, MdmDistributionRecord } from '@/types'

// ===== 应用凭证 =====

export function listApp(query?: Partial<MdmApp> & { pageNum?: number; pageSize?: number }): Promise<TableDataInfo<MdmApp[]>> {
  return request({
    url: '/mdm/distribution/app/list',
    method: 'get',
    params: query
  })
}

export function getApp(appId: number): Promise<AjaxResult<MdmApp>> {
  return request({
    url: '/mdm/distribution/app/' + appId,
    method: 'get'
  })
}

export function addApp(data: MdmApp): Promise<AjaxResult<MdmApp>> {
  return request({
    url: '/mdm/distribution/app',
    method: 'post',
    data
  })
}

export function editApp(data: MdmApp): Promise<AjaxResult> {
  return request({
    url: '/mdm/distribution/app',
    method: 'put',
    data
  })
}

export function delApp(appIds: number[] | string): Promise<AjaxResult> {
  return request({
    url: '/mdm/distribution/app/' + appIds,
    method: 'delete'
  })
}

/** 重置密钥（返回新 secret） */
export function resetSecret(appId: number): Promise<AjaxResult<string>> {
  return request({
    url: '/mdm/distribution/app/reset/' + appId,
    method: 'put'
  })
}

// ===== 分发配置 =====

export function listDist(query?: Partial<MdmDistribution> & { pageNum?: number; pageSize?: number }): Promise<TableDataInfo<MdmDistribution[]>> {
  return request({
    url: '/mdm/distribution/config/list',
    method: 'get',
    params: query
  })
}

export function getDist(distId: number): Promise<AjaxResult<MdmDistribution>> {
  return request({
    url: '/mdm/distribution/config/' + distId,
    method: 'get'
  })
}

export function addDist(data: MdmDistribution): Promise<AjaxResult> {
  return request({
    url: '/mdm/distribution/config',
    method: 'post',
    data
  })
}

export function editDist(data: MdmDistribution): Promise<AjaxResult> {
  return request({
    url: '/mdm/distribution/config',
    method: 'put',
    data
  })
}

export function delDist(distIds: number[] | string): Promise<AjaxResult> {
  return request({
    url: '/mdm/distribution/config/' + distIds,
    method: 'delete'
  })
}

// ===== 分发记录 =====

export function listRecord(query?: Partial<MdmDistributionRecord> & { pageNum?: number; pageSize?: number }): Promise<TableDataInfo<MdmDistributionRecord[]>> {
  return request({
    url: '/mdm/distribution/record/list',
    method: 'get',
    params: query
  })
}

/** 失败重推 */
export function retryRecord(recordId: number): Promise<AjaxResult> {
  return request({
    url: '/mdm/distribution/record/retry/' + recordId,
    method: 'put'
  })
}
// ===== 分发监控（1.1.0） =====

/** 获取 MQ 分发监控数据 */
export function getDistributionMonitor(): Promise<AjaxResult<Record<string, unknown>>> {
  return request({
    url: '/mdm/distribution/monitor',
    method: 'get'
  })
}
