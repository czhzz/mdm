import request from '@/utils/request'
import type { AjaxResult, TableDataInfo, MdmQualityRule, MdmQualityIssue } from '@/types'

// ===== 校验规则 =====

/** 查询校验规则列表 */
export function listRule(query?: Partial<MdmQualityRule> & { pageNum?: number; pageSize?: number }): Promise<TableDataInfo<MdmQualityRule[]>> {
  return request({
    url: '/mdm/quality/rule/list',
    method: 'get',
    params: query
  })
}

/** 新增校验规则 */
export function addRule(data: MdmQualityRule): Promise<AjaxResult> {
  return request({
    url: '/mdm/quality/rule',
    method: 'post',
    data
  })
}

/** 修改校验规则 */
export function editRule(data: MdmQualityRule): Promise<AjaxResult> {
  return request({
    url: '/mdm/quality/rule',
    method: 'put',
    data
  })
}

/** 删除校验规则 */
export function delRule(ruleIds: number[] | string): Promise<AjaxResult> {
  return request({
    url: '/mdm/quality/rule/' + ruleIds,
    method: 'delete'
  })
}

// ===== 重复检测 =====

/** 执行重复检测 */
export function duplicateCheck(objectCode: string, fields: string[]): Promise<AjaxResult<Record<string, any>[]>> {
  return request({
    url: '/mdm/quality/duplicate',
    method: 'post',
    data: { objectCode, fields }
  })
}

// ===== 质量台账 =====

/** 查询质量台账列表 */
export function listIssue(query?: Partial<MdmQualityIssue> & { pageNum?: number; pageSize?: number }): Promise<TableDataInfo<MdmQualityIssue[]>> {
  return request({
    url: '/mdm/quality/issue/list',
    method: 'get',
    params: query
  })
}

/** 处理质量问题 */
export function handleIssue(data: MdmQualityIssue): Promise<AjaxResult> {
  return request({
    url: '/mdm/quality/issue/handle',
    method: 'put',
    data
  })
}

// ===== 质量大屏（1.1.0） =====

/** 获取质量大屏聚合数据 */
export function getQualityDashboard(): Promise<AjaxResult<Record<string, unknown>>> {
  return request({
    url: '/mdm/quality/dashboard',
    method: 'get'
  })
}
