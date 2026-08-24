SET NAMES utf8mb4;

-- ============================================================
-- 示例主数据落地（第 8 周 9.2）：客户、供应商
-- 依赖：已执行 sql/mdm/init.sql
-- 幂等：可重复执行（先清理再插入）
-- ============================================================

-- ---------- 示例对象：客户 customer（演示启用审核） ----------
set @oid = (select object_id from mdm_object where object_code = 'customer' limit 1);
delete from mdm_audit_flow where object_id = @oid;
delete from mdm_code_rule_segment where rule_id in (select rule_id from mdm_code_rule where object_id = @oid);
delete from mdm_code_rule where object_id = @oid;
delete from mdm_attribute where object_id = @oid;
delete from mdm_audit_task where object_id = @oid;
drop table if exists mdm_data_customer;
delete from mdm_object where object_code = 'customer';

insert into mdm_object (object_code, object_name, category_id, status, version, order_num, create_by, create_time)
values ('customer', '客户', 0, '1', '1.0', 1, 'system', sysdate());
set @oid = last_insert_id();

insert into mdm_attribute (object_id, attr_code, attr_name, data_type, required_flag, unique_flag, source_type, enum_values, order_num, status, create_by, create_time) values
(@oid, 'cust_code',  '客户编码', 'text', 'Y', 'Y', 'input', null,      1, '0', 'system', sysdate()),
(@oid, 'cust_name',  '客户名称', 'text', 'Y', 'N', 'input', null,      2, '0', 'system', sysdate()),
(@oid, 'cust_level', '客户等级', 'text', 'N', 'N', 'enum',  'A,B,C',   3, '0', 'system', sysdate()),
(@oid, 'contact',    '联系人',   'text', 'N', 'N', 'input', null,      4, '0', 'system', sysdate()),
(@oid, 'phone',      '联系电话', 'text', 'N', 'N', 'input', null,      5, '0', 'system', sysdate());

insert into mdm_code_rule (object_id, rule_name, reset_type, code_field, status, create_by, create_time)
values (@oid, '客户编码规则', 'DAY', 'cust_code', '0', 'system', sysdate());
set @rid = last_insert_id();
insert into mdm_code_rule_segment (rule_id, seg_type, seg_value, order_num) values
(@rid, 'CONSTANT', 'CUST-',      1),
(@rid, 'DATE',     'yyyyMMdd',   2),
(@rid, 'SEQUENCE', '4',          3);

insert into mdm_audit_flow (object_id, enabled, audit_role, create_by, create_time)
values (@oid, '1', 'admin', 'system', sysdate());

create table mdm_data_customer (
  id          bigint(20)   not null auto_increment comment '主键',
  object_code varchar(50)  not null comment '对象编码',
  status      char(1)      default '0' comment '状态(0草稿 1生效 2停用)',
  version     int          default 1 comment '版本号',
  cust_code   varchar(255) not null comment '客户编码',
  cust_name   varchar(255) not null comment '客户名称',
  cust_level  varchar(100) default null comment '客户等级',
  contact     varchar(255) default null comment '联系人',
  phone       varchar(255) default null comment '联系电话',
  create_by   varchar(64)  default '' comment '创建者',
  create_time datetime     comment '创建时间',
  update_by   varchar(64)  default '' comment '更新者',
  update_time datetime     comment '更新时间',
  remark      varchar(500) default null comment '备注',
  primary key (id),
  unique key uk_object_code (object_code),
  unique key uk_cust_code (cust_code)
) engine=innodb default charset=utf8mb4 comment='主数据业务表';

-- ---------- 示例对象：供应商 supplier（演示不走审核，直接维护） ----------
set @oid = (select object_id from mdm_object where object_code = 'supplier' limit 1);
delete from mdm_code_rule_segment where rule_id in (select rule_id from mdm_code_rule where object_id = @oid);
delete from mdm_code_rule where object_id = @oid;
delete from mdm_attribute where object_id = @oid;
delete from mdm_audit_task where object_id = @oid;
drop table if exists mdm_data_supplier;
delete from mdm_object where object_code = 'supplier';

insert into mdm_object (object_code, object_name, category_id, status, version, order_num, create_by, create_time)
values ('supplier', '供应商', 0, '1', '1.0', 2, 'system', sysdate());
set @oid = last_insert_id();

insert into mdm_attribute (object_id, attr_code, attr_name, data_type, required_flag, unique_flag, source_type, enum_values, order_num, status, create_by, create_time) values
(@oid, 'supplier_code',  '供应商编码', 'text', 'Y', 'Y', 'input', null,      1, '0', 'system', sysdate()),
(@oid, 'supplier_name',  '供应商名称', 'text', 'Y', 'N', 'input', null,      2, '0', 'system', sysdate()),
(@oid, 'supplier_grade', '供应等级',   'text', 'N', 'N', 'enum',  'A,B,C',   3, '0', 'system', sysdate()),
(@oid, 'city',           '所在城市',   'text', 'N', 'N', 'input', null,      4, '0', 'system', sysdate());

insert into mdm_code_rule (object_id, rule_name, reset_type, code_field, status, create_by, create_time)
values (@oid, '供应商编码规则', 'DAY', 'supplier_code', '0', 'system', sysdate());
set @rid = last_insert_id();
insert into mdm_code_rule_segment (rule_id, seg_type, seg_value, order_num) values
(@rid, 'CONSTANT', 'SUP-',      1),
(@rid, 'DATE',     'yyyyMMdd',  2),
(@rid, 'SEQUENCE', '4',         3);

create table mdm_data_supplier (
  id             bigint(20)   not null auto_increment comment '主键',
  object_code    varchar(50)  not null comment '对象编码',
  status         char(1)      default '0' comment '状态(0草稿 1生效 2停用)',
  version        int          default 1 comment '版本号',
  supplier_code  varchar(255) not null comment '供应商编码',
  supplier_name  varchar(255) not null comment '供应商名称',
  supplier_grade varchar(100) default null comment '供应等级',
  city           varchar(255) default null comment '所在城市',
  create_by      varchar(64)  default '' comment '创建者',
  create_time    datetime     comment '创建时间',
  update_by      varchar(64)  default '' comment '更新者',
  update_time    datetime     comment '更新时间',
  remark         varchar(500) default null comment '备注',
  primary key (id),
  unique key uk_object_code (object_code),
  unique key uk_supplier_code (supplier_code)
) engine=innodb default charset=utf8mb4 comment='主数据业务表';