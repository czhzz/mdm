import request from '@/utils/request'
import type { AjaxResult, TableDataInfo } from '@/types'
import type { MdmCodeRule } from '@/types'

/** 查询编码规则列表 */
export function listRule(query?: Partial<MdmCodeRule> & { pageNum?: number; pageSize?: number }): Promise<TableDataInfo<MdmCodeRule[]>> {
  return request({
    url: '/mdm/coderule/rule/list',
    method: 'get',
    params: query
  })
}

/** 查询编码规则详细（含分段） */
export function getRule(ruleId: number): Promise<AjaxResult<MdmCodeRule>> {
  return request({
    url: '/mdm/coderule/rule/' + ruleId,
    method: 'get'
  })
}

/** 新增编码规则 */
export function addRule(data: MdmCodeRule): Promise<AjaxResult> {
  return request({
    url: '/mdm/coderule/rule',
    method: 'post',
    data
  })
}

/** 修改编码规则 */
export function editRule(data: MdmCodeRule): Promise<AjaxResult> {
  return request({
    url: '/mdm/coderule/rule',
    method: 'put',
    data
  })
}

/** 删除编码规则 */
export function delRule(ruleId: number): Promise<AjaxResult> {
  return request({
    url: '/mdm/coderule/rule/' + ruleId,
    method: 'delete'
  })
}

/** 预览示例编码 */
export function previewRule(data: MdmCodeRule): Promise<AjaxResult<string>> {
  return request({
    url: '/mdm/coderule/rule/preview',
    method: 'post',
    data
  })
}
