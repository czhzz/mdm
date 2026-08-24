# 主数据管理平台（MDM）部署架构说明

基于若依（RuoYi）前后端分离版搭建的主数据管理平台。

## 1. 项目结构

```
mdm/
├── mdm-backend/   # 后端源码（RuoYi-Vue，springboot2 分支）
├── mdm-frontend/  # 前端源码（RuoYi-Vue3，typescript 分支，GitCode）
└── doc/           # 部署架构与说明文档
```

## 2. 技术栈

### 后端（mdm-backend）
| 组件 | 版本 |
|------|------|
| Spring Boot | 2.5.15 |
| Java | 1.8+ |
| 若依版本 | 3.9.2 |
| 安全框架 | Spring Security + JWT |
| 持久层 | MyBatis + Druid |
| 任务调度 | Quartz |
| 工作流引擎 | Flowable 6.8.1（1.1.0） |
| 消息队列 | RabbitMQ + Spring AMQP（1.1.0） |

后端模块：
- `ruoyi-admin`：Web 启动模块
- `ruoyi-framework`：框架核心
- `ruoyi-system`：系统业务（用户、角色、菜单等）
- `ruoyi-common`：通用工具
- `ruoyi-quartz`：定时任务
- `ruoyi-generator`：代码生成
- `ruoyi-mdm`：主数据管理（model/coderule/standard/maintenance/quality/distribution/relation/audit/lineage/template）

### 前端（mdm-frontend）
| 组件 | 版本 |
|------|------|
| Vue | 3.5.26 |
| TypeScript | 5.6.3 |
| Vite | 6.4.1 |
| Element Plus | 2.13.1 |
| Pinia | 3.0.4 |
| Vue Router | 4.6.4 |

## 3. 部署架构

```
┌─────────────┐       ┌──────────────────────────┐
│   浏览器     │──────▶│  Nginx（静态资源+反向代理） │
└─────────────┘       └────────────┬─────────────┘
                                   │ /api、/prod-api
                                   ▼
                        ┌──────────────────────┐
                        │  RuoYi 单体后端 (8080) │
                        │  ruoyi-admin         │
                        └──────┬───────┬───────┘
                               │       │
                               ▼       ▼
                        ┌─────────┐ ┌───────┐
                        │  MySQL  │ │ Redis │
                        │ 5.7/8.0 │ │  5+   │
                        └─────────┘ └───────┘
```

- 单体架构：后端单应用启动（`ruoyi-admin`），前端构建产物由 Nginx 托管
- 认证方式：JWT Token，前端存 Cookie
- 缓存：Redis（验证码、Token 校验、Session）
- 数据库：MySQL（主库 + quartz 库）

## 4. 环境要求

| 组件 | 版本要求 |
|------|----------|
| JDK | 1.8+ |
| Maven | 3.6+ |
| Node.js | 16+（Vite 6 建议 18+/20+） |
| MySQL | 5.7 / 8.0 |
| Redis | 5.0+ |
| RabbitMQ | 3.9+（管理插件，1.1.0） |
| Nginx | 任意稳定版 |

## 5. 部署步骤

### 5.1 数据库初始化
```sql
-- 创建库
CREATE DATABASE IF NOT EXISTS `mdm` DEFAULT CHARACTER SET utf8mb4;
-- 导入主库脚本（含默认数据）
mysql -uroot -p mdm < sql/ry_20260417.sql
-- 导入定时任务库脚本
mysql -uroot -p mdm < sql/quartz.sql
-- 导入 MDM 初始化脚本
mysql -uroot -p mdm < sql/mdm/init.sql
```

### 5.2 后端启动
```bash
cd mdm-backend
# 修改 ruoyi-admin/src/main/resources/application-druid.yml 中的 MySQL 连接信息
# 修改 application.yml 中的 Redis 连接信息
mvn clean package -DskipTests
cd ruoyi-admin/target
nohup java -jar ruoyi-admin.jar > ruoyi.log 2>&1 &
# 默认端口 8080
```

### 5.3 前端构建部署
```bash
cd mdm-frontend
# 修改 .env.development / .env.production 中的 VITE_APP_BASE_API 指向后端地址
npm install
npm run build:prod   # 产物在 dist/
# 将 dist/ 部署到 Nginx html 目录
```

### 5.4 Nginx 配置示例
```nginx
server {
    listen       80;
    server_name  localhost;

    location / {
        root   /usr/share/nginx/html;
        index  index.html;
        try_files $uri $uri/ /index.html;   # 前端路由 history 模式
    }

    location /prod-api/ {
        proxy_pass http://127.0.0.1:8080/;  # 反向代理后端
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

## 6. 默认账号

| 账号 | 密码 | 权限 |
|------|------|------|
| admin | admin123 | 超级管理员 |
| ry | admin123 | 普通用户 |

## 7. 端口规划

| 服务 | 端口 |
|------|------|
| 后端（ruoyi-admin） | 8080 |
| 前端（Nginx） | 80 |
| MySQL | 3306 |
| Redis | 6379 |
| RabbitMQ | 5672（AMQP）/ 15672（管理台） |
