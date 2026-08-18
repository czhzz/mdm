/** 主数据对象 */
export interface MdmObject {
  objectId?: number
  objectCode?: string
  objectName?: string
  categoryId?: number
  status?: string
  version?: string
  orderNum?: number
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  remark?: string
}

/** 主数据分类 */
export interface MdmCategory {
  categoryId?: number
  parentId?: number
  ancestors?: string
  categoryName?: string
  categoryCode?: string
  orderNum?: number
  status?: string
  children?: MdmCategory[]
}

/** 主数据属性 */
export interface MdmAttribute {
  attrId?: number
  objectId?: number
  attrCode?: string
  attrName?: string
  dataType?: string
  requiredFlag?: string
  uniqueFlag?: string
  primaryFlag?: string
  sourceType?: string
  dictType?: string
  minValue?: string
  maxValue?: string
  enumValues?: string
  defaultValue?: string
  orderNum?: number
  status?: string
}

/** 对象模型元数据 */
export interface MdmObjectMeta {
  object: MdmObject
  attributes: MdmAttribute[]
}
