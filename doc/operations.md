# 主数据管理平台 部署与运维手册

> 适用于 1.0.0。单体后端（ruoyi-admin 含 mdm 模块）+ MySQL 8 + Redis 7，Nginx 反代前端。架构详见 `deploy-architecture.md`。

## 一、快速启动（Docker Compose）

前置：Docker、Docker Compose，项目根 `.env`（模板见 `.env.example`）。

```bash
# 首次或代码变更后：构建镜像（后端镜像内执行 mvn package）
docker compose build backend

# 启动全部服务（mysql / redis / backend / frontend）
docker compose up -d

# 查看状态（全部 healthy 后可用）
docker compose ps

# 访问
#   前端：http://localhost:80
#   后端：http://localhost:8080
默认账号：admin / admin123
```

> MySQL 容器首次启动自动执行 `sql/ry_20260417.sql` + `sql/quartz.sql` + `sql/mdm/init.sql`（按名称排序）。
> 已有数据卷时不会自动重跑，需按下方「数据库初始化」手动执行。

## 二、数据库初始化与迁移

脚本均可重复执行（幂等）：

```bash
SQL=mdm-backend/sql/mdm
# 元数据/菜单/种子字典（drop+create mdm 表，会清空 mdm 业务数据）
docker exec -i mdm-mysql mysql -uroot -p<MYSQL_PASSWORD> mdm < $SQL/init.sql
# 示例对象（客户/供应商，含编码方案与审核流程）
docker exec -i mdm-mysql mysql -uroot -p<MYSQL_PASSWORD> mdm < $SQL/seed-demo.sql
```

变更约定：
- 新增表/字段：追加到 `init.sql`（保持 drop-table-if-exists 幂等模式）后手动执行。
- 示例数据：单独 `seed-demo.sql`，与交付数据解耦。
- 业务动态表 `mdm_data_<obj_code>` 由「模型发布」生成，勿手工修改结构；预留规则见设计决策六（仅允许新增可空/带默认值属性）。

## 三、数据备份

```bash
# 全量备份（库 mdm 含若依系统表 + 主数据表）
docker exec mdm-mysql mysqldump -uroot -p<password> --databases mdm --single-transaction > backup_mdm_$(date +%F).sql
```

恢复：`docker exec -i mdm-mysql mysql -uroot -p<password> < backup_mdm_*.sql`。
Redis 为缓存/编码流水号（`mdm:code:*`），丢失后重新生成编码即可，无需备份。

## 四、日志与问题排查

```bash
docker logs -f mdm-backend      # 后端日志（也落盘 ./logs）
docker logs -f mdm-frontend     # 前端 Nginx
docker exec mdm-mysql mysqladmin -uroot -p<password> ping   # DB 存活
```

常见问题：

| 现象 | 排查 |
|------|------|
| 登录页验证码报错 | Redis 未启动或密码与 `.env` 不一致（`REDIS_PASSWORD`） |
| 中文乱码 | 确认 MySQL 连接参数 `characterEncoding=utf8`（application-druid.yml），容器已设 utf8mb4 |
| 对象发布失败「数据表已存在」 | 曾发布过未停用，或手工建过 `mdm_data_*` 表；停用对象后清理该表再发布 |
| 分发记录一直失败 | 订阅方地址不可达/超时；改对地址后「重推」即可，主流程不受影响（异步推送） |
| 端口冲突 | 后端 8080 / 前端 80 可经 `.env`（`SERVER_PORT`）与 compose 端口映射调整 |

## 五、扩容与后续演进

- 分发推送当前为内嵌固定线程池（见 `DistributionServiceImpl`，标注 `ponytail`），订阅方增多时按设计决策五引入 RabbitMQ。
- 若依原功能与 mdm 完全隔离：停用对应菜单/`/mdm/**` 路由、删除 mdm 表即可回退，不影响 `sys_*` 与若依业务（见 release-checklist 回滚演练）。