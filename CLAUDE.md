# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

基于若依 RuoYi-Vue 3.9.2 前后端分离版的主数据管理平台（MDM）。当前代码为若依原版，MDM 业务尚未实现，按 OpenSpec 计划推进（见 `openspec/changes/mdm-platform-init`，六大能力：模型管理、编码规则、数据标准、维护审核、数据质量、数据分发）。

## 常用命令

### 后端（mdm-backend/，Spring Boot 2.5.15 / Java 8）
- 构建：`mvn clean package -DskipTests`（项目无单元测试）
- 启动：`cd ruoyi-admin && mvn spring-boot:run`（端口 8080），或 `java -jar ruoyi-admin/target/ruoyi-admin.jar`
- 依赖：MySQL 库 `mdm`（初始化 `sql/ry_20260417.sql` + `sql/quartz.sql` + `sql/mdm/init.sql`）+ Redis，连接配置在 `ruoyi-admin/src/main/resources/application-druid.yml` 与 `application.yml`

### 前端（mdm-frontend/，Vue 3.5 + TS + Vite 6）
- 安装：`npm install`（Node 22，`.nvmrc` 已指定）
- 开发：`npm run dev`
- 构建：`npm run build:prod`（产物 `dist/`）
- 无 lint/test 脚本
- 后端地址：开发/生产分别配置于 `.env.development` / `.env.production` 的 `VITE_APP_BASE_API`

## 架构

### 后端
Maven 多模块：
- `ruoyi-admin`：启动模块，集中配置（数据源、Redis、日志）
- `ruoyi-framework`：Spring Security + JWT 认证授权、全局异常与响应处理
- `ruoyi-system`：RBAC 系统管理（用户/角色/菜单/字典/参数/操作日志）
- `ruoyi-common`、`ruoyi-quartz`（定时任务）、`ruoyi-generator`（代码生成）

MDM 业务规划新增 `ruoyi-modules/mdm` 独立模块（当前不存在），通过 ruoyi-admin 依赖加载。

### 前端
- `src/api/`：按业务模块封装接口；请求封装在 `src/utils/request.ts`（统一响应体 `AjaxResult`、分页 `TableDataInfo`）
- `src/views/`：页面；`src/router/` + `src/permission.ts`：动态路由与权限控制
- `src/store/`：Pinia 状态；`src/directive/`：权限指令（v-hasPermi 等）

### 数据与中间件
- MySQL 库 `mdm`：`sys_*` 若依系统表 + `mdm_*` 主数据表
- Redis：验证码、Token 校验、缓存；编码流水号将基于 Redis INCR
- 部署架构见 `doc/deploy-architecture.md`（Nginx + 单体后端 + MySQL + Redis）

## 开发约定

- 需求与任务走 OpenSpec 工作流（Node 22 下 `openspec` CLI）：开发计划在 `openspec/changes/mdm-platform-init/tasks.md`，用 `/opsx:*` 系列命令推进（propose / apply / update / archive）
- 新增接口遵循若依风格：响应体 `AjaxResult`、分页 `TableDataInfo`、MyBatis 注解 SQL
- 新增菜单与权限需在 SQL 脚本中登记，前端动态菜单依赖 sys_menu 数据
- 默认账号 admin / admin123；新增对外接口鉴权规划见 design.md 决策七（应用凭证 appid/secret）
