SET NAMES utf8mb4;

-- ----------------------------
-- 主数据管理平台（MDM）初始化脚本
-- 执行位置：mdm-backend/sql/mdm/init.sql
-- 依赖：需先导入 sql/ry_20260417.sql（若依主库）
-- 说明：本脚本为第一期（1.0.0）元数据表，编码规则/标准/质量/分发等表按开发阶段追加
-- ----------------------------

-- ----------------------------
-- 1. 主数据分类表
-- ----------------------------
drop table if exists mdm_category;
create table mdm_category (
  category_id    bigint(20)      not null auto_increment    comment '分类ID',
  parent_id      bigint(20)      default 0                  comment '父分类ID',
  ancestors      varchar(500)    default ''                 comment '祖级列表',
  category_name  varchar(50)     not null                   comment '分类名称',
  category_code  varchar(50)     not null                   comment '分类编码',
  order_num      int(4)          default 0                  comment '显示顺序',
  status         char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by      varchar(64)     default ''                 comment '创建者',
  create_time    datetime                                   comment '创建时间',
  update_by      varchar(64)     default ''                 comment '更新者',
  update_time    datetime                                   comment '更新时间',
  remark         varchar(500)    default null               comment '备注',
  primary key (category_id),
  unique key uk_category_code (category_code)
) engine=innodb auto_increment=1 comment = '主数据分类表';

-- ----------------------------
-- 2. 主数据对象表
-- ----------------------------
drop table if exists mdm_object;
create table mdm_object (
  object_id      bigint(20)      not null auto_increment    comment '对象ID',
  object_code    varchar(50)     not null                   comment '对象编码',
  object_name    varchar(100)    not null                   comment '对象名称',
  category_id    bigint(20)      default 0                  comment '所属分类ID',
  status         char(1)         default '0'                comment '状态（0未发布 1已发布 2停用）',
  version        varchar(20)     default '1.0'              comment '模型版本号',
  order_num      int(4)          default 0                  comment '显示顺序',
  create_by      varchar(64)     default ''                 comment '创建者',
  create_time    datetime                                   comment '创建时间',
  update_by      varchar(64)     default ''                 comment '更新者',
  update_time    datetime                                   comment '更新时间',
  remark         varchar(500)    default null               comment '备注',
  primary key (object_id),
  unique key uk_object_code (object_code)
) engine=innodb auto_increment=1 comment = '主数据对象表';

-- ----------------------------
-- 3. 主数据属性表
-- ----------------------------
drop table if exists mdm_attribute;
create table mdm_attribute (
  attr_id        bigint(20)      not null auto_increment    comment '属性ID',
  object_id      bigint(20)      not null                   comment '所属对象ID',
  attr_code      varchar(50)     not null                   comment '属性编码',
  attr_name      varchar(100)    not null                   comment '属性名称',
  data_type      varchar(20)     default 'text'             comment '数据类型（text/number/date/datetime/dict/enum/boolean）',
  required_flag  char(1)         default 'N'                comment '是否必填（Y是 N否）',
  unique_flag    char(1)         default 'N'                comment '是否唯一（Y是 N否）',
  primary_flag   char(1)         default 'N'                comment '是否主属性（Y是 N否）',
  source_type    varchar(20)     default 'input'            comment '数据源类型（input/dict/enum/range）',
  dict_type      varchar(100)    default null               comment '关联字典类型',
  min_value      varchar(50)     default null               comment '最小值',
  max_value      varchar(50)     default null               comment '最大值',
  enum_values    varchar(500)    default null               comment '枚举值（逗号分隔）',
  default_value  varchar(255)    default null               comment '默认值',
  order_num      int(4)          default 0                  comment '显示顺序',
  status         char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by      varchar(64)     default ''                 comment '创建者',
  create_time    datetime                                   comment '创建时间',
  update_by      varchar(64)     default ''                 comment '更新者',
  update_time    datetime                                   comment '更新时间',
  remark         varchar(500)    default null               comment '备注',
  primary key (attr_id),
  unique key uk_object_attr (object_id, attr_code),
  key idx_object_id (object_id)
) engine=innodb auto_increment=1 comment = '主数据属性表';

-- ----------------------------
-- 3.1 主数据编码规则表
-- ----------------------------
drop table if exists mdm_code_rule_segment;
drop table if exists mdm_code_rule;
create table mdm_code_rule (
  rule_id      bigint(20)      not null auto_increment    comment '规则ID',
  object_id    bigint(20)      not null                   comment '对象ID',
  rule_name    varchar(100)    not null                   comment '规则名称',
  reset_type   varchar(10)     default 'NONE'             comment '流水重置周期（NONE/DAY/MONTH/YEAR）',
  code_field   varchar(50)     default null               comment '编码回填字段（对象属性编码）',
  status       char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by    varchar(64)     default ''                 comment '创建者',
  create_time  datetime                                   comment '创建时间',
  update_by    varchar(64)     default ''                 comment '更新者',
  update_time  datetime                                   comment '更新时间',
  remark       varchar(500)    default null               comment '备注',
  primary key (rule_id),
  unique key uk_object_rule (object_id)
) engine=innodb auto_increment=1 comment = '主数据编码规则表';

drop table if exists mdm_code_rule_segment;
create table mdm_code_rule_segment (
  segment_id   bigint(20)      not null auto_increment    comment '分段ID',
  rule_id      bigint(20)      not null                   comment '规则ID',
  seg_type     varchar(10)     not null                   comment '分段类型（CONSTANT常量/DATE日期/SEQUENCE流水/ATTRIBUTE属性值）',
  seg_value    varchar(100)    default ''                 comment '分段值（常量值/日期格式/流水位数/属性编码）',
  order_num    int(4)          default 0                  comment '显示顺序',
  primary key (segment_id),
  key idx_rule_id (rule_id)
) engine=innodb auto_increment=1 comment = '主数据编码规则分段表';

-- ----------------------------
-- 4. 菜单与权限
-- ----------------------------
-- 清理已存在的 mdm 菜单（保证脚本可重复执行）
delete from sys_role_menu where menu_id between 2000 and 2045;
delete from sys_menu where menu_id between 2000 and 2045;
-- 一级目录
insert into sys_menu values('2000', '主数据管理', '0', '10', 'mdm',            null,                    '', '', 1, 0, 'M', '0', '0', '',                    'tree-table', 'admin', sysdate(), '', null, '主数据管理目录');
-- 二级菜单
insert into sys_menu values('2001', '模型管理',     '2000', '1', 'model',       'mdm/model/index',       '', '', 1, 0, 'C', '0', '0', 'mdm:model:list',        'example',     'admin', sysdate(), '', null, '模型管理菜单');
insert into sys_menu values('2002', '编码规则',     '2000', '2', 'coderule',    'mdm/coderule/index',    '', '', 1, 0, 'C', '0', '0', 'mdm:coderule:list',     'example',     'admin', sysdate(), '', null, '编码规则菜单');
insert into sys_menu values('2003', '数据标准',     '2000', '3', 'standard',    'mdm/standard/index',    '', '', 1, 0, 'C', '0', '0', 'mdm:standard:list',     'example',     'admin', sysdate(), '', null, '数据标准菜单');
insert into sys_menu values('2004', '数据维护',     '2000', '4', 'maintenance', 'mdm/maintenance/index', '', '', 1, 0, 'C', '0', '0', 'mdm:maintenance:list',  'example',     'admin', sysdate(), '', null, '数据维护菜单');
insert into sys_menu values('2005', '数据质量',     '2000', '5', 'quality',     'mdm/quality/index',     '', '', 1, 0, 'C', '0', '0', 'mdm:quality:list',      'example',     'admin', sysdate(), '', null, '数据质量菜单');
insert into sys_menu values('2006', '数据分发',     '2000', '6', 'distribution','mdm/distribution/index','', '', 1, 0, 'C', '0', '0', 'mdm:distribution:list', 'example',     'admin', sysdate(), '', null, '数据分发菜单');
-- 模型管理按钮权限
insert into sys_menu values('2007', '对象查询', '2001', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:model:query',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2008', '对象新增', '2001', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:model:add',      '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2009', '对象修改', '2001', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:model:edit',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2010', '对象删除', '2001', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:model:remove',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2011', '分类查询', '2001', '5', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:category:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2012', '分类新增', '2001', '6', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:category:add',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2013', '分类修改', '2001', '7', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:category:edit',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2014', '分类删除', '2001', '8', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:category:remove','#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2015', '属性查询', '2001', '9', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:attribute:query','#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2016', '属性新增', '2001', '10', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:attribute:add',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2017', '属性修改', '2001', '11', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:attribute:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2018', '属性删除', '2001', '12', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:attribute:remove','#', 'admin', sysdate(), '', null, '');
-- 数据维护按钮权限
insert into sys_menu values('2019', '数据查询', '2004', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:maintenance:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2020', '数据新增', '2004', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:maintenance:add',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2021', '数据修改', '2004', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:maintenance:edit',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2022', '数据删除', '2004', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:maintenance:remove','#', 'admin', sysdate(), '', null, '');
-- 编码规则按钮权限
insert into sys_menu values('2023', '规则查询', '2002', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:coderule:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2024', '规则新增', '2002', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:coderule:add',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2025', '规则修改', '2002', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:coderule:edit',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2026', '规则删除', '2002', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:coderule:remove','#', 'admin', sysdate(), '', null, '');
-- 审核菜单（复用数据维护按钮权限）
insert into sys_menu values('2027', '审核', '2000', '7', 'audit', 'mdm/audit/index', '', '', 1, 0, 'C', '0', '0', 'mdm:maintenance:list', 'example', 'admin', sysdate(), '', null, '审核任务菜单');
-- 数据质量按钮权限
insert into sys_menu values('2028', '质量查询', '2005', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:quality:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2029', '质量新增', '2005', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:quality:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2030', '质量修改', '2005', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:quality:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2031', '质量删除', '2005', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:quality:remove', '#', 'admin', sysdate(), '', null, '');
-- 数据分发按钮权限
insert into sys_menu values('2032', '分发查询', '2006', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:distribution:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2033', '分发新增', '2006', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:distribution:add',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2034', '分发修改', '2006', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:distribution:edit',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2035', '分发删除', '2006', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:distribution:remove','#', 'admin', sysdate(), '', null, '');
-- 1.1.0 新增菜单
insert into sys_menu values('2036', '关系管理',   '2000', '7', 'relation',      'mdm/relation/index',         '', '', 1, 0, 'C', '0', '0', 'mdm:relation:list',      'link',        'admin', sysdate(), '', null, '关系管理菜单');
insert into sys_menu values('2037', '模板库',     '2000', '8', 'template',      'mdm/template/index',         '', '', 1, 0, 'C', '0', '0', 'mdm:template:list',      'component',    'admin', sysdate(), '', null, '对象模板库菜单');
insert into sys_menu values('2038', '质量大屏',   '2000', '9', 'quality-dashboard', 'mdm/quality-dashboard/index', '', '', 1, 0, 'C', '0', '0', 'mdm:quality:list',   'dashboard',    'admin', sysdate(), '', null, '数据质量大屏');
insert into sys_menu values('2039', '流程管理',   '2000', '10', 'audit-process', 'mdm/audit/process-list',     '', '', 1, 0, 'C', '0', '0', 'mdm:maintenance:list', 'guide',        'admin', sysdate(), '', null, '审核流程管理');
insert into sys_menu values('2040', '流程设计器', '2039', '1', 'audit-designer', 'mdm/audit/designer/index',   '', '', 1, 0, 'C', '0', '0', 'mdm:maintenance:list', 'edit',         'admin', sysdate(), '', null, 'BPMN 流程设计器');
insert into sys_menu values('2041', '分发监控',   '2006', '5', 'dist-monitor',   'mdm/distribution/monitor',   '', '', 1, 0, 'C', '0', '0', 'mdm:distribution:list', 'monitor',      'admin', sysdate(), '', null, '分发监控面板');
-- 1.1.0 按钮权限
insert into sys_menu values('2042', '关系查询', '2036', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:relation:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2043', '关系新增', '2036', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:relation:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2044', '关系修改', '2036', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:relation:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2045', '关系删除', '2036', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'mdm:relation:remove', '#', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 5. 种子字典
-- ----------------------------
-- 清理已存在的 mdm 种子字典（保证脚本可重复执行）
delete from sys_dict_data where dict_code between 2000 and 2199;
delete from sys_dict_type where dict_id = 200;
-- 字典类型：主数据属性数据类型
insert into sys_dict_type values(200, '主数据属性数据类型', 'mdm_data_type', '0', 'admin', sysdate(), 'admin', sysdate(), '主数据模型属性数据类型');
insert into sys_dict_data values(2000, 1, '文本',   'text',     'mdm_data_type', '', 'default', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), null);
insert into sys_dict_data values(2001, 2, '数字',   'number',   'mdm_data_type', '', 'default', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), null);
insert into sys_dict_data values(2002, 3, '日期',   'date',     'mdm_data_type', '', 'default', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), null);
insert into sys_dict_data values(2003, 4, '日期时间', 'datetime', 'mdm_data_type', '', 'default', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), null);
insert into sys_dict_data values(2004, 5, '字典',   'dict',     'mdm_data_type', '', 'default', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), null);
insert into sys_dict_data values(2005, 6, '枚举',   'enum',     'mdm_data_type', '', 'default', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), null);
insert into sys_dict_data values(2006, 7, '布尔',   'boolean',  'mdm_data_type', '', 'default', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), null);

-- ----------------------------
-- 3.2 数据质量校验规则表
-- ----------------------------
drop table if exists mdm_quality_rule;
create table mdm_quality_rule (
  rule_id      bigint(20)      not null auto_increment    comment '规则ID',
  object_id    bigint(20)      not null                   comment '对象ID',
  target_type  varchar(10)     not null default 'ATTRIBUTE' comment '作用目标（OBJECT对象级/ATTRIBUTE属性级）',
  target_value varchar(50)     default null               comment '目标属性编码（属性级时必填）',
  rule_type    varchar(10)     not null                   comment '规则类型（REQUIRED必填/REGEX正则/UNIQUE唯一/RANGE范围）',
  rule_name    varchar(100)    default null               comment '规则名称',
  rule_expr    varchar(500)    default null               comment '规则表达式（正则/范围等）',
  rule_msg     varchar(200)    default null               comment '违规提示信息',
  status       char(1)         default '0'                comment '状态（0启用 1停用）',
  create_by    varchar(64)     default ''                 comment '创建者',
  create_time  datetime                                   comment '创建时间',
  update_by    varchar(64)     default ''                 comment '更新者',
  update_time  datetime                                   comment '更新时间',
  remark       varchar(500)    default null               comment '备注',
  primary key (rule_id),
  key idx_object_id (object_id)
) engine=innodb auto_increment=1 comment = '数据质量校验规则表';

-- ----------------------------
-- 3.3 数据质量台账表
-- ----------------------------
drop table if exists mdm_quality_issue;
create table mdm_quality_issue (
  issue_id     bigint(20)      not null auto_increment    comment '问题ID',
  object_id    bigint(20)      not null                   comment '对象ID',
  data_id      bigint(20)      not null                   comment '数据ID',
  issue_type   varchar(20)     not null                   comment '问题类型（VALIDATE校验失败/DUPLICATE重复/MISSING缺失）',
  issue_desc   varchar(500)    default null               comment '问题描述',
  handle_status char(1)        default '0'                comment '处理状态（0未处理 1已处理 2忽略）',
  handle_by    varchar(64)     default null               comment '处理人',
  handle_time  datetime                                   comment '处理时间',
  create_by    varchar(64)     default ''                 comment '创建者',
  create_time  datetime                                   comment '创建时间',
  update_by    varchar(64)     default ''                 comment '更新者',
  update_time  datetime                                   comment '更新时间',
  remark       varchar(500)    default null               comment '备注',
  primary key (issue_id),
  key idx_object_data (object_id, data_id)
) engine=innodb auto_increment=1 comment = '数据质量台账表';

-- ----------------------------
-- 3.4 审核流程配置表
-- ----------------------------
drop table if exists mdm_audit_flow;
create table mdm_audit_flow (
  flow_id      bigint(20)      not null auto_increment    comment '流程ID',
  object_id    bigint(20)      not null                   comment '对象ID',
  enabled      char(1)         default '0'                comment '是否启用（0禁用 1启用）',
  audit_role   varchar(100)    default null               comment '审核角色标识',
  create_by    varchar(64)     default ''                 comment '创建者',
  create_time  datetime                                   comment '创建时间',
  update_by    varchar(64)     default ''                 comment '更新者',
  update_time  datetime                                   comment '更新时间',
  remark       varchar(500)    default null               comment '备注',
  primary key (flow_id),
  unique key uk_object_flow (object_id)
) engine=innodb auto_increment=1 comment = '主数据审核流程配置表';

-- ----------------------------
-- 3.5 审核任务表
-- ----------------------------
drop table if exists mdm_audit_task;
create table mdm_audit_task (
  task_id      bigint(20)      not null auto_increment    comment '任务ID',
  object_id    bigint(20)      not null                   comment '对象ID',
  data_id      bigint(20)      not null                   comment '数据ID',
  action_type  varchar(10)     not null                   comment '操作类型（INSERT新增/UPDATE修改）',
  before_data  text                                       comment '变更前快照(JSON)',
  after_data   text                                       comment '变更后快照(JSON)',
  status       char(1)         default '0'                comment '状态（0待审核 1通过 2驳回）',
  reject_reason varchar(500)   default null               comment '驳回原因',
  submit_by    varchar(64)     default null               comment '提交人',
  audit_by     varchar(64)     default null               comment '审核人',
  audit_time   datetime                                   comment '审核时间',
  create_by    varchar(64)     default ''                 comment '创建者',
  create_time  datetime                                   comment '创建时间',
  update_by    varchar(64)     default ''                 comment '更新者',
  update_time  datetime                                   comment '更新时间',
  remark       varchar(500)    default null               comment '备注',
  primary key (task_id),
  key idx_object_data (object_id, data_id)
) engine=innodb auto_increment=1 comment = '主数据审核任务表';

-- ----------------------------
-- 4. 数据分发与集成
-- ----------------------------
-- 4.1 应用凭证表
drop table if exists mdm_app;
create table mdm_app (
  app_id       bigint(20)      not null auto_increment    comment '应用ID',
  app_name     varchar(100)    not null                   comment '应用名称（订阅方）',
  appid        varchar(64)     not null                   comment '应用标识',
  secret       varchar(128)    not null                   comment '应用密钥',
  enabled      char(1)         default '0'                comment '状态（0停用 1启用）',
  remark       varchar(500)    default null               comment '备注',
  create_by    varchar(64)     default ''                 comment '创建者',
  create_time  datetime                                   comment '创建时间',
  update_by    varchar(64)     default ''                 comment '更新者',
  update_time  datetime                                   comment '更新时间',
  primary key (app_id),
  unique key uk_appid (appid)
) engine=innodb auto_increment=1 comment = '主数据应用凭证表';

-- 4.2 分发配置表
drop table if exists mdm_distribution;
create table mdm_distribution (
  dist_id      bigint(20)      not null auto_increment    comment '配置ID',
  app_id       bigint(20)      not null                   comment '订阅应用ID',
  object_id    bigint(20)      not null                   comment '数据对象ID',
  trigger_type varchar(20)     default 'IMMEDIATE'        comment '触发时机（IMMEDIATE变更即推/MANUAL手动重推）',
  endpoint_url varchar(500)    not null                   comment '订阅方回调地址',
  enabled      char(1)         default '0'                comment '状态（0停用 1启用）',
  remark       varchar(500)    default null               comment '备注',
  create_by    varchar(64)     default ''                 comment '创建者',
  create_time  datetime                                   comment '创建时间',
  update_by    varchar(64)     default ''                 comment '更新者',
  update_time  datetime                                   comment '更新时间',
  primary key (dist_id),
  key idx_app_object (app_id, object_id)
) engine=innodb auto_increment=1 comment = '主数据分发配置表';

-- 4.3 分发记录表
drop table if exists mdm_distribution_record;
create table mdm_distribution_record (
  record_id    bigint(20)      not null auto_increment    comment '记录ID',
  app_id       bigint(20)      not null                   comment '订阅应用ID',
  object_code  varchar(64)     not null                   comment '数据对象编码',
  data_id      bigint(20)      default null               comment '数据ID',
  action_type  varchar(10)     not null                   comment '操作类型（INSERT/UPDATE）',
  endpoint_url varchar(500)    not null                   comment '回调地址（重推用）',
  payload      text                                       comment '推送内容(JSON)',
  status       char(1)         default '0'                comment '状态（0待发送 1成功 2失败）',
  error_msg    varchar(500)    default null               comment '失败原因',
  send_time    datetime                                   comment '发送时间',
  success_time datetime                                   comment '成功时间',
  confirm_time datetime                                   comment '订阅方确认时间',
  retry_count  int            default 0                   comment '重试次数',
  create_by    varchar(64)     default ''                 comment '创建者',
  create_time  datetime                                   comment '创建时间',
  update_by    varchar(64)     default ''                 comment '更新者',
  update_time  datetime                                   comment '更新时间',
  remark       varchar(500)    default null               comment '备注',
  primary key (record_id),
  key idx_app_status (app_id, status)
) engine=innodb auto_increment=1 comment = '主数据分发记录表';

-- ----------------------------
-- 5. 主数据关系建模表（1.1.0）
-- ----------------------------
drop table if exists mdm_relation;
create table mdm_relation (
  id                bigint(20)      not null auto_increment    comment '关系ID',
  source_object_code varchar(64)    not null                   comment '源对象编码',
  target_object_code varchar(64)    not null                   comment '目标对象编码',
  relation_type     varchar(16)     not null                   comment '关系类型（ONE_TO_ONE/ONE_TO_MANY/MANY_TO_MANY）',
  source_field_code varchar(64)     default null               comment '源对象引用属性编码',
  cascade_rule      varchar(16)     default 'RESTRICT'         comment '级联规则（RESTRICT阻止删除/SET_NULL置空/CASCADE级联删除）',
  is_bidirectional  char(1)         default '0'                comment '是否双向（0否 1是）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (id),
  unique key uk_source_target (source_object_code, target_object_code)
) engine=innodb auto_increment=1 comment = '主数据对象关系定义表';

-- 1.1.0 列扩展：mdm_attribute（引用对象）
drop procedure if exists mdm_alter_columns;
delimiter $$
create procedure mdm_alter_columns()
begin
  if not exists (select 1 from information_schema.columns where table_schema=database() and table_name='mdm_attribute' and column_name='ref_object_code') then
    alter table mdm_attribute add column ref_object_code varchar(64) default null comment '引用目标对象编码' after enum_values;
  end if;
  if not exists (select 1 from information_schema.columns where table_schema=database() and table_name='mdm_attribute' and column_name='ref_display') then
    alter table mdm_attribute add column ref_display varchar(255) default null comment '引用显示字段（逗号分隔）' after ref_object_code;
  end if;

  -- 1.1.0 列扩展：mdm_object（审核流程 + 模板来源）
  if not exists (select 1 from information_schema.columns where table_schema=database() and table_name='mdm_object' and column_name='audit_process_key') then
    alter table mdm_object add column audit_process_key varchar(64) default null comment 'Flowable审核流程Key' after version;
  end if;
  if not exists (select 1 from information_schema.columns where table_schema=database() and table_name='mdm_object' and column_name='template_source') then
    alter table mdm_object add column template_source varchar(64) default null comment '模板来源（模板编码）' after audit_process_key;
  end if;

  -- 1.1.0 列扩展：mdm_distribution（分发方式）
  if not exists (select 1 from information_schema.columns where table_schema=database() and table_name='mdm_distribution' and column_name='channel') then
    alter table mdm_distribution add column channel varchar(8) default 'HTTP' comment '分发方式（HTTP/MQ）' after trigger_type;
  end if;
  if not exists (select 1 from information_schema.columns where table_schema=database() and table_name='mdm_distribution' and column_name='queue_name') then
    alter table mdm_distribution add column queue_name varchar(128) default null comment 'MQ队列名称' after channel;
  end if;
end$$
delimiter ;
call mdm_alter_columns();
drop procedure if exists mdm_alter_columns;

-- 1.1.0 字典：数据类型新增"引用"
insert into sys_dict_data values(2199, 8, '引用', 'ref', 'mdm_data_type', '', 'default', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), null) on duplicate key update dict_label='引用';

-- 1.1.0 字典：级联规则
delete from sys_dict_data where dict_code between 2200 and 2202;
delete from sys_dict_type where dict_id = 201;
insert into sys_dict_type values(201, '级联规则', 'mdm_cascade_rule', '0', 'admin', sysdate(), 'admin', sysdate(), '关系建模级联规则');
insert into sys_dict_data values(2200, 1, '阻止删除', 'RESTRICT', 'mdm_cascade_rule', '', 'default', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), null);
insert into sys_dict_data values(2201, 2, '置空',     'SET_NULL', 'mdm_cascade_rule', '', 'default', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), null);
insert into sys_dict_data values(2202, 3, '级联删除', 'CASCADE',  'mdm_cascade_rule', '', 'default', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), null);
