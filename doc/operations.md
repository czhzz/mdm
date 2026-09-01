# 主数据管理平台 部署与运维手册

> 适用于 1.2.0。单体后端（ruoyi-admin 含 mdm 模块）+ MySQL 8 + Redis 7 + RabbitMQ 3 + Flowable 6.8.1，Nginx 反代前端。架构详见 `deploy-architecture.md`。

## 一、快速启动（Docker Compose）

前置：Docker、Docker Compose，项目根 `.env`（模板见 `.env.example`）。

```bash
# 首次或代码变更后：构建镜像（后端镜像内执行 mvn package）
docker compose build backend

# 启动全部服务（mysql / redis / rabbitmq / backend / frontend）
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

## 八、1.1.0 新增组件运维

### 8.1 RabbitMQ

- 管理台：http://localhost:15672（默认 guest/guest，生产务必改密码并关闭 guest）
- 分发交换机：`mdm.distribution`（topic），死信队列 `mdm.distribution.dlx.queue`
- 死信积压告警：积压 > 100 建议检查订阅方消费状态；分发监控页可看实时数据
- 订阅方队列：分发配置保存时自动声明（名称可自定义，默认 `mdm.dist.<对象编码>`）
- 监控命令：

```bash
docker exec mdm-rabbitmq rabbitmqctl list_queues name messages
docker exec mdm-rabbitmq rabbitmqctl list_exchanges
```

### 8.2 Flowable 流程引擎

- ACT_* 表由引擎自动创建（`flowable.database-schema-update: true`），与 mdm 表同库
- 流程运维入口：主数据管理 → 流程管理（部署/挂起/激活/删除）；设计器三个预设模板
- 1.0.0 审核数据不迁移：旧表 `mdm_audit_flow`/`mdm_audit_task` 已废弃可归档
- 对象绑定流程：模型管理 → 对象编辑 → 审核流程下拉（未绑定的对象不做审核）
- 已知限制：退回简化实现为驳回；复杂退回需 Flowable 跳转 API（1.2.0）

### 8.3 存量对象血缘列补丁

1.1.0 之前发布的对象无 `source` 列，查看血缘前需手动补列（见 `lineage-guide.md`）。

### 8.4 升级顺序（1.0.0 → 1.1.0）

1. 停后端 → 备份数据库 → 重新执行 `sql/mdm/init.sql`（幂等，自动加列/加菜单/加字典）
2. `.env` 补齐 `RABBITMQ_*` 变量（模板见 `.env.example`）
3. `docker compose up -d`（自动拉起 rabbitmq 容器）
4. 流程管理页部署审核流程 → 对象绑定流程

## 九、1.2.0 集成管理运维

### 9.1 存量库升级（1.1.0 → 1.2.0）

数据分发升级为集成管理，涉及表重命名与菜单变更，存量库升级顺序：

```bash
SQL=mdm-backend/sql/mdm
# 停后端后执行增量迁移（幂等，可重复执行；内容含补列/菜单/新表/表重命名）
docker exec -i mdm-mysql mysql -uroot -p<MYSQL_PASSWORD> --default-character-set=utf8mb4 mdm < $SQL/upgrade-1.2.0.sql
docker compose up -d
```

迁移内容：

| 项 | 说明 |
|----|------|
| 表重命名 | `mdm_distribution` → `mdm_distribute_api`；`mdm_distribution_record` → `mdm_distribute_log`（新增 `app_code` 列，经 `mdm_app` 回填，历史数据无损） |
| 新表 | `mdm_receive_api` / `mdm_query_api`（接口配置）、`mdm_receive_log` / `mdm_query_log`（接口日志） |
| 菜单 | 新增「审核中心」（2046）与「集成管理」（2047-2070）；原分发菜单停用 |
| 补列 | `mdm_object` 幂等补 `audit_process_key` / `template_source`（防御） |

> 全新环境无需手动执行（`init.sql` 已含 1.2.0 全部结构）；仅 1.1.0 存量库需走上面迁移。

### 9.2 集成管理 5 菜单

| 菜单 | 作用 |
|------|------|
| 应用管理 | 接入方应用注册与凭证（appid/secret），停用即拒绝其所有对外调用；secret 重置后旧值立即失效 |
| 接收接口管理 | 配置对外接收接口（apiCode / 目标对象 / 启停），创建后外部系统可 `POST /open/integration/receive/{apiCode}` 上报数据 |
| 查询接口管理 | 配置对外查询接口，外部系统经 `POST /open/integration/query/{apiCode}` 条件查询主数据 |
| 分发管理 | 原分发配置（配置 / 监控两 Tab），HTTP 回调与 RabbitMQ 双通道，失败重推 |
| 集成日志 | 接收 / 查询 / 分发三 Tab 独立日志，支持按应用/对象/状态/时间检索与清理 |

### 9.3 对外接口（/open/integration/**）

- 鉴权：Header `X-App-Id` / `X-App-Secret`（对应应用管理的 appid/secret）；缺失或凭证无效返回 401。
- 接收（幂等）：`POST /open/integration/receive/{apiCode}`，Body `{ "dataCode": "<唯一键值>", "data": { ... } }`
  - `dataCode` 映射目标对象唯一属性（`unique_flag`/`primary_flag`），重复推送按 UPDATE 处理。
  - 柔性落库：目标对象绑定审核流程则自动提交审核（`source=API:<appCode>`），否则直接生效。
  - 目标对象未发布或接收接口已停用时调用报业务异常（500）。
- 查询：`POST /open/integration/query/{apiCode}`，Body `{ "filters": { "字段": 值 }, "pageNum": 1, "pageSize": 10 }`，返回 `{ total, rows }`。
- 验证示例：

```bash
curl -X POST http://localhost:8080/open/integration/receive/recv_demo \
  -H "X-App-Id: app_xxx" -H "X-App-Secret: sk_xxx" -H "Content-Type: application/json" \
  -d '{"dataCode":"SUP-2026-001","data":{"supplier_name":"示例","city":"杭州"}}'
```

### 9.4 日志保留与清理

- 三张日志表全量保留，页面「集成日志」按 type（receive/query/distribute）手动清理（`/mdm/integration/log/clean`，删除指定截止时间前记录）。
- 分发日志沿用原 record 三态（0 待发送 / 1 成功 / 2 失败），失败记录页面「重推」按日志 id 重放，payload 完整保留不截断。
- 无定时清理任务；日志量大时再按需增加 Quartz。

### 9.5 回滚（1.2.0 → 1.1.0）

1. 停后端，备份数据库。
2. 恢复 1.1.0 镜像与旧表名（`mdm_distribute_api` → `mdm_distribution`，`mdm_distribute_log` → `mdm_distribution_record` 反向 RENAME，删 1.2.0 新增表与菜单）。
3. 恢复前端旧目录（`views/mdm/distribution/` 已在 1.2.0 删除，需从版本库还原）。
4. 回滚为重大变更操作，执行前务必完整备份并在测试库演练。
