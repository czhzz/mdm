# 版本 1.0.0 发布检查清单与回滚演练

> 周任务 9.5/9.6。目标：验证 mdm 六能力可用、若依原功能不受影响，可一键回滚。

## 一、发布前检查清单

- [ ] 后端 `mvn clean package -DskipTests` 通过（Dockerfile 内已执行）
- [ ] 前端 `npm run build:prod` 通过
- [ ] `init.sql` 可重复执行（drop+create mdm 表、delete+insert 菜单/字典）
- [ ] `seed-demo.sql` 可重复执行（客户/供应商示例对象就绪）
- [ ] 端到端冒烟通过：`scripts/smoke-mdm.sh` 全绿
  - 模型发布 → 编码生成 → 数据维护 → 审核（提交/通过/驳回）→ 质量（规则/查重/台账）→ 分发（凭证/配置/推送/重推/记录）
  - 对外接口：正确签名返回 200，无效凭证返回 HTTP 401
- [ ] 关键页可访问：模型/编码/标准/维护/审核/质量/分发（admin 全部菜单 2000-2035）
- [ ] 版本号已更新：`mdm-backend/pom.xml` 与各模块 `1.0.0`、`mdm-frontend/package.json` `1.0.0`

## 二、发布操作

```bash
# 1. 构建启动
docker compose build && docker compose up -d
# 2. 初始化/核对数据库（幂等）
docker exec -i mdm-mysql mysql -uroot -p<password> mdm < mdm-backend/sql/mdm/init.sql
docker exec -i mdm-mysql mysql -uroot -p<password> mdm < mdm-backend/sql/mdm/seed-demo.sql
# 3. 冒烟
bash scripts/smoke-mdm.sh http://localhost:8080
# 4. 打标签
git tag -a v1.0.0 -m "主数据管理平台 1.0.0 发布"
```

## 三、回滚演练

**回滚目标**：停用 mdm，若依原功能（登录/系统管理/监控）完全不受影响。

### 方案 A：仅停用 mdm（保留部署，最快）
1. 前端：删除 mdm 菜单（`sys_menu` 2000-2035），或停用「主数据管理」目录 → 侧边栏消失（admin 直查 Redis 权限缓存 `login_tokens` 后刷新即可）。
2. 后端：不重启服务即生效；如经网关关闭 `/mdm/**` 与 `/open/**` 路由更彻底（当前单体内嵌，无独立网关时跳过）。
3. 若依系统表 `sys_*` 不依赖任何 `mdm_*` 表，删除 `mdm_*` 表不影响若依 —— 验证：`SHOW TABLES` 中若依表完好，登录/字典/用户管理均正常。

### 方案 B：镜像回滚（整体退回上一版本）
1. `docker compose stop backend frontend`
2. 恢复上一发布镜像：`docker tag <旧镜像> mdm-backend:latest && docker compose up -d backend frontend`
3. 若已变更数据库，按「二、数据库初始化」重跑旧版本对应的 `init.sql`（幂等脚本反向兼容）。

### 演练验收
- [ ] 删除/停用 mdm 菜单后，admin 登录、系统管理、监控页面正常
- [ ] 若依原有 `sys_user/sys_menu/sys_role` 数据未被动过
- [ ] 回滚后 `mdm` 相关页面不可见、接口 404/403，核心服务无报错
- [ ] 编码流水（Redis）清理后重新生成无冲突（唯一索引兜底）

## 四、发布说明条目（RELEASES.md）

- 六大能力：模型管理、编码规则、数据标准、数据维护与审核、数据质量、数据分发与集成
- 示例对象：客户、供应商（编码方案 + 审核流程 + 分发订阅可演示）
- 对外接口：`/open/mdm/data`、`/open/mdm/distribution/confirm`（appid + HMAC-SHA256 签名鉴权）
- 兼容性：若依 3.9.2 底座，新增 mdm 表与菜单，不影响原有功能