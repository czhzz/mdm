import request from '@/utils/request'
import type { AjaxResult, TableDataInfo } from '@/types'

/** 查询主数据列表（动态属性为查询条件） */
export function listData(objectCode: string, query?: Record<string, any>): Promise<TableDataInfo<Record<string, any>[]>> {
  return request({
    url: '/mdm/data/' + objectCode + '/list',
    method: 'get',
    params: query
  })
}

/** 查询主数据详情 */
export function getData(objectCode: string, id: number): Promise<AjaxResult<Record<string, any>>> {
  return request({
    url: '/mdm/data/' + objectCode + '/' + id,
    method: 'get'
  })
}

/** 新增主数据 */
export function addData(objectCode: string, data: Record<string, any>): Promise<AjaxResult> {
  return request({
    url: '/mdm/data/' + objectCode,
    method: 'post',
    data
  })
}

/** 修改主数据 */
export function editData(objectCode: string, id: number, data: Record<string, any>): Promise<AjaxResult> {
  return request({
    url: '/mdm/data/' + objectCode + '/' + id,
    method: 'put',
    data
  })
}

/** 删除主数据 */
export function delData(objectCode: string, ids: number[] | string): Promise<AjaxResult> {
  return request({
    url: '/mdm/data/' + objectCode + '/' + ids,
    method: 'delete'
  })
}

/** 更新主数据生命周期状态 */
export function updateDataStatus(objectCode: string, id: number, status: string): Promise<AjaxResult> {
  return request({
    url: '/mdm/data/' + objectCode + '/' + id + '/status',
    method: 'put',
    data: { status }
  })
}
