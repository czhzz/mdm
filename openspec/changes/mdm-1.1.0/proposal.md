## Why

主数据管理平台 1.0.0 已落地六大核心能力并通过冒烟验证。但作为企业级主数据平台，仍缺少以下关键能力：

1. **对象孤立**：客户、供应商、物料等主数据对象之间没有关联关系，现实业务中它们天然关联（客户属于分类、物料由供应商供货、组织包含人员）
2. **审核流程简单**：当前仅支持单节点"提交→通过/驳回"，无法满足多级审批、会签等企业实际需求
3. **分发通道单一**：HTTP 推送仅靠线程池，可靠性不足，订阅方无法选择更适合的异步消息队列方式
4. **缺乏可视化**：数据质量有台账但无趋势分析与大屏展示，管理层无法直观了解数据治理成效
5. **缺乏血缘追踪**：数据从哪来、推送到哪去，无法直观追溯
6. **缺少模板库**：每新建一个对象类型都要从头配置模型、编码、校验、字典，缺乏开箱即用的标准模板
7. **微服务演进方向不明**：是否需要以及何时迁移 RuoYi-Cloud 架构尚无评估

1.1.0 旨在补齐上述能力，推动 MDM 从"能用"走向"好用"。

## What Changes

基于 1.0.0 增量升级，分两条轨并行推进：

### 轨一：业务功能（6 项）

- **mdm/relation-modeling**：对象关系建模——新增 `mdm_relation` 关系表，支持 1:1/1:N/N:M 关联关系与级联规则，前端动态渲染关联组件
- **mdm/audit-flowable**：审核流程升级——引入 Flowable 6.x 流程引擎，废弃轻量审核表，前端集成 BPMN 流程设计器（bpmn-js），支持会签/转办/退回
- **mdm/distribution-mq**：RabbitMQ 分发——引入 RabbitMQ + Spring AMQP，HTTP 推送与 MQ 双通道共存，订阅方配置时选择分发方式
- **mdm/quality-dashboard**：数据质量大屏——ECharts 单页全屏大屏（质量趋势、对象健康度排行、规则命中分布、最近问题列表）
- **mdm/data-lineage**：主数据血缘追踪——基于审计字段 + 分发记录，数据详情页血缘 Tab，ECharts 关系图展示来源→数据→去向
- **mdm/object-templates**：对象模板库——5 个预置模板（客户/供应商/物料/组织/人员），一键创建完整对象配置（模型+编码+校验+字典+查重）

### 轨二：独立评估（1 项）

- **mdm/cloud-evaluation**：RuoYi-Cloud 微服务评估报告——迁移成本、收益、风险、建议时间线

### 废弃

- `mdm_audit_flow` 表：Flowable 流程定义替换
- `mdm_audit_task` 表：Flowable 运行时任务替换
- 分发线程池：MQ 通道替换线程池推送（HTTP 通道保留异步线程池）

### 新增依赖

- Flowable 6.x（Spring Boot 2.5.15 兼容）
- RabbitMQ + Spring AMQP
- 前端：bpmn-js（流程设计器）

**BREAKING**：审核表 `mdm_audit_flow` / `mdm_audit_task` 废弃，1.0.0 的审核数据不再迁移，需重新配置 Flowable 审核流程。

## Capabilities

### New Capabilities

- `mdm/relation-modeling`: 主数据对象关系建模——关联关系定义、级联规则、动态渲染关联组件
- `mdm/audit-flowable`: 审核流程引擎——Flowable 集成、BPMN 流程设计器、多级/会签审核
- `mdm/distribution-mq`: RabbitMQ 分发通道——MQ 消息队列分发、双通道共存、死信队列与重试
- `mdm/quality-dashboard`: 数据质量大屏——质量趋势、对象健康度、规则命中分布、问题列表
- `mdm/data-lineage`: 主数据血缘追踪——来源追溯、去向可视化、血缘关系图
- `mdm/object-templates`: 对象模板库——预置模板、一键创建、模板预览
- `mdm/cloud-evaluation`: RuoYi-Cloud 微服务评估——迁移成本、收益、风险分析

### Modified Capabilities

- `mdm/maintenance`: 动态维护页面新增血缘 Tab、关联对象选择器（关系建模）
- `mdm/distribution`: 分发配置新增分发方式选择（HTTP/MQ），分发记录新增通道列
- `mdm/model-management`: 属性类型新增"引用"，对象模板来源标记

## Impact

- **后端**（mdm-backend）：新增 `ruoyi-mdm` 子包 `relation`（关系建模）；`audit` 子包重写（Flowable 替换）；`distribution` 子包增强（MQ 通道）；新增 Flowable、RabbitMQ 依赖
- **前端**（mdm-frontend）：新增关系管理、流程设计器、质量大屏、血缘追踪、模板库页面；维护页面增强（血缘 Tab、关联选择器）；分发配置增强（通道选择）
- **API**：新增 `/mdm/relation/**`、`/mdm/template/**` 接口；`/mdm/audit/**` 接口重写；`/mdm/distribution/**` 接口增强
- **中间件**：MySQL + Redis + RabbitMQ（新增）
- **文档**：doc/ 下新增血缘追踪说明、模板使用指南、微服务评估报告