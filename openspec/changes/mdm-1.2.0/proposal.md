## Why

1.1.0 上线后用户反馈 6 类问题：3 个阻断性缺陷（添加属性报错、编码规则选不到对象、新建流程 404）、2 个体验问题（关系管理手输编码、模板库无响应）、1 个模块重做需求（数据分发 → 集成管理）。1.2.0 目标：缺陷清零 + 分发模块升级为集成管理，推动 MDM 从"好用"走向"企业级集成枢纽"。

## What Changes

### 一、缺陷修复（P0，第 1 周）

- **model-attribute-param**（#1）：`MdmAttributeMapper.checkAttributeCodeUnique(Long objectId, String attrCode)` 缺 `@Param` 注解，MyBatis 将参数命名为 arg0/arg1，XML 中 `#{objectId}` 绑定失败。修复为 `@Param("objectId") @Param("attrCode")`，并全量排查 mapper 层同类多参数方法。
- **coderule-object-list**（#2）：编码规则页对象下拉调用 `/mdm/model/object/list` 返回空。排查确证候选根因（存量库缺 1.1.0 ALTER 列导致 SQL 报错 / status 过滤不匹配），修复并提供增量迁移 SQL。
- **audit-designer-route**（#6）：`sys_menu` 中流程设计器 path 为 `audit-designer`，前端 `router.push('/mdm/audit/designer')`，路由不匹配 → 404。统一路由约定后修复。

### 二、体验优化（P1，第 2 周）

- **relation-dropdown**（#4）：关系管理表单源对象/目标对象/引用属性编码由自由文本改为下拉选择（对象列表来自模型管理，属性编码依赖源对象联动加载）。
- **template-fix**（#5）：排查模板库点击无反应（菜单路由未挂载 / 模板接口报错），修复并冒烟验证。

### 三、数据分发重做 → 集成管理（P2，第 2-3 周）

- **distribution→integration**（#3）：数据分发模块重构为集成管理模块，拆分为 5 个菜单：

| 菜单 | 说明 |
|------|------|
| 应用管理 | 接入方应用注册与凭证（appid/secret），沿用 `mdm_app` |
| 接收接口管理 | 配置平台向外部系统提供的接收接口（数据上报/同步入口） |
| 查询接口管理 | 配置开放给外部的数据查询接口（按对象、条件查询主数据） |
| 分发管理 | 原分发配置（订阅对象 + HTTP/MQ 通道） |
| 集成日志 | 接收日志 / 查询日志 / 分发日志 三个视图，支持按应用/对象/状态/时间检索 |

集成管理作为独立模块开发（后端 `integration` 子包 + 前端 `views/mdm/integration/`），管理侧接口统一 `/mdm/integration/*` 前缀（分发与接收、查询命名对齐）。

### 废弃

- 旧 `distribution` 子包（后端）、`views/mdm/distribution/` + `api/mdm/distribution.ts`（前端）：代码迁入 integration 模块后废弃
- 原分发单页结构：由集成管理多菜单替换
- 表命名统一：配置表 `mdm_receive_api` / `mdm_query_api` / `mdm_distribute_api`（原 mdm_distribution 重命名）；日志表分设 `mdm_receive_log` / `mdm_query_log` / `mdm_distribute_log`（原 mdm_distribution_record 重命名），页面分开查看；`mdm_app` 沿用

## Impact

- **后端**（mdm-backend）：`ruoyi-mdm` 新增独立 `integration` 子包（迁入 distribution 现有代码与 ruoyi-admin 的 `MdmDistributionController`），新增接收接口、查询接口、集成日志三组 domain/mapper/service/controller；`model` 子包修复 mapper 参数注解；旧 `distribution` 包废弃；新增 SQL 迁移脚本 `sql/mdm/upgrade-1.2.0.sql`（ALTER + sys_menu 变更 + 新表 + 表重命名）
- **前端**（mdm-frontend）：新增 `views/mdm/integration/` 五页面与 `api/mdm/integration.ts`，旧 `views/mdm/distribution/` 与 `api/mdm/distribution.ts` 废弃；`views/mdm/relation/` 表单改下拉；`views/mdm/audit/` 路由 push 修正
- **API**：管理侧统一 `/mdm/integration/{app,receive,query,distribute,log}/**`；对外新增 `/open/integration/receive/{apiCode}`、`/open/integration/query/{apiCode}`；旧 `/mdm/distribution/**` 停用
- **文档**：`doc/RELEASES.md` 登记 1.2.0
