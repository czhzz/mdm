import request from '@/utils/request'
import type {
  AjaxResult,
  TableDataInfo,
  MdmObject,
  MdmCategory,
  MdmAttribute,
  MdmObjectMeta
} from '@/types'

// ===== 数据对象 =====

/** 查询对象列表 */
export function listObject(query?: Partial<MdmObject> & { pageNum?: number; pageSize?: number }): Promise<TableDataInfo<MdmObject[]>> {
  return request({
    url: '/mdm/model/object/list',
    method: 'get',
    params: query
  })
}

/** 查询对象详细 */
export function getObject(objectId: number): Promise<AjaxResult<MdmObject>> {
  return request({
    url: '/mdm/model/object/' + objectId,
    method: 'get'
  })
}

/** 查询对象模型元数据（对象+属性） */
export function getObjectMeta(objectId: number): Promise<AjaxResult<MdmObjectMeta>> {
  return request({
    url: '/mdm/model/object/meta/' + objectId,
    method: 'get'
  })
}

/** 新增对象 */
export function addObject(data: MdmObject): Promise<AjaxResult> {
  return request({
    url: '/mdm/model/object',
    method: 'post',
    data
  })
}

/** 修改对象 */
export function editObject(data: MdmObject): Promise<AjaxResult> {
  return request({
    url: '/mdm/model/object',
    method: 'put',
    data
  })
}

/** 发布对象 */
export function publishObject(objectId: number): Promise<AjaxResult> {
  return request({
    url: '/mdm/model/object/publish/' + objectId,
    method: 'put'
  })
}

/** 删除对象 */
export function delObject(objectIds: number[] | string): Promise<AjaxResult> {
  return request({
    url: '/mdm/model/object/' + objectIds,
    method: 'delete'
  })
}

// ===== 数据分类 =====

/** 查询分类树 */
export function listCategory(query?: Partial<MdmCategory>): Promise<AjaxResult<MdmCategory[]>> {
  return request({
    url: '/mdm/model/category/list',
    method: 'get',
    params: query
  })
}

/** 新增分类 */
export function addCategory(data: MdmCategory): Promise<AjaxResult> {
  return request({
    url: '/mdm/model/category',
    method: 'post',
    data
  })
}

/** 修改分类 */
export function editCategory(data: MdmCategory): Promise<AjaxResult> {
  return request({
    url: '/mdm/model/category',
    method: 'put',
    data
  })
}

/** 删除分类 */
export function delCategory(categoryId: number): Promise<AjaxResult> {
  return request({
    url: '/mdm/model/category/' + categoryId,
    method: 'delete'
  })
}

// ===== 数据属性 =====

/** 查询属性列表 */
export function listAttribute(query?: Partial<MdmAttribute>): Promise<AjaxResult<MdmAttribute[]>> {
  return request({
    url: '/mdm/model/attribute/list',
    method: 'get',
    params: query
  })
}

/** 新增属性 */
export function addAttribute(data: MdmAttribute): Promise<AjaxResult> {
  return request({
    url: '/mdm/model/attribute',
    method: 'post',
    data
  })
}

/** 修改属性 */
export function editAttribute(data: MdmAttribute): Promise<AjaxResult> {
  return request({
    url: '/mdm/model/attribute',
    method: 'put',
    data
  })
}

/** 删除属性 */
export function delAttribute(attrIds: number[] | string): Promise<AjaxResult> {
  return request({
    url: '/mdm/model/attribute/' + attrIds,
    method: 'delete'
  })
}
