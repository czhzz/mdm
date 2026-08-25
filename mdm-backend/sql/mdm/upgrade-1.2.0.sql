-- =====================================================================
-- MDM 1.2.0 增量升级脚本（存量库执行，幂等可重复执行）
-- 用法：mysql -u<user> -p<password> mdm < upgrade-1.2.0.sql
-- 变更清单见 openspec/changes/mdm-1.2.0/design.md
-- =====================================================================

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
