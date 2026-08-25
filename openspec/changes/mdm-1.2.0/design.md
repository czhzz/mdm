## 背景

1.1.0 上线后反馈 6 类问题：3 个阻断缺陷（添加属性报错、编码规则选不到对象、新建流程 404）、2 个体验问题（关系管理手输编码、模板库无响应）、1 个模块重做（数据分发 → 集成管理）。

## 目标与非目标

**目标**
- 修复 3 个 P0 缺陷，提供存量库增量迁移脚本 `sql/mdm/upgrade-1.2.0.sql`
- 关系管理表单下拉化、模板库恢复可用
- 数据分发重做为独立的"集成管理"模块：应用管理、接收接口管理、查询接口管理、分发管理、集成日志 5 个菜单
- 管理侧接口统一前缀 `/mdm/integration/*`，对外接口统一前缀 `/open/integration/*`

**非目标**
- 不做字段级脱敏/白名单（查询接口全属性返回）
- 不做对外接口限流（第一版观察用量后加）
- 不做日志定时清理（全量保留 + 手动清理）
- 不迁移历史分发配置数据（表沿用，菜单与接口重排）

## 已确认的决策（用户拍板）

| 决策点 | 结论 |
|--------|------|
| 接收数据落库 | 柔性方案：草稿落库 → 对象绑定 `audit_process_key` 则自动提交审核，未绑定直接生效；`source=API:appCode` |
| 接收/查询接口鉴权 | 复用 `mdm_app` 的 appid/secret（Header `X-App-Id` / `X-App-Secret`） |
| 集成日志保留 | 全量保留 + 页面手动清理，无定时任务 |
| 模块形态 | 集成管理作为独立模块开发：后端新 `integration` 子包、前端新 `views/mdm/integration/` 目录 |
| 接口命名 | 分发与接收、查询统一：管理侧 `/mdm/integration/{app,receive,query,distribute,log}/**` |

## 一、缺陷修复

### 1. 添加属性报 BindingException

- 根因：`model/mapper/MdmAttributeMapper.java:36` 的 `checkAttributeCodeUnique(Long objectId, String attrCode)` 双参数缺 `@Param`，MyBatis 命名为 arg0/arg1，`MdmAttributeMapper.xml:58` 的 `#{objectId}`/`#{attrCode}` 绑定失败（调用链 `MdmAttributeServiceImpl.java:85`）
- 修复：加 `@Param("objectId") @Param("attrCode")`
- 附带：全量排查 `ruoyi-mdm` mapper 层多参数方法（XML `#{}` 引用与 Java 参数比对），同类问题一并修

### 2. 编码规则选不到数据对象

- 根因（对 live 环境确证）：`coderule/index.vue:106` 加载对象下拉时传 `status:'1'`（仅生效对象），而 `mdm_object.status` 约定为 0草稿/1生效/2停用——用户当前唯一对象为草稿状态，被过滤掉 → 下拉空。编码规则本就应在对象发布前配置，过滤生效对象是错误行为
- 修复：前端去掉 status 过滤，列出全部对象（发布前即可配置编码规则）
- 防御：upgrade-1.2.0.sql 保留幂等 ALTER 补列（`audit_process_key`/`template_source`）——live 库已确认列齐全，但其他存量库若未执行 1.1.0 ALTER，`selectMdmObjectList` 查询这两列会报错导致同样症状

### 4. 关系管理下拉化

- `views/mdm/relation/index.vue:61-75` 表单三字段 `el-input` → `el-select`：
  - `sourceObjectCode` / `targetObjectCode`：选项来自 `listObject`，显示 `objectName（objectCode）`
  - `sourceFieldCode`：随源对象联动加载，选项 = `getObjectMeta` 返回属性中 `ref_object_code` 非空的引用属性
- 查询区两个编码输入框同步改下拉；后端不改

### 5. 模板库点击无反应

- 已排除静态问题：sys_menu 2037 已登记（component `mdm/template/index` 存在）、前后端接口路径一致（`/mdm/template/list|preview/{code}|create/{code}`）
- 定位路径：浏览器 Network 看 `/mdm/template/list` 是否 500（`MdmTemplateServiceImpl` 模板扫描逻辑）→ 若接口正常则查前端卡片 `@click="previewTemplate"` 逻辑
- 修复原则：运行时定位后改一处，不做防御性重写

### 6. 新建流程 404

- 根因：`sys_menu` 2040 path=`audit-designer`、parent=2039(path=`audit-process`) → 实际路由 `/mdm/audit-process/audit-designer`，而前端 `router.push('/mdm/audit/designer')`
- 修复（统一约定 `/mdm/audit/*`）：upgrade-1.2.0.sql 改 sys_menu —— 2039 挂 2027 下 path=`process`、2040 挂 2027 下 path=`designer` → `/mdm/audit/process`、`/mdm/audit/designer`；前端 push 不变

## 二、集成管理模块（独立模块）

### 模块结构

**后端**：`ruoyi-mdm` 新增第 11 个子包 `integration`，旧 `distribution` 包废弃：

```text
integration/
├── domain/       MdmApp、MdmDistributeApi、MdmDistributeLog（自 distribution 迁入，随表更名）
│                 MdmReceiveApi、MdmQueryApi、MdmReceiveLog、MdmQueryLog（新增）
├── mapper/       对应 MyBatis Mapper + XML
├── service/      app / receive / query / distribute / log 五组接口与实现
├── controller/   管理侧五组 Controller（前缀 /mdm/integration）
├── open/         对外接口 OpenReceiveController、OpenQueryController + ApiAuthFilter
└── config/       RabbitMQConfig 迁入（MQ 分发通道）
```

**前端**：新增 `views/mdm/integration/` 目录与 `api/mdm/integration.ts`，旧 `views/mdm/distribution/`、`api/mdm/distribution.ts` 废弃。

### 菜单结构

新增一级目录"集成管理"（path `integration`），下挂 5 个 C 菜单：

| 菜单 | 路由 | 页面（component） | 权限标识 |
|------|------|-------------------|----------|
| 应用管理 | /integration/app | mdm/integration/app/index | mdm:integration:app:list |
| 接收接口管理 | /integration/receive | mdm/integration/receive/index | mdm:integration:receive:list |
| 查询接口管理 | /integration/query | mdm/integration/query/index | mdm:integration:query:list |
| 分发管理 | /integration/distribute | mdm/integration/distribute/index | mdm:integration:distribute:list |
| 集成日志 | /integration/log | mdm/integration/log/index | mdm:integration:log:list |

- 分发管理页含两个 Tab：分发配置 + 分发监控（原 `monitor.vue` 内容并入）
- 集成日志页含三个 Tab：接收 / 查询 / 分发，各自独立日志表与查询接口，分开查看
- 原分发菜单（若有）在 upgrade SQL 中停用，不删除数据

### 表设计

表命名统一：配置表 `mdm_{类型}_api`、日志表 `mdm_{类型}_log`，接收/查询/分发各一张独立日志表：

```sql
mdm_receive_api       -- 接收接口配置
├── id BIGINT PK  AUTO_INCREMENT
├── api_code VARCHAR(64)   -- 接口编码（唯一）
├── api_name VARCHAR(128)
├── object_code VARCHAR(64) -- 目标数据对象编码
├── status CHAR(1)         -- 0 启用 / 1 停用
├── remark VARCHAR(255)
└── create_by/create_time/update_by/update_time

mdm_query_api         -- 查询接口配置（结构同 mdm_receive_api）

mdm_distribute_api    -- 分发配置（原 mdm_distribution 重命名）
├── 字段沿用现有 mdm_distribution（dist_id/app_id/object_id/trigger_type/
│   endpoint_url/queue_name/channel/enabled/remark/审计字段）
└── 仅改表名，字段不动

mdm_receive_log       -- 接收日志（独立表）
├── id BIGINT PK  AUTO_INCREMENT
├── app_code VARCHAR(64)
├── object_code VARCHAR(64)
├── business_code VARCHAR(64) -- 接收数据的 dataCode
├── success CHAR(1)          -- 0 成功 / 1 失败
├── request_summary TEXT     -- 请求摘要（截断 500 字符）
├── response_summary TEXT    -- 响应摘要（截断 500 字符）
├── error_msg VARCHAR(2000)
├── cost_ms INT
├── ip VARCHAR(64)
├── create_time DATETIME
└── KEY idx_app_time (app_code, create_time), KEY idx_object_time (object_code, create_time)

mdm_query_log         -- 查询日志（独立表）
├── 同 mdm_receive_log，无 business_code
├── result_count INT         -- 返回条数
└── KEY idx_app_time (app_code, create_time), KEY idx_object_time (object_code, create_time)

mdm_distribute_log    -- 分发日志（原 mdm_distribution_record 重命名，独立表）
├── 字段沿用原 record 表（id/app_id/object_code/data_id/action_type/
│   endpoint_url/payload/status(0待发送/1成功/2失败)/error_msg/
│   send_time/success_time/confirm_time/retry_count/审计字段）
├── 新增 app_code VARCHAR(64) -- 经 mdm_app 回填，统一筛选维度
└── KEY idx_app_status (app_id, status) 沿用
```

复用 1 张：`mdm_app`（应用与凭证）。三张日志表独立查询、独立清理；分发日志沿用原 record 表的三态状态与重推语义（payload 不截断），不与其他日志混表。

### 接口设计

**管理侧**（若依风格 `AjaxResult` / `TableDataInfo`，`@PreAuthorize` 权限）：

```text
/mdm/integration/app/**        应用 CRUD + secret 重置（对齐原 distribution.ts 调用语义）
/mdm/integration/receive/**    接收接口配置 CRUD
/mdm/integration/query/**      查询接口配置 CRUD
/mdm/integration/distribute/config/**   分发配置 CRUD（读 mdm_distribute_api）
/mdm/integration/distribute/monitor     分发监控（聚合 mdm_distribute_log）
/mdm/integration/log/receive/list      接收日志分页（mdm_receive_log）
/mdm/integration/log/query/list        查询日志分页（mdm_query_log）
/mdm/integration/log/distribute/list   分发日志分页（mdm_distribute_log）+ /mdm/integration/log/distribute/retry/{id} 重推
/mdm/integration/log/clean     手动清理（type 参数指定日志表，删除截止时间前日志，页面二次确认）
```

**对外侧**（外部系统调用，与若依登录无关）：

```text
POST /open/integration/receive/{apiCode}   Header: X-App-Id / X-App-Secret
    Body: { dataCode, data:{...} }
    幂等：同 dataCode 重复推送按 UPDATE 处理
    落库（柔性方案）：草稿 → 对象绑定 audit_process_key 则自动提交审核，否则直接生效
    source=API:<appCode>（供血缘）

POST /open/integration/query/{apiCode}     Header: X-App-Id / X-App-Secret
    Body: { filters:{...}, pageNum, pageSize }
    按对象查动态表，返回主数据列表

分发：无对外入口（平台被动推送，HTTP 推送 + MQ 双通道沿用）
```

**鉴权**：`open` 包内统一 Filter 校验 appid/secret + 应用状态（停用返回 401），失败记一条失败日志。

### 数据流

```text
外部系统 ──POST /open/integration/receive/{apiCode}──→ ApiAuthFilter 鉴权
  → 写 mdm_receive_log
  → 草稿写入 mdm_data_<obj_code>（source=API:appCode）
  → 对象绑定审核流程？──是──→ 启动 Flowable 审核 → 通过后生效
                        └─否─→ 直接生效
外部系统 ──POST /open/integration/query/{apiCode}───→ ApiAuthFilter 鉴权
  → 写 mdm_query_log → 查动态表 → 返回 JSON
数据变更 ──分发配置匹配──→ HTTP 推送 / MQ 发送
  → 写 mdm_distribute_log（含 endpoint_url/payload/retry_count）
```

## 三、迁移脚本 upgrade-1.2.0.sql（汇总）

- `mdm_object` 幂等补列：`audit_process_key`、`template_source`（#2）
- `sys_menu`：流程管理/流程设计器挂到审核菜单下改 path（#6）
- `sys_menu`：新增集成管理目录 + 5 子菜单；停用原分发菜单（2006 数据分发、2041 分发监控，2032-2035 按钮随迁或停用）（#3）
- 新表：`mdm_receive_api`、`mdm_query_api`、`mdm_receive_log`、`mdm_query_log`
- 表重命名：`mdm_distribution` RENAME → `mdm_distribute_api`；`mdm_distribution_record` RENAME → `mdm_distribute_log`（新增 app_code 列，经 `mdm_app` 回填）

## 风险与权衡

- [分发 Controller 迁移] → 现有 `MdmDistributionController`（位于 ruoyi-admin 的 `com.ruoyi.web.controller.mdm`，已完整实现前端 13 个接口）迁入 integration 包并统一路径/命名，无功能补写，仅核对 Service 参数对齐
- [旧 distribution 包废弃] → 代码迁入 integration 包后，旧包与旧前端目录的物理删除需用户确认（红线）；升级路径中旧菜单停用不删数据
- [对外接口安全性] → 仅 appid/secret 鉴权 + 日志留痕，无 IP 白名单与限流；企业内网部署可接受，公网暴露需升级
- [日志表膨胀] → 摘要截断 500 字符 + 手动清理按钮；量大再加 Quartz 定时清理
- [幂等接收] → 按 dataCode upsert；跨对象重复编码仍由对象唯一约束兜底
- [日志分表] → 接收/查询/分发各一张日志表，页面三 Tab 分开查看；分发日志沿用原 record 表三态状态与重推语义（payload 不截断），仅 RENAME + 补 app_code 列，历史数据无损；表数量多两张，但各表职责单一、互不干扰

## 待定问题

- 旧 `distribution` 包、`views/mdm/distribution/`、`api/mdm/distribution.ts` 迁移完成后是否物理删除（默认：删除，需确认）
- 集成管理一级菜单的图标与排序位置
- 对外接收接口是否需要重放/回调（第一版不做，失败仅记日志）
