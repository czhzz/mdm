# RuoYi-Cloud 微服务迁移评估报告（1.1.0）

> 日期：2026-08-24　评估范围：主数据管理平台（MDM）1.1.0 → RuoYi-Cloud 微服务架构迁移可行性

## 一、现状分析

| 维度 | 现状 |
|------|------|
| 架构 | RuoYi-Vue 单体（Spring Boot 2.5.15 / JDK 8） |
| 模块 | Maven 多模块：ruoyi-admin（启动）/ framework / system / quartz / generator / common / **mdm** |
| MDM 边界 | `ruoyi-mdm` 已按业务域分包（model/coderule/standard/maintenance/quality/distribution/relation/audit/lineage/template），与 ruoyi-system 无代码耦合，仅依赖 RBAC 用户体系 |
| 中间件 | MySQL + Redis + RabbitMQ（1.1.0 新增）+ Flowable（1.1.0 新增，ACT_* 表） |
| 部署 | docker-compose 单体（mysql/redis/rabbitmq/backend/frontend 五容器） |

## 二、目标架构（RuoYi-Cloud）

```
ruoyi-gateway（网关：限流/路由/鉴权）
  ├── ruoyi-auth（认证中心：token 签发/校验）
  ├── ruoyi-system（系统服务：用户/角色/菜单/字典/参数）
  ├── ruoyi-mdm（主数据服务：MDM 六大能力 + 1.1.0 七项增强）
  ├── ruoyi-job（定时任务）
  └── ruoyi-file（文件服务，可选）
基础设施：Nacos（注册/配置中心）+ Sentinel（熔断限流）+ Feign（服务调用）
```

## 三、迁移范围

### 可直接搬迁（成本低）

- **`ruoyi-mdm` 模块**：包边界清晰，无跨模块内部调用，可直接升级为独立服务
  - 需改造点：
    - `SecurityUtils.getUsername()`（Token 上下文）→ 从网关透传的 header 获取用户信息
    - `ruoyi-framework` 的全局异常/RBAC 注解 → 迁移到网关或复制到 mdm 服务
    - `@PreAuthorize("@ss.hasPermi('mdm:*')")` 权限校验 → 由 mdm 服务持有权限数据（Feign 调 system 或本地缓存）
    - 菜单 sys_menu 写入 → 跨服务调用（Feign）或 mdm 自行维护菜单表

### 需要重构（成本中）

- **认证鉴权链**：JWT 签发移至 ruoyi-auth，mdm 服务做无状态校验
- **动态数据表 DDL**：mdm 服务独占 MySQL 库（或同库不同 schema），保持物理表方案不变
- **Flowable 引擎**：随 mdm 服务整体搬迁，ACT_* 表独立库，无分布式问题
- **RabbitMQ 分发**：本就是跨服务消息场景，微服务化后更自然

### 需要决策（成本高/风险高）

- **分布式事务**：模板一键创建（多表写入）目前靠本地事务；拆分后若 system 与 mdm 分离，菜单写入跨服务 → 需要最终一致性方案（MQ 补偿）或保留菜单在 mdm 库
- **服务间调用延迟**：动态 CRUD 高频查 sys_dict_data（值域校验）→ Feign 调用开销，建议本地缓存
- **运维复杂度**：从 5 容器 → 10+ 容器（Nacos/Sentinel/多服务实例）

## 四、迁移成本估算

| 项目 | 工作量（人天） | 说明 |
|------|--------------|------|
| 工程脚手架搭建 | 3 | 创建 mdm 服务骨架、网关/认证配置 |
| 认证鉴权改造 | 3 | 网关统一鉴权 + mdm 无状态化 |
| 跨服务调用改造 | 5 | 字典/菜单/用户信息 Feign 化 + 缓存 |
| 配置中心迁移 | 2 | application.yml → Nacos 配置 |
| 测试与回归 | 5 | 全链路冒烟（smoke-mdm.sh 扩展） |
| 部署与上线 | 2 | k8s/docker-compose 编排改造 |
| **合计** | **约 20 人天** | 约 3-4 周（2 人） |

## 五、收益分析

| 收益 | 说明 |
|------|------|
| 独立部署 | mdm 服务可独立发布，不重启若依系统管理功能 |
| 弹性伸缩 | 数据维护高峰期可独立扩容 mdm 实例 |
| 故障隔离 | mdm 服务异常不影响系统管理/其他业务 |
| 技术栈统一 | 若企业内部已用 RuoYi-Cloud，架构对齐便于团队协作 |

## 六、风险点

1. **分布式事务**（最高风险）：模板创建、审核落地等跨表写操作，微服务化后需引入补偿机制
2. **性能回退**：服务间调用网络开销，动态 CRUD 高频路径需缓存兜底
3. **运维复杂度上升**：Nacos/Sentinel 等组件引入，故障排查链路变长
4. **Flowable 与微服务**：流程引擎在单体服务内运行无问题；后续如多服务共享流程需评估 Flowable 多租户

## 七、建议时间线

| 阶段 | 时间 | 内容 | 前置条件 |
|------|------|------|----------|
| 阶段一：PoC | 1-2 周 | 脚手架 + mdm 服务剥离 + 网关连通性验证 | 1.1.0 稳定运行 |
| 阶段二：试点 | 2-3 周 | 认证改造 + 字典缓存 + 冒烟全绿 | PoC 通过 |
| 阶段三：全量 | 2-3 周 | 全功能回归 + 部署编排 + 灰度切换 | 试点通过 |

**结论**：当前单体架构满足 MDM 1.1.0 的业务规模（订阅方数量少、数据量中小）。建议在以下信号出现时启动迁移：① mdm 服务与系统管理出现发布耦合冲突；② 数据维护请求量达到单实例瓶颈；③ 企业技术栈整体转向微服务。在此之前，保持 `ruoyi-mdm` 模块边界清晰（已做到），迁移成本可控。
