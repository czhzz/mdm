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
delete from sys_role_menu where menu_id between 2000 and 2026;
delete from sys_menu where menu_id between 2000 and 2026;
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

-- ----------------------------
-- 5. 种子字典
-- ----------------------------
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
