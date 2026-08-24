# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

基于若依 RuoYi-Vue 3.9.2 前后端分离版的主数据管理平台（MDM），版本 1.0.0。六大能力已全部实现：模型管理、编码规则、数据标准、维护审核、数据质量、数据分发。开发计划见 `openspec/changes/mdm-platform-init/tasks.md`（8 周任务全部完成）。

## 常用命令

### 后端（mdm-backend/，Spring Boot 2.5.15 / Java 8 / Maven）
- 构建：`cd mdm-backend && mvn clean package -DskipTests`
- 启动：`cd mdm-backend/ruoyi-admin && mvn spring-boot:run`（端口 8080），或 `java -jar mdm-backend/ruoyi-admin/target/ruoyi-admin.jar`
- 依赖：MySQL 库 `mdm`（初始化 `mdm-backend/sql/ry_20260417.sql` + `sql/quartz.sql` + `sql/mdm/init.sql`）+ Redis
- 连接配置：`mdm-backend/ruoyi-admin/src/main/resources/application-druid.yml` 与 `application.yml`，占位符从 `.env` 读取

### 前端（mdm-frontend/，Vue 3.5 + TS + Vite 6）
- 安装：`cd mdm-frontend && npm install`（Node 22，根目录 `.nvmrc` 已指定）
- 开发：`cd mdm-frontend && npm run dev`（端口从 `.env` 的 `FRONTEND_PORT` 读取）
- 构建：`cd mdm-frontend && npm run build:prod`（产物 `dist/`）
- 无 lint/test 脚本
- 后端地址：`.env.development` / `.env.production` 的 `VITE_APP_BASE_API`

### Docker（根目录 docker-compose.yml）
- 启动全套：`docker compose up -d`（mysql + redis + backend + frontend，端口从 `.env` 读取）
- 停止：`docker compose down`
- 初始化：首次启动自动执行 SQL 脚本（挂载到 mysql 容器的 `/docker-entrypoint-initdb.d/`）

### 环境变量（`.env`，从 `.env.example` 复制）
- 端口：`SERVER_PORT`（后端 8080）、`FRONTEND_PORT`（前端 80）
- 数据源：`MYSQL_HOST/PORT/DATABASE/USERNAME/PASSWORD`
- Redis：`REDIS_HOST/PORT/PASSWORD`（留空 = 无密码）
- 其他：`DRUID_USERNAME/PASSWORD`、`DRUID_MONITOR_ENABLED`、`TOKEN_SECRET`、`SWAGGER_ENABLED`
- 优先级：环境变量 > `application-{profile}.yml` > `application.yml`
- `.env` 已被 `.gitignore` 忽略，勿提交真实密码

## 架构

### 后端（Maven 多模块，父 POM: `com.ruoyi:mdm:1.0.0`）
- `ruoyi-admin`：启动模块，集中配置（数据源、Redis、日志）
- `ruoyi-framework`：Spring Security + JWT 认证授权、全局异常与响应处理
- `ruoyi-system`：RBAC 系统管理（用户/角色/菜单/字典/参数/操作日志）
- `ruoyi-mdm`：MDM 业务模块，6 个子包 —— `model`（模型管理）、`coderule`（编码规则）、`standard`（数据标准）、`maintenance`（动态数据维护）、`quality`（数据质量）、`distribution`（数据分发）
- `ruoyi-common`、`ruoyi-quartz`（定时任务）、`ruoyi-generator`（代码生成）

### 前端
- `src/api/`：按业务模块封装接口（`mdm/`、`system/`、`monitor/`、`tool/`）；请求封装 `src/utils/request.ts`（统一响应体 `AjaxResult`、分页 `TableDataInfo`）
- `src/views/`：页面（`mdm/` 为主数据业务，`system/` 为系统管理）；`src/router/` + `src/permission.ts`：动态路由与权限控制
- `src/store/`：Pinia 状态；`src/directive/`：权限指令（v-hasPermi 等）

### 数据与中间件
- MySQL 库 `mdm`：`sys_*` 若依系统表 + `mdm_*` 主数据表（元数据 object/attribute/category、编码规则 code_rule、字典 dict、审核 audit_task、分发 app/record、动态数据表 `mdm_data_<obj_code>`）
- Redis：验证码、Token 校验、缓存；编码流水号基于 Redis INCR
- 部署架构见 `doc/deploy-architecture.md`（Nginx + 单体后端 + MySQL + Redis）

## 文档

- `doc/main-data-platform-requirements.md`：需求文档
- `doc/deploy-architecture.md`：部署架构
- `doc/operations.md`：运维手册
- `doc/demo-script.md`：演示脚本
- `doc/RELEASES.md` + `doc/release-checklist.md`：发布记录与检查清单
- `scripts/smoke-mdm.sh`：冒烟测试脚本（22 项）

## 开发约定

- 新增接口遵循若依风格：响应体 `AjaxResult`、分页 `TableDataInfo`、MyBatis 注解 SQL
- 新增菜单与权限需在 SQL 脚本中登记，前端动态菜单依赖 sys_menu 数据
- 默认账号 admin / admin123
- 对外接口鉴权：应用凭证 appid/secret（`mdm_app` 表管理）
- .env 新增变量需同步更新 `.env.example`（模板，不含真实值）