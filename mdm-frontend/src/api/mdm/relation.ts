import request from '@/utils/request'
import type { AjaxResult, TableDataInfo } from '@/types'

export interface MdmRelation {
  id?: number
  sourceObjectCode: string
  targetObjectCode: string
  relationType: string
  sourceFieldCode?: string
  cascadeRule?: string
  isBidirectional?: string
}

/** 查询关联关系列表 */
export function listRelation(query?: Partial<MdmRelation> & { pageNum?: number; pageSize?: number }): Promise<TableDataInfo<MdmRelation[]>> {
  return request({ url: '/mdm/relation/list', method: 'get', params: query })
}

/** 查询关联关系详情 */
export function getRelation(id: number): Promise<AjaxResult<MdmRelation>> {
  return request({ url: '/mdm/relation/' + id, method: 'get' })
}

/** 新增关联关系 */
export function addRelation(data: MdmRelation): Promise<AjaxResult> {
  return request({ url: '/mdm/relation', method: 'post', data })
}

/** 修改关联关系 */
export function editRelation(data: MdmRelation): Promise<AjaxResult> {
  return request({ url: '/mdm/relation', method: 'put', data })
}

/** 删除关联关系 */
export function delRelation(ids: string): Promise<AjaxResult> {
  return request({ url: '/mdm/relation/' + ids, method: 'delete' })
}