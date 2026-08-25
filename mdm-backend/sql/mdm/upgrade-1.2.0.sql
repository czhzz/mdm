-- =====================================================================
-- MDM 1.2.0 增量升级脚本（存量库执行，幂等可重复执行）
-- 用法：mysql --default-character-set=utf8mb4 -u<user> -p<password> mdm < upgrade-1.2.0.sql
-- 变更清单见 openspec/changes/mdm-1.2.0/design.md
-- =====================================================================
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 一、#2 编码规则选不到数据对象：存量库补 1.1.0 缺失列
-- 根因：selectMdmObjectList 查询 audit_process_key/template_source，
--       存量库未执行 1.1.0 的 ALTER 导致 SQL 报错、对象下拉为空
-- ---------------------------------------------------------------------
drop procedure if exists mdm_add_column_if_missing;
delimiter ;;
create procedure mdm_add_column_if_missing()
begin
    if not exists (select 1 from information_schema.columns
                   where table_schema = database()
                     and table_name = 'mdm_object'
                     and column_name = 'audit_process_key') then
        alter table mdm_object
            add column audit_process_key varchar(64) default null comment 'Flowable审核流程Key' after version;
    end if;

    if not exists (select 1 from information_schema.columns
                   where table_schema = database()
                     and table_name = 'mdm_object'
                     and column_name = 'template_source') then
        alter table mdm_object
            add column template_source varchar(64) default null comment '模板来源（模板编码）' after audit_process_key;
    end if;
end;;
delimiter ;
call mdm_add_column_if_missing();
drop procedure mdm_add_column_if_missing;

-- ---------------------------------------------------------------------
-- 二、#6 新建流程跳转 404：sys_menu 路由与前端 push 对齐
-- 根因：菜单 path（audit-designer）与前端 push（/mdm/audit/designer）不一致；
--       且若依 buildMenus 仅渲染 M（目录）型菜单的子菜单，C 下挂 C 不生成路由。
-- 修复：新建"审核中心"M 目录（path=audit），审核待办/流程管理/流程设计器挂其下。
-- 目标路由：/mdm/audit/index、/mdm/audit/process、/mdm/audit/designer（前端 push 不变）
-- 注意：sys_menu 共 20 列（含 route_name，位于 query 之后），勿漏
-- ---------------------------------------------------------------------
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '2046', '审核中心', '2000', '7', 'audit', '', '', '', '1', '0', 'M', '0', '0', '', 'example', 'admin', sysdate(), '', null, '审核中心目录（1.2.0）'
from dual where not exists (select 1 from sys_menu where menu_id = '2046');

update sys_menu set parent_id = '2046', path = 'index',    order_num = '1' where menu_id = '2027';
update sys_menu set parent_id = '2046', path = 'process',  order_num = '2' where menu_id = '2039';
update sys_menu set parent_id = '2046', path = 'designer', order_num = '3' where menu_id = '2040';

-- ---------------------------------------------------------------------
-- 三、#3 数据分发 → 集成管理：表重命名 + 新表（第 2 周）
-- 配置表命名 mdm_{类型}_api，日志表 mdm_{类型}_log
-- RENAME 仅改表名，字段不动（含 1.1.0 的 channel/queue_name 列）
-- ---------------------------------------------------------------------
drop procedure if exists mdm_rename_distribution_tables;
delimiter ;;
create procedure mdm_rename_distribution_tables()
begin
    if exists (select 1 from information_schema.tables
               where table_schema = database() and table_name = 'mdm_distribution')
       and not exists (select 1 from information_schema.tables
               where table_schema = database() and table_name = 'mdm_distribute_api') then
        rename table mdm_distribution to mdm_distribute_api;
    end if;

    if exists (select 1 from information_schema.tables
               where table_schema = database() and table_name = 'mdm_distribution_record')
       and not exists (select 1 from information_schema.tables
               where table_schema = database() and table_name = 'mdm_distribute_log') then
        rename table mdm_distribution_record to mdm_distribute_log;
        -- 新增 app_code 统一筛选维度，经 mdm_app.appid 回填（RENAME 后一次性执行）
        alter table mdm_distribute_log add column app_code varchar(64) default null comment '应用编码（mdm_app.appid 回填）' after app_id;
        update mdm_distribute_log l left join mdm_app a on a.app_id = l.app_id set l.app_code = a.appid;
    end if;
end;;
delimiter ;
call mdm_rename_distribution_tables();
drop procedure mdm_rename_distribution_tables;

-- 接收接口配置表
create table if not exists mdm_receive_api (
  id            bigint(20)      not null auto_increment    comment '接口ID',
  api_code      varchar(64)     not null                   comment '接口编码（唯一，对外路径用）',
  api_name      varchar(128)    not null                   comment '接口名称',
  object_code   varchar(64)     not null                   comment '目标数据对象编码',
  status        char(1)         default '0'                comment '状态（0启用 1停用）',
  remark        varchar(500)    default null               comment '备注',
  create_by     varchar(64)     default ''                 comment '创建者',
  create_time   datetime                                   comment '创建时间',
  update_by     varchar(64)     default ''                 comment '更新者',
  update_time   datetime                                   comment '更新时间',
  primary key (id),
  unique key uk_api_code (api_code)
) engine=innodb auto_increment=1 comment = '接收接口配置表';

-- 查询接口配置表（结构同接收接口）
create table if not exists mdm_query_api (
  id            bigint(20)      not null auto_increment    comment '接口ID',
  api_code      varchar(64)     not null                   comment '接口编码（唯一，对外路径用）',
  api_name      varchar(128)    not null                   comment '接口名称',
  object_code   varchar(64)     not null                   comment '目标数据对象编码',
  status        char(1)         default '0'                comment '状态（0启用 1停用）',
  remark        varchar(500)    default null               comment '备注',
  create_by     varchar(64)     default ''                 comment '创建者',
  create_time   datetime                                   comment '创建时间',
  update_by     varchar(64)     default ''                 comment '更新者',
  update_time   datetime                                   comment '更新时间',
  primary key (id),
  unique key uk_api_code (api_code)
) engine=innodb auto_increment=1 comment = '查询接口配置表';

-- 接收日志表
create table if not exists mdm_receive_log (
  id               bigint(20)   not null auto_increment    comment '日志ID',
  app_code         varchar(64)  default null               comment '应用编码（appid）',
  object_code      varchar(64)  default null               comment '数据对象编码',
  business_code    varchar(64)  default null               comment '接收数据的 dataCode',
  success          char(1)      default '0'                comment '0成功 1失败',
  request_summary  text                                    comment '请求摘要（截断500字符）',
  response_summary text                                    comment '响应摘要（截断500字符）',
  error_msg        varchar(2000) default null              comment '错误信息',
  cost_ms          int          default 0                  comment '耗时（毫秒）',
  ip               varchar(64)  default null               comment '来源IP',
  create_time      datetime                                comment '创建时间',
  primary key (id),
  key idx_app_time (app_code, create_time),
  key idx_object_time (object_code, create_time)
) engine=innodb auto_increment=1 comment = '接收接口日志表';

-- 查询日志表
create table if not exists mdm_query_log (
  id               bigint(20)   not null auto_increment    comment '日志ID',
  app_code         varchar(64)  default null               comment '应用编码（appid）',
  object_code      varchar(64)  default null               comment '数据对象编码',
  success          char(1)      default '0'                comment '0成功 1失败',
  request_summary  text                                    comment '请求摘要（截断500字符）',
  response_summary text                                    comment '响应摘要（截断500字符）',
  error_msg        varchar(2000) default null              comment '错误信息',
  cost_ms          int          default 0                  comment '耗时（毫秒）',
  ip               varchar(64)  default null               comment '来源IP',
  result_count     int          default 0                  comment '返回条数',
  create_time      datetime                                comment '创建时间',
  primary key (id),
  key idx_app_time (app_code, create_time),
  key idx_object_time (object_code, create_time)
) engine=innodb auto_increment=1 comment = '查询接口日志表';
