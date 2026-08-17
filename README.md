# 主数据管理平台（MDM）

基于若依（RuoYi-Vue 3.9.2）前后端分离版搭建的企业主数据管理平台，统一管理客户、供应商、物料、组织、人员等主数据，实现主数据的**标准统一、集中管理、质量保障、共享分发**，为各业务系统提供权威数据源。

> 当前处于开发阶段，已实现模型管理基础能力（数据对象/分类/属性），后续能力按 [OpenSpec 开发计划](openspec/changes/mdm-platform-init/tasks.md) 推进。

## 功能模块

| 模块 | 说明 |
|------|------|
| 模型管理 | 数据对象、属性、分类的元数据建模（已实现 CRUD） |
| 编码规则 | 编码方案、分段规则、流水号生成 |
| 数据标准 | 标准字典、值域定义与校验 |
| 数据维护 | 主数据增删改查、状态流转、审核流程 |
| 数据质量 | 校验规则、重复检测、质量台账 |
| 数据分发 | 分发订阅、Excel 导入导出、对外 API |

## 技术栈

- **后端**：Spring Boot 2.5.15 / Java 8、Spring Security + JWT、MyBatis、MySQL、Redis
- **前端**：Vue 3.5 + TypeScript + Vite 6 + Element Plus + Pinia

## 目录结构

```
mdm/
├── mdm-backend/     # 后端（若依单体多模块）
│   ├── ruoyi-admin/     # 启动模块（端口 8080）
│   ├── ruoyi-framework/ # 安全框架
│   ├── ruoyi-system/    # RBAC 系统管理
│   ├── ruoyi-mdm/       # 主数据业务模块
│   ├── ruoyi-common/    # 通用工具
│   ├── ruoyi-quartz/    # 定时任务
│   ├── ruoyi-generator/ # 代码生成
│   └── sql/             # 数据库脚本
├── mdm-frontend/    # 前端（Vue3 + TS）
├── docker-compose.yml   # Docker 一键部署
├── doc/                 # 部署架构文档
└── openspec/            # OpenSpec 需求与开发计划
```

## 快速启动

### 方式一：Docker Compose 一键启动（推荐）

环境要求：Docker 24+（含 Compose v2）。

```bash
# 1. 构建并启动全部服务（MySQL/Redis/后端/前端）
docker compose up -d --build

# 2. 等待后端就绪后访问
#    前端：http://localhost
#    后端接口：http://localhost:8080
```

说明：
- 首次启动 MySQL 会自动执行 `sql/` 下的初始化脚本（建库、系统表、MDM 表、菜单权限）
- 端口映射：前端 80、后端 8080、MySQL 3306、Redis 6379（如本机端口占用，可在 `docker-compose.yml` 中调整）

停止与清理：

```bash
docker compose down          # 停止服务
docker compose down -v       # 停止并删除数据卷（含数据库数据，慎用）
```

### 方式二：本地手动启动

环境要求：JDK 8+、Maven 3.6+、Node 22（`.nvmrc`）、MySQL 5.7/8.0、Redis 5+。

**1. 初始化数据库**

```bash
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS \`mdm\` DEFAULT CHARACTER SET utf8mb4"
mysql -uroot -p mdm < mdm-backend/sql/ry_20260417.sql   # 若依主库
mysql -uroot -p mdm < mdm-backend/sql/quartz.sql         # 定时任务
mysql -uroot -p mdm < mdm-backend/sql/mdm/init.sql       # MDM 元数据与菜单
```

**2. 启动后端（端口 8080）**

```bash
cd mdm-backend
# 确认 MySQL/Redis 连接配置（默认 localhost，可用环境变量覆盖）
#   MYSQL_HOST / MYSQL_PORT / MYSQL_USERNAME / MYSQL_PASSWORD / REDIS_HOST / REDIS_PORT
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar
# 或开发模式：cd ruoyi-admin && mvn spring-boot:run
```

**3. 启动前端（端口 80/5173）**

```bash
cd mdm-frontend
npm install
npm run dev        # 开发模式，http://localhost:5173（自带 /dev-api 代理到 8080）
npm run build:prod # 生产构建，产物 dist/（配合 Nginx 部署，见 doc/deploy-architecture.md）
```

## 默认账号

| 账号 | 密码 | 权限 |
|------|------|------|
| admin | admin123 | 超级管理员 |

## 端口规划

| 服务 | 端口 |
|------|------|
| 前端（Nginx / Vite dev） | 80 / 5173 |
| 后端（ruoyi-admin） | 8080 |
| MySQL | 3306 |
| Redis | 6379 |

## 相关文档

- [部署架构说明](doc/deploy-architecture.md)：Nginx + 单体后端 + MySQL + Redis 生产部署
- [OpenSpec 开发计划](openspec/changes/mdm-platform-init/tasks.md)：需求规格、设计决策、8 周开发计划（用 `/opsx:*` 命令推进）
- [CLAUDE.md](CLAUDE.md)：开发约定与常用命令
