## Why

企业内部的主数据（客户、供应商、物料、组织、人员等）分散在 ERP、CRM、HR 等多个业务系统中，缺乏统一的标准定义与管理手段，导致数据不一致、重复录入、质量参差不齐。需要建设统一的主数据管理平台（MDM），实现主数据的**标准统一、集中管理、质量保障、共享分发**，为各业务系统提供权威数据源，从源头治理数据资产。

## What Changes

基于若依（RuoYi）前后端分离版（后端 Spring Boot 2.5.15 / JDK8，前端 Vue3 + TypeScript）搭建主数据管理平台，在若依系统管理底座之上新增六大主数据能力：

- **主数据模型管理**：数据对象（实体）、属性、分类的元数据建模，模型发布后驱动维护功能生成
- **编码规则管理**：编码方案与分段规则配置，编码自动生成
- **数据标准管理**：标准字典、值域定义与录入校验
- **主数据维护与审核**：数据增删改查、生命周期状态流转、提交审核流程
- **数据质量管理**：校验规则、重复检测与质量台账
- **数据分发与集成**：分发订阅、Excel 批量导入导出、对外数据接口

**BREAKING**：无——全新平台，不影响现有若依功能。

## Capabilities

### New Capabilities

- `mdm/model-management`: 主数据模型管理——数据对象/实体、属性、分类的元数据建模与管理
- `mdm/code-rule`: 主数据编码规则管理——编码方案、分段规则、流水号生成
- `mdm/standard`: 数据标准管理——标准字典、值域定义及数据校验
- `mdm/maintenance`: 主数据维护与审核——数据增删改查、状态流转、提交与审核流程
- `mdm/data-quality`: 数据质量管理——校验规则、重复检测、质量台账
- `mdm/distribution`: 数据分发与集成——分发订阅、Excel 导入导出、对外数据 API

### Modified Capabilities

无。全新平台，不修改现有若依 spec。

## Impact

- **后端**（mdm-backend）：新增 `ruoyi-mdm` 独立 Maven 模块（与 ruoyi-system 平级，model、coderule、standard、maintenance、quality、distribution 六个子包）；新增 `mdm_*` 系列数据表；复用若依 ruoyi-system 的 RBAC、字典、参数、日志
- **前端**（mdm-frontend）：新增 mdm 菜单与页面（模型管理、编码规则、数据标准、数据维护、数据质量、数据分发），复用若依布局与组件
- **API**：新增 `/mdm/**` 系列 REST 接口，遵循若依统一响应体 `AjaxResult` 与分页 `TableDataInfo`
- **依赖**：沿用 MySQL + Redis，不引入新中间件；授权沿用若依 JWT + RBAC
- **文档**：doc/ 下补充主数据平台需求、部署与运维文档
