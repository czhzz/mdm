import request from '@/utils/request'
import type { AjaxResult } from '@/types'

export interface MdmTemplate {
  code: string
  name: string
  description: string
  icon: string
  objectCode: string
  objectName: string
  attributes: Array<{
    attrCode: string
    attrName: string
    dataType: string
    requiredFlag: string
    uniqueFlag: string
  }>
  codeRule?: {
    ruleName: string
    resetType: string
    codeField: string
    segments: Array<{ segType: string; segValue: string; orderNum: number }>
  }
}

/** 获取模板列表 */
export function listTemplates(): Promise<AjaxResult<MdmTemplate[]>> {
  return request({ url: '/mdm/template/list', method: 'get' })
}

/** 获取模板预览 */
export function previewTemplate(code: string): Promise<AjaxResult<MdmTemplate>> {
  return request({ url: '/mdm/template/preview/' + code, method: 'get' })
}

/** 一键创建 */
export function createFromTemplate(code: string): Promise<AjaxResult> {
  return request({ url: '/mdm/template/create/' + code, method: 'post' })
}