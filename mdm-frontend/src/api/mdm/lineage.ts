import request from '@/utils/request'
import type { AjaxResult } from '@/types'

export interface LineageData {
  objectCode: string
  dataId: number
  dataCode: string
  source: { type: string; value: string; time: string }
  targets: Array<{ appName: string; sendTime: string; success: boolean }>
}

/** 查询数据血缘（来源 → 数据 → 去向） */
export function getLineage(objectCode: string, dataId: number): Promise<AjaxResult<LineageData>> {
  return request({
    url: '/mdm/lineage/' + objectCode + '/' + dataId,
    method: 'get'
  })
}