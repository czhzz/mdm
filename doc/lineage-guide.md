# 血缘追踪说明（1.1.0）

## 概述

血缘追踪展示一条主数据的完整生命周期链路：**来源 → 当前数据 → 下游系统**。不建新表，来源取自动态数据表 `source`/`source_time` 通用列，去向取自分发记录表 `mdm_distribution_record`。

## 来源标记规则

| 录入方式 | source 值 | 示例 |
|----------|-----------|------|
| 页面手动录入 | `MANUAL` | MANUAL |
| Excel 批量导入 | `IMPORT:<文件名>` | IMPORT:供应商数据.xlsx |
| 开放接口 API 推送 | `API:<appid>` | API:app_erp |

## 查看血缘

1. 进入「数据维护」，选择对象
2. 数据行操作列点击「血缘」
3. 弹窗展示：来源节点（类型 + 标记 + 时间）→ 当前数据（编码）→ 下游系统列表（应用名 + 推送时间 + 成功/失败）

## 技术说明

- `source` 列为对象发布时自动创建的通用列（`MdmObjectServiceImpl.publishObject` 建表 DDL）
- 导入/API 推送需调用 `insertDataWithSource(objectCode, data, source)`（普通页面录入走 `insertData` 默认 MANUAL）
- 去向匹配：`mdm_distribution_record` 中 `object_code` + `data_id` 相等的所有分发记录
- 存量对象（1.1.0 之前发布）无 source 列，需手动 ALTER TABLE 补列：

```sql
ALTER TABLE mdm_data_<obj_code>
  ADD COLUMN source VARCHAR(64) DEFAULT NULL COMMENT '数据来源',
  ADD COLUMN source_time DATETIME DEFAULT NULL COMMENT '来源时间';
```

## 限制

- 血缘为单跳展示（来源与去向各一层），不做字段级映射与跨对象传递追踪
- 手动编辑数据不更新 source（来源保持首次录入标记）
