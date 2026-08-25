# mdm-1.2.0

主数据管理平台 1.2.0 —— 缺陷修复 + 数据分发重做为集成管理

## 版本

1.2.0（基于 1.1.0 增量升级）

## 问题清单 → 计划映射

| # | 问题（用户反馈） | 根因定位 | 类型 | 优先级 |
|---|------------------|----------|------|--------|
| 1 | 模型管理添加属性报 `BindingException: Parameter 'objectId' not found` | `MdmAttributeMapper.checkAttributeCodeUnique` 双参数缺 `@Param`，XML 引用 `#{objectId}/#{attrCode}` | 缺陷 | P0 |
| 2 | 编码规则选不到数据对象 | 已确证：`coderule/index.vue` 仅加载 `status='1'`（生效）对象，草稿对象被过滤导致下拉空；已修复（去掉过滤，编码规则发布前即可配置） | 缺陷 | P0 |
| 3 | 数据分发模块重做 → 集成管理 | 现分发模块功能单一，重做为多菜单集成管理（应用/接收接口/查询接口/分发/集成日志） | 重构 | P2 |
| 4 | 关系管理源/目标对象、属性编码应为下拉 | `relation/index.vue` 表单三字段均为 `el-input` 自由文本 | 体验 | P1 |
| 5 | 模板库点击没有反应 | 待排查：菜单路由未挂载或模板接口报错 | 缺陷 | P1 |
| 6 | 流程管理新建流程跳转 404 | 已确证：`sys_menu` 路径 `audit-designer` ≠ 前端 `router.push('/mdm/audit/designer')` | 缺陷 | P0 |

## 里程碑（4 周，2 人团队）

- 第 1 周：P0 缺陷修复（#1 #2 #6）+ 回归
- 第 2 周：P1（#4 #5）+ 集成管理重构启动（#3）
- 第 3 周：集成管理完成（#3）
- 第 4 周：联调 + 全量冒烟 + 发布

## 关键决策

- 集成管理作为独立模块开发：后端新 `integration` 子包（迁入 distribution 代码与 ruoyi-admin 的 `MdmDistributionController`）、前端新 `views/mdm/integration/`；管理侧接口统一 `/mdm/integration/*`、对外 `/open/integration/*`
- 表命名统一：配置表 `mdm_receive_api` / `mdm_query_api` / `mdm_distribute_api`（原 mdm_distribution 重命名）；日志表分设 `mdm_receive_log` / `mdm_query_log` / `mdm_distribute_log`（原 mdm_distribution_record 重命名），集成日志页面三 Tab 分开查看
- 已确认：接收数据柔性落库（绑定流程走审核，否则直接生效）、接收/查询复用 appid/secret 鉴权、日志全量保留 + 手动清理
- 路由问题统一修复约定：前端 `router.push` 与 `sys_menu.path` 对齐，目标路由 `/mdm/audit/designer`
- 存量库提供增量迁移脚本 `sql/mdm/upgrade-1.2.0.sql`（ALTER + 菜单变更 + 新表），不再依赖重新执行 init.sql
