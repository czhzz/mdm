# 1.1.0 实施计划（6 周 / 约 65 人天，按 2 人团队：后端 + 前端并行）

> 周期：6 周（含联调与发布）。里程碑见文末。
> 每任务标注预估工时（人天），后端/前端按任务标注。

## 前置准备

- [ ] 0.1 后端：`ruoyi-mdm/pom.xml` 新增 Flowable 6.x + Spring AMQP 依赖（工时：0.5 人天）
- [ ] 0.2 后端：`application.yml` 新增 Flowable + RabbitMQ 连接配置（占位符从 `.env` 读取）（工时：0.5 人天）
- [ ] 0.3 根目录 `.env.example` 新增 `RABBITMQ_HOST/PORT/USERNAME/PASSWORD` 变量（工时：0.25 人天）
- [ ] 0.4 `docker-compose.yml` 新增 RabbitMQ 服务（`rabbitmq:3-management`）（工时：0.25 人天）
- [ ] 0.5 前端：安装 bpmn-js 依赖（`npm install bpmn-js`）（工时：0.25 人天）

## 第 1 周：关系建模 + 模板库（10 人天）

### 后端

- [ ] 1.1 后端：`sql/mdm/init.sql` 新增 `mdm_relation` 表 DDL（字段：id, source_object_code, target_object_code, relation_type, source_field_code, cascade_rule, is_bidirectional, create_time, update_time）（工时：0.5 人天）
- [ ] 1.2 后端：`sql/mdm/init.sql` 对 `mdm_attribute` 新增 `ref_object_code`、`ref_display` 列（ALTER TABLE）（工时：0.25 人天）
- [ ] 1.3 后端：`sql/mdm/init.sql` 对 `mdm_object` 新增 `audit_process_key`、`template_source` 列（ALTER TABLE）（工时：0.25 人天）
- [ ] 1.4 后端：新建 `relation` 子包 —— `domain/MdmRelation.java`（继承 BaseEntity，字段对应 DDL）（工时：0.5 人天）
- [ ] 1.5 后端：`relation/mapper/MdmRelationMapper.java` + `resources/mapper/mdm/MdmRelationMapper.xml`（MyBatis CRUD + 按 source/target 对象编码查询）（工时：1 人天）
- [ ] 1.6 后端：`relation/service/IMdmRelationService.java` + `impl/MdmRelationServiceImpl.java`（CRUD + 级联校验逻辑：RESTRICT 阻止删除 / SET_NULL 置空 / CASCADE 级联删除）（工时：1.5 人天）
- [ ] 1.7 后端：`relation/controller/MdmRelationController.java`（REST 接口：列表/新增/编辑/删除，遵循 AjaxResult 响应体）（工时：0.5 人天）
- [ ] 1.8 后端：`model/domain/MdmAttribute.java` 新增 `refObjectCode`、`refDisplay` 字段（getter/setter + toString）（工时：0.25 人天）
- [ ] 1.9 后端：`model/domain/MdmObject.java` 新增 `auditProcessKey`、`templateSource` 字段（getter/setter + toString）（工时：0.25 人天）
- [ ] 1.10 后端：`maintenance/service/impl/MdmDataServiceImpl.java` 增强动态 CRUD——引用字段的下拉数据查询（根据 ref_object_code 查目标对象动态表）、N:M 关系中间表自动建表（模型发布时检测 mdm_relation 中的 MANY_TO_MANY 关系，生成 `mdm_data_<source>_<target>` 中间表）（工时：2 人天）
- [ ] 1.11 后端：`maintenance/service/impl/MdmDataServiceImpl.java` 增强删除逻辑——删除目标数据时执行级联规则（RESTRICT/SET_NULL/CASCADE）（工时：1 人天）

### 前端

- [ ] 1.12 前端：`src/api/mdm/relation.ts`（关联关系 CRUD 接口封装）（工时：0.5 人天）
- [ ] 1.13 前端：`src/views/mdm/relation/index.vue`（关联关系管理页面：对象间关系列表 + 新增/编辑弹窗，表单含源对象/目标对象/关系类型/级联规则/双向开关）（工时：1.5 人天）
- [ ] 1.14 前端：`src/views/mdm/model/index.vue` 属性编辑增强——dataType 下拉新增"引用"类型，选择后显示目标对象选择器和显示字段输入框（工时：1 人天）
- [ ] 1.15 前端：`src/views/mdm/maintenance/index.vue` 动态表单增强——引用字段渲染为下拉选择器（选项从目标对象加载），N:M 渲染为双列表穿梭框（工时：1.5 人天）

### 模板库

- [ ] 1.16 后端：新建 `template` 子包 —— `service/IMdmTemplateService.java` + `impl/MdmTemplateServiceImpl.java`（模板列表：扫描 5 个预置模板枚举；模板预览：返回模板详情 JSON；一键创建：事务中依次写入 mdm_object → mdm_attribute → mdm_code_rule → mdm_code_rule_segment → 字典值域 → 校验规则 → 查重配置 → sys_menu）（工时：2 人天）
- [ ] 1.17 后端：`template/controller/MdmTemplateController.java`（REST 接口：模板列表/预览/一键创建）（工时：0.5 人天）
- [ ] 1.18 前端：`src/api/mdm/template.ts`（模板接口封装）（工时：0.25 人天）
- [ ] 1.19 前端：`src/views/mdm/template/index.vue`（模板列表页：卡片布局，展示模板名称/描述/属性数；点击预览展开详情；一键创建按钮 + 确认弹窗）（工时：1.5 人天）

## 第 2 周：Flowable 审核（10 人天）

### 后端

- [ ] 2.1 后端：`ruoyi-mdm/pom.xml` 引入 Flowable 依赖（`flowable-spring-boot-starter` 6.x，兼容 Spring Boot 2.5.15）（工时：0.5 人天）
- [ ] 2.2 后端：`ruoyi-admin/src/main/resources/application.yml` 新增 Flowable 配置（数据源复用、自动建表 `ACT_*`、关闭异步历史、关闭 IDM 引擎）（工时：0.5 人天）
- [ ] 2.3 后端：新建 `audit` 子包（重写，覆盖 1.0.0 轻量审核）—— `service/IMdmAuditFlowableService.java` + `impl/MdmAuditFlowableServiceImpl.java`（流程定义部署：接收 BPMN XML 字符串，调用 RepositoryService 部署；流程定义查询：列表 + 版本列表；流程定义删除/挂起/激活）（工时：2 人天）
- [ ] 2.4 后端：`audit/service/impl/MdmAuditFlowableServiceImpl.java` 审核任务接口（启动流程实例：传入数据快照 JSON 作为流程变量；查询待办/已办：调用 TaskService 按 assignee/candidateGroup 查询；审批通过：complete task + 流程变量 approval=true；驳回：complete task + approval=false；退回：Flowable 的 back 跳转；加签/转办：TaskService delegateTask / addCandidateUser）（工时：2.5 人天）
- [ ] 2.5 后端：`audit/controller/MdmAuditFlowableController.java`（REST 接口：流程定义 CRUD + 部署/挂起/激活；审核任务：提交/待办列表/已办列表/通过/驳回/退回/加签/转办）（工时：1 人天）
- [ ] 2.6 后端：`maintenance/service/impl/MdmDataServiceImpl.java` 修改提交审核逻辑——从旧 `mdm_audit_task` 改为调用 Flowable 启动流程实例（工时：1 人天）
- [ ] 2.7 后端：`audit/listener/MdmAuditCompleteListener.java`（Flowable 任务完成监听器：审批通过时更新数据状态为已生效，驳回时回退为草稿）（工时：0.5 人天）
- [ ] 2.8 后端：废弃旧审核表 `mdm_audit_flow` / `mdm_audit_task`（不删除物理表，注释标记废弃，旧 Controller 停用）（工时：0.5 人天）

### 前端

- [ ] 2.9 前端：`src/api/mdm/audit.ts` 重写（Flowable 接口封装：流程定义 CRUD、审核任务待办/已办/通过/驳回/退回/加签/转办）（工时：0.5 人天）
- [ ] 2.10 前端：`src/views/mdm/audit/designer/index.vue`（流程设计器页面：集成 bpmn-js Modeler，左侧节点面板拖拽，中间画布编辑，右侧属性面板配置节点属性；工具栏：保存/部署/导出 XML；预设模板下拉：单人审批/多人会签/逐级审批）（工时：3 人天）
- [ ] 2.11 前端：`src/views/mdm/audit/process-list.vue`（流程定义列表：已部署流程列表、版本号、挂起/激活操作、删除）（工时：1 人天）
- [ ] 2.12 前端：`src/views/mdm/audit/index.vue` 重构（审核待办/已办：对接 Flowable TaskService；操作按钮：通过/驳回/退回/加签/转办；流程进度图：展示当前流程节点高亮）（工时：2 人天）
- [ ] 2.13 前端：`src/views/mdm/model/index.vue` 对象编辑增强——新增"审核流程"选择（下拉已部署的流程定义，保存到 `audit_process_key`）（工时：0.5 人天）

## 第 3 周：RabbitMQ 分发 + 质量大屏（10 人天）

### RabbitMQ 分发

- [ ] 3.1 后端：`ruoyi-mdm/pom.xml` 引入 Spring AMQP 依赖（`spring-boot-starter-amqp`）（工时：0.25 人天）
- [ ] 3.2 后端：`ruoyi-admin/src/main/resources/application.yml` 新增 RabbitMQ 连接配置（host/port/username/password/virtual-host，占位符从 `.env` 读取）（工时：0.25 人天）
- [ ] 3.3 后端：`distribution/config/RabbitMQConfig.java`（声明 Topic Exchange `mdm.distribution`、死信队列 `mdm.distribution.dlx`、Jackson2JsonMessageConverter）（工时：0.5 人天）
- [ ] 3.4 后端：`distribution/domain/MdmDistribution.java` 新增 `channel`、`queueName` 字段（getter/setter + toString）（工时：0.25 人天）
- [ ] 3.5 后端：`sql/mdm/init.sql` 对 `mdm_distribution` 新增 `channel` VARCHAR(8)、`queue_name` VARCHAR(128) 列（ALTER TABLE）（工时：0.25 人天）
- [ ] 3.6 后端：`distribution/service/impl/DistributionServiceImpl.java` 增强——分发逻辑按 channel 分流：HTTP 走现有线程池推送；MQ 走 RabbitTemplate 发送到 Topic Exchange（路由键 `mdm.dist.<obj_code>`）（工时：1.5 人天）
- [ ] 3.7 后端：`distribution/service/impl/DistributionServiceImpl.java` MQ 重试逻辑——发布确认 + 死信队列监听 + 重推接口（从死信队列消费后重新投递）（工时：1 人天）
- [ ] 3.8 后端：`distribution/controller/MdmDistributionController.java` 新增分发监控接口（队列积压量、成功率、延迟趋势——通过 RabbitMQ Management API 查询或 RabbitTemplate 获取）（工时：0.5 人天）

### 前端

- [ ] 3.9 前端：`src/api/mdm/distribution.ts` 新增分发监控接口封装（工时：0.25 人天）
- [ ] 3.10 前端：`src/views/mdm/distribution/index.vue` 增强——分发配置表单新增"分发方式"下拉（HTTP/MQ），MQ 时显示队列名称输入框；分发记录列表新增"通道"列（工时：1 人天）
- [ ] 3.11 前端：`src/views/mdm/distribution/monitor.vue`（分发监控面板：队列积压数字卡片、成功率折线图、延迟趋势折线图；数据每 30s 自动刷新）（工时：1.5 人天）

### 数据质量大屏

- [ ] 3.12 后端：`quality/service/impl/MdmQualityServiceImpl.java` 新增大屏聚合查询方法（质量总览：数据总量/完整率/合规率/问题数 + 环比；对象健康度排行：各对象完整率/合规率 Top-10；质量问题趋势：按日/周/月统计校验失败/重复/缺失三类问题数；规则命中分布：各规则命中次数占比）（工时：1.5 人天）
- [ ] 3.13 后端：`quality/controller/MdmQualityController.java` 新增大屏数据接口（`GET /mdm/quality/dashboard`，返回所有面板数据）（工时：0.5 人天）
- [ ] 3.14 前端：`src/api/mdm/quality.ts` 新增大屏接口封装（工时：0.25 人天）
- [ ] 3.15 前端：`src/views/mdm/quality-dashboard/index.vue`（全屏大屏页面：5 面板 ECharts 布局——上排总览卡片 3 个（数据总量/完整率/合规率）+ 中排趋势图 2 个（问题趋势折线图 + 规则命中饼图）+ 下排排行柱状图 + 问题滚动表格；时间范围选择器；30s/60s/手动刷新切换）（工时：2.5 人天）

## 第 4 周：血缘追踪 + 微服务评估（10 人天）

### 血缘追踪

- [ ] 4.1 后端：`sql/mdm/init.sql` 新增动态数据表通用列说明（ALTER TABLE 所有 `mdm_data_*` 表新增 `source` VARCHAR(64)、`source_time` DATETIME 列）——模型发布时建表逻辑同步更新（工时：0.5 人天）
- [ ] 4.2 后端：`maintenance/service/impl/MdmDataServiceImpl.java` 增强录入/导入/API 推送——写入 `source` 字段（手动录入 = `MANUAL`，Excel 导入 = `IMPORT:<文件名>`，API 推送 = `API:<appid>`）+ `source_time`（工时：1 人天）
- [ ] 4.3 后端：新建 `lineage` 子包 —— `service/IMdmLineageService.java` + `impl/MdmLineageServiceImpl.java`（血缘查询：读取数据 `source` + `source_time` → 来源节点；查询 `mdm_distribution_record` WHERE data_id → 去向节点列表；返回 `{ source, targets[] }` 结构）（工时：1 人天）
- [ ] 4.4 后端：`lineage/controller/MdmLineageController.java`（REST 接口：`GET /mdm/lineage/{objectCode}/{dataId}`，返回血缘数据）（工时：0.5 人天）

### 前端

- [ ] 4.5 前端：`src/api/mdm/lineage.ts`（血缘接口封装）（工时：0.25 人天）
- [ ] 4.6 前端：`src/views/mdm/maintenance/index.vue` 数据详情页新增"血缘"Tab——ECharts 关系图（force-directed graph），中心节点为当前数据，左侧来源节点，右侧去向节点；点击节点可跳转数据详情；无去向时显示"暂无下游消费记录"（工时：2 人天）

### 微服务评估

- [ ] 4.7 文档：`doc/cloud-evaluation.md`（不写代码，交付评估报告，覆盖：现状分析/目标架构/迁移范围/迁移成本/收益分析/风险点/建议时间线）（工时：1.5 人天）

## 第 5 周：联调与集成测试（10 人天）

- [ ] 5.1 后端：端到端联调——关系建模 + 模板库（创建客户模板 → 一键创建 → 配置引用关系 → 录入关联数据 → 验证级联删除）（工时：1.5 人天）
- [ ] 5.2 后端：端到端联调——Flowable 审核（设计流程 → 部署 → 绑定对象 → 提交审核 → 多级审批 → 会签 → 通过/驳回/退回 → 验证数据状态回写）（工时：2 人天）
- [ ] 5.3 后端：端到端联调——RabbitMQ 分发（启动 RabbitMQ → 配置 MQ 通道分发 → 变更数据 → 验证消息投递 → 死信重试 → 分发记录）（工时：1.5 人天）
- [ ] 5.4 前端：端到端联调——大屏数据渲染（验证各面板数据准确性 + 刷新机制）（工时：1 人天）
- [ ] 5.5 前端：端到端联调——血缘追踪（录入数据 → 分发 → 验证血缘图节点和连线）（工时：1 人天）
- [ ] 5.6 前端：端到端联调——流程设计器（bpmn-js 拖拽 → 保存 → 部署 → 验证流程实例运行）（工时：1.5 人天）
- [ ] 5.7 全链路：关系建模 → 模板建对象 → 编码生成 → 审核 → 质量校验 → MQ 分发 → 大屏展示 → 血缘追溯（工时：1.5 人天）

## 第 6 周：落地、文档与 1.1.0 发布（10 人天）

- [ ] 6.1 后端：`sql/mdm/init.sql` 新增 1.1.0 菜单与权限（关系管理/流程设计/模板库/大屏/分发监控 + 按钮权限）（工时：0.5 人天）
- [ ] 6.2 后端：`sql/mdm/init.sql` 新增种子数据——`mdm_data_type` 字典新增"引用"类型（ref）；`mdm_relation.cascade_rule` 字典类型（RESTRICT/SET_NULL/CASCADE）（工时：0.5 人天）
- [ ] 6.3 后端：`sql/mdm/templates.sql` 编写 5 个模板种子 INSERT 语句（客户/供应商/物料/组织/人员的完整配置）（工时：1.5 人天）
- [ ] 6.4 前端：`src/views/mdm/` 新增菜单路由注册（关联关系/模板库/大屏/流程设计/分发监控 页面路由）（工时：0.5 人天）
- [ ] 6.5 文档：更新 `doc/main-data-platform-requirements.md`（1.1.0 新增能力说明）（工时：0.5 人天）
- [ ] 6.6 文档：更新 `doc/deploy-architecture.md`（新增 RabbitMQ 服务，更新部署架构图）（工时：0.5 人天）
- [ ] 6.7 文档：更新 `doc/operations.md`（Flowable 流程运维、RabbitMQ 队列监控、大屏使用说明）（工时：0.5 人天）
- [ ] 6.8 文档：编写 `doc/template-guide.md`（模板使用指南：如何从模板创建对象及自定义修改）（工时：0.5 人天）
- [ ] 6.9 文档：编写 `doc/lineage-guide.md`（血缘追踪说明：来源标记规则、血缘图解读）（工时：0.5 人天）
- [ ] 6.10 更新 `scripts/smoke-mdm.sh`（新增 1.1.0 冒烟用例：关系建模/Flowable 审核/MQ 分发/大屏/血缘/模板创建）（工时：1 人天）
- [ ] 6.11 更新版本号与文档：`mdm-backend/pom.xml` 版本号 → 1.1.0；`doc/RELEASES.md` 新增 1.1.0 发布说明（工时：0.5 人天）
- [ ] 6.12 发布检查清单与回滚演练（工时：1 人天）
- [ ] 6.13 版本 1.1.0 发布：打 tag v1.1.0、更新版本号、发布说明、部署上线（工时：1 人天）

## 里程碑安排

| 里程碑 | 时间点 | 交付内容 |
|--------|--------|----------|
| 关系建模 + 模板库 | 第 1 周末 | 关联关系配置与引用 + 5 模板一键创建可用 |
| Flowable 审核 | 第 2 周末 | 流程设计器 + 多级/会签审核全链路可用 |
| MQ 分发 + 大屏 | 第 3 周末 | 双通道分发 + 质量大屏 5 面板可用 |
| 血缘 + 评估 | 第 4 周末 | 血缘追踪可视化 + 微服务评估报告 |
| 联调完成 | 第 5 周末 | 全链路打通，冒烟通过 |
| 版本 1.1.0 发布 | 第 6 周末 | 7 项能力交付 + 文档齐备 + 部署上线 |

## 依赖关系

```
0.1-0.5 前置准备
   ├── 1.1-1.11 关系建模（后端）
   │     ├── 1.12-1.15 关系建模（前端）
   │     ├── 1.16-1.17 模板库（后端）
   │     │     └── 1.18-1.19 模板库（前端）
   │     └── 2.1-2.8 Flowable 审核（后端）
   │           └── 2.9-2.13 Flowable 审核（前端）
   ├── 3.1-3.8 RabbitMQ 分发（后端）
   │     └── 3.9-3.11 RabbitMQ 分发（前端）
   ├── 3.12-3.13 质量大屏（后端）
   │     └── 3.14-3.15 质量大屏（前端）
   ├── 4.1-4.4 血缘追踪（后端）
   │     └── 4.5-4.6 血缘追踪（前端）
   └── 4.7 微服务评估（独立）
```

## 关键风险

| 风险 | 缓解 |
|------|------|
| Flowable 6.x 与 Spring Boot 2.5.15 兼容性 | 0.1 前置验证，官方文档确认兼容 |
| bpmn-js 前端集成复杂度 | 预设 3 个模板降低门槛，按需进阶 |
| RabbitMQ 部署增加运维负担 | docker-compose 预置，开箱即用 |
| N:M 中间表动态 DDL 事务一致性 | 与模型发布 DDL 同事务，失败回滚并标记发布失败 |
| 大屏聚合查询跨多张动态表性能 | 第一期数据量小直接查；ponytail: 加缓存或预聚合表当单对象超 10 万条 |