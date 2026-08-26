# 1.2.0 实施计划（4 周 / 约 40 人天，按 2 人团队：后端 + 前端并行）

> 周期：4 周（含联调与发布）。里程碑见文末。
> 每任务标注预估工时（人天），后端/前端按任务标注。

## 前置准备

- [x] 0.1 后端：新建 `sql/mdm/upgrade-1.2.0.sql` 增量迁移脚本框架（已含 #2 幂等补列 + #6 sys_menu 修复；新表与菜单在第 2-3 周追加）（工时：0.5 人天）

## 第 1 周：P0 缺陷修复（10 人天）

### #1 添加属性报 BindingException

- [x] 1.1 后端：`model/mapper/MdmAttributeMapper.java:36` 的 `checkAttributeCodeUnique` 加 `@Param("objectId") @Param("attrCode")`（根因：双参数无 @Param，MyBatis 命名为 arg0/arg1，XML `MdmAttributeMapper.xml:58` 的 `#{objectId}` 绑定失败）（工时：0.25 人天）
- [x] 1.2 后端：全量排查 `ruoyi-mdm` 下所有 mapper 多参数方法——13 个接口逐一扫描，仅此一处缺 @Param，其余均单参数或已标注（工时：1 人天）
- [x] 1.3 后端：`mvn -pl ruoyi-mdm -am compile` 编译通过；线上验证待后端容器重建后执行（工时：0.25 人天）

### #2 编码规则选不到数据对象

- [x] 1.4 后端：确证根因——live 库 `mdm_object` 两列齐全（排除缺列），真正原因是 `coderule/index.vue` 的 `status:'1'` 过滤把草稿对象（status=0）排除，当前唯一对象为草稿 → 下拉空（工时：0.5 人天）
- [x] 1.5 前端：`coderule/index.vue` loadObjects 去掉 status 过滤，列出全部对象（编码规则应在对象发布前即可配置）（工时：0.25 人天）
- [x] 1.6 后端：upgrade-1.2.0.sql 保留幂等补列（`audit_process_key`、`template_source`，仿 init.sql:378-382 写法）作存量库防御（工时：0.25 人天）
- [x] 1.7 验证：对象列表接口返回草稿对象（ORG），编码规则页下拉数据来源恢复（工时：0.25 人天）

### #6 新建流程 404

- [x] 1.8 后端：upgrade-1.2.0.sql 修正 `sys_menu`——新建"审核中心"M 目录（2046，path=audit），2027/2039/2040 挂其下（index/process/designer）。实施中发现若依 buildMenus 仅渲染 M 型子菜单，C 下挂 C 不生成路由，故必须建 M 目录（工时：0.5 人天）
- [x] 1.9 前端：核对 push 无需改动——`audit/index.vue:172,176`、`process-list.vue:51` 的 `/mdm/audit/process`、`/mdm/audit/designer` 与菜单新 path 一致（工时：0.25 人天）
- [x] 1.10 验证：getRouters 返回 `/mdm/audit/index|process|designer` 三个子路由，与前端 push 一致（工时：0.25 人天）

### 回归

- [x] 1.11 后端：`scripts/smoke-mdm.sh` 全量冒烟 26 项全绿（导入种子 + 部署演示审核流程 + 起 mock 推送容器后；顺带修复冒烟脚本 3 处环境假设问题、seed-demo.sql 的审核绑定（旧 mdm_audit_flow → Flowable））（工时：0.5 人天）

## 第 2 周：体验优化 + 集成管理启动（10 人天）

### #4 关系管理下拉化

- [x] 2.1 前端：`views/mdm/relation/index.vue` 表单源对象/目标对象 `el-input` → `el-select`（选项调 `listObject`，展示 objectName（objectCode））（工时：0.5 人天）
- [x] 2.2 前端：引用属性编码 `el-input` → `el-select`，依赖源对象选择联动加载（`getObjectMeta` 返回的属性列表，仅引用类型属性可选）（工时：1 人天）
- [x] 2.3 前端：查询区两个编码输入框同步改下拉（工时：0.5 人天）

### #5 模板库点击无反应

- [x] 2.4 排查：确认根因——菜单（2037）与接口路径（`/mdm/template/list|preview/{code}|create/{code}`）均正常，无反应点位于卡片点击：`template/index.vue` 本地函数 `previewTemplate` 与 API import 同名，函数体内自递归 → 栈溢出、弹窗永不打开（工时：1 人天）
- [x] 2.5 修复：API import 别名 `previewTemplate as fetchTemplatePreview`，消除遮蔽；无需 SQL 菜单修正（工时：1 人天）
- [x] 2.6 验证：前端构建通过；弹窗/一键创建运行时验证归 4.2 全量回归（工时：0.5 人天）

### #3 集成管理（启动）

- [x] 2.7 后端：设计确认——表结构与模块形态见 design.md（`mdm_receive_api` / `mdm_query_api` / `mdm_integration_log`；集成管理为独立模块）（工时：0.5 人天）
- [x] 2.8 后端：upgrade-1.2.0.sql —— 新建 `mdm_receive_api`、`mdm_query_api`、`mdm_receive_log`、`mdm_query_log`；`mdm_distribution` RENAME `mdm_distribute_api`；`mdm_distribution_record` RENAME `mdm_distribute_log`（新增 app_code 列经 mdm_app 回填）（工时：1 人天）
- [x] 2.9 后端：新建 `integration` 子包（独立模块）——迁入 distribution 现有 domain/mapper/service/RabbitMQConfig（随表更名 MdmDistributeApi/MdmDistributeLog + app_code 回填），新增 receive/query/log 三组 domain + mapper（MyBatis XML 与现有风格一致）；RabbitMQConfig Bean 声明暂留旧包，旧包删除时并入（工时：1.5 人天）
- [x] 2.10 前端：新建 `views/mdm/integration/` 目录骨架（5 页面占位）+ `api/mdm/integration.ts` 统一接口封装（app/receive/query/distribute/log/clean，管理侧前缀 `/mdm/integration`）（工时：1 人天）

## 第 3 周：集成管理完成（10 人天）

- [x] 3.1 后端：应用管理——`MdmDistributionController`（ruoyi-admin）迁入 integration 包（`IntegrationAppController`），接口路径改为 `/mdm/integration/app`，对齐原 distribution.ts 语义；`OpenMdmController` 引用同步切到 integration 版（工时：1 人天）
- [x] 3.2 后端：接收接口管理——`IntegrationReceiveController` CRUD + 对外 `POST /open/integration/receive/{apiCode}`（ApiAuthFilter appid/secret 鉴权、dataCode 映射对象唯一属性幂等 upsert、柔性落库：绑定审核流程则提交审核否则直接生效、source=API:appCode、写 mdm_receive_log）（工时：2 人天）
- [x] 3.3 后端：查询接口管理——`IntegrationQueryController` CRUD + 对外 `POST /open/integration/query/{apiCode}`（条件查询动态表，ApiAuthFilter 鉴权，写 mdm_query_log）（工时：2 人天）
- [x] 3.4 后端：分发管理——`IntegrationDistributeController` 命名统一 `/mdm/integration/distribute/config|monitor`；读写改 `mdm_distribute_api` + `mdm_distribute_log`（重推按日志 id，payload 完整保留不截断），MQ/HTTP 双通道核心逻辑不动；`MdmDataServiceImpl.triggerPush` 引用切 integration 版（修复 RENAME 后旧表不存在的回归）（工时：2 人天）
- [x] 3.5 后端：集成日志——`IntegrationLogController` 三表分页 list + 失败重推 + `/mdm/integration/log/clean`（type 参数指定表）手动清理（工时：1.5 人天）
- [x] 3.6 前端：`views/mdm/integration/` 五个页面（若依标准列表 + 弹窗表单；分发管理含配置/监控两 Tab，集成日志含接收/查询/分发三 Tab + 清理按钮）（工时：2.5 人天）
- [x] 3.7 后端：upgrade-1.2.0.sql 登记集成管理一级目录（2047）+ 5 子菜单（2048-2052）+ 按钮权限（2053-2070），停用原分发菜单（2006/2041），admin 角色授权（工时：0.5 人天）
- [x] 3.8 后端：旧 `distribution` 包、`MdmDistributionController`、旧 mapper XML、`views/mdm/distribution/`、`api/mdm/distribution.ts` 已删除（用户确认）；RabbitMQConfig Bean 声明并入 integration 版，`OpenMdmController` 引用已切换（工时：0.25 人天）

## 第 4 周：联调 + 回归 + 发布（10 人天）

- [ ] 4.1 前后端联调：集成管理 5 菜单全流程走查（应用注册 → 配置接收/查询接口 → 分发 → 日志可查）（工时：3 人天）
- [ ] 4.2 回归：`scripts/smoke-mdm.sh` 全量 + 本版本 6 项问题逐一复验（工时：2 人天）
- [ ] 4.3 文档：`doc/RELEASES.md` 登记 1.2.0 发布记录；`doc/operations.md` 补充集成管理运维说明（存量库执行 upgrade-1.2.0.sql）（工时：1 人天）
- [ ] 4.4 发布：打 tag `v1.2.0`（工时：0.5 人天）

## 里程碑

- M1（第 1 周末）：3 个 P0 缺陷修复，冒烟全绿
- M2（第 2 周末）：关系下拉 + 模板库可用，集成管理表结构与后端骨架完成
- M3（第 3 周末）：集成管理 5 菜单功能完整
- M4（第 4 周末）：全量回归通过，发布 v1.2.0

## 待确认问题

- 集成管理一级菜单的图标与排序位置（菜单名与 5 子菜单已定）
- 旧 distribution 相关文件迁移完成后是否物理删除（默认删除，动手前确认）
- 日志保留/限流已定：全量保留 + 手动清理；限流第一版不做
