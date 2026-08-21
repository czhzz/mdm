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

/** 主数据编码规则 */
export interface MdmCodeRule {
  ruleId?: number
  objectId?: number
  ruleName?: string
  resetType?: string
  codeField?: string
  status?: string
  segments?: MdmCodeRuleSegment[]
}

/** 主数据编码规则分段 */
export interface MdmCodeRuleSegment {
  segmentId?: number
  ruleId?: number
  segType?: string
  segValue?: string
  orderNum?: number
}

/** 数据质量校验规则 */
export interface MdmQualityRule {
  ruleId?: number
  objectId?: number
  targetType?: string
  targetValue?: string
  ruleType?: string
  ruleName?: string
  ruleExpr?: string
  ruleMsg?: string
  status?: string
}

/** 数据质量台账 */
export interface MdmQualityIssue {
  issueId?: number
  objectId?: number
  dataId?: number
  issueType?: string
  issueDesc?: string
  handleStatus?: string
  handleBy?: string
  handleTime?: string
}

/** 主数据审核任务 */
export interface MdmAuditTask {
  taskId?: number
  objectId?: number
  dataId?: number
  actionType?: string
  beforeData?: string
  afterData?: string
  status?: string
  rejectReason?: string
  submitBy?: string
  auditBy?: string
  auditTime?: string
  createTime?: string
}

/** 应用凭证 */
export interface MdmApp {
  appId?: number
  appName?: string
  appid?: string
  secret?: string
  enabled?: string
  remark?: string
  createTime?: string
}

/** 分发配置 */
export interface MdmDistribution {
  distId?: number
  appId?: number
  objectId?: number
  triggerType?: string
  endpointUrl?: string
  enabled?: string
  remark?: string
  appName?: string
  objectName?: string
}

/** 分发记录 */
export interface MdmDistributionRecord {
  recordId?: number
  appId?: number
  objectCode?: string
  dataId?: number
  actionType?: string
  endpointUrl?: string
  payload?: string
  status?: string
  errorMsg?: string
  sendTime?: string
  successTime?: string
  confirmTime?: string
  retryCount?: number
  appName?: string
  createTime?: string
}

/** 审核流程配置 */
export interface MdmAuditFlow {
  flowId?: number
  objectId?: number
  enabled?: string
  auditRole?: string
}
