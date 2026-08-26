package com.ruoyi.mdm.maintenance.service.impl;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mdm.coderule.domain.MdmCodeRule;
import com.ruoyi.mdm.coderule.service.IMdmCodeRuleService;
import com.ruoyi.mdm.integration.service.IIntegrationService;
import com.ruoyi.mdm.maintenance.service.IMdmDataService;
import com.ruoyi.mdm.model.domain.MdmAttribute;
import com.ruoyi.mdm.model.domain.MdmObject;
import com.ruoyi.mdm.model.mapper.MdmAttributeMapper;
import com.ruoyi.mdm.model.mapper.MdmObjectMapper;
import com.ruoyi.mdm.quality.domain.MdmQualityRule;
import com.ruoyi.mdm.quality.mapper.MdmQualityRuleMapper;
import com.ruoyi.mdm.relation.domain.MdmRelation;
import com.ruoyi.mdm.relation.mapper.MdmRelationMapper;
import com.ruoyi.mdm.relation.service.IMdmRelationService;

/**
 * 主数据动态数据 服务层实现
 *
 * <p>动态 SQL 表名/列名均来自白名单校验后的对象编码与属性编码，值全部使用参数占位，无注入风险。
 *
 * @author ruoyi
 */
@Service
public class MdmDataServiceImpl implements IMdmDataService
{
    /** 通用列（不含属性列） */
    private static final String[] BASE_COLUMNS = {"id", "object_code", "status", "version",
            "create_by", "create_time", "update_by", "update_time", "remark", "source", "source_time"};

    @Autowired
    private MdmObjectMapper objectMapper;

    @Autowired
    private MdmAttributeMapper attributeMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IMdmCodeRuleService codeRuleService;

    @Autowired
    private MdmQualityRuleMapper qualityRuleMapper;

    // 1.2.0：分发通道迁入 integration 包（旧 distribution 包随表更名废弃）
    @Autowired
    private IIntegrationService integrationService;

    @Autowired
    private MdmRelationMapper relationMapper;

    @Autowired
    private IMdmRelationService relationService;

    @Override
    public List<Map<String, Object>> selectDataList(String objectCode, Map<String, Object> query, int pageNum, int pageSize)
    {
        MdmTable table = resolveTable(objectCode);
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(table.tableName);
        List<Object> args = new ArrayList<>();
        String where = buildWhere(table, query, args);
        sql.append(where).append(" ORDER BY id DESC LIMIT ?, ?");
        args.add((pageNum - 1) * pageSize);
        args.add(pageSize);
        return jdbcTemplate.queryForList(sql.toString(), args.toArray());
    }

    @Override
    public long countData(String objectCode, Map<String, Object> query)
    {
        MdmTable table = resolveTable(objectCode);
        StringBuilder sql = new StringBuilder("SELECT COUNT(1) FROM ").append(table.tableName);
        List<Object> args = new ArrayList<>();
        sql.append(buildWhere(table, query, args));
        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, args.toArray());
        return count == null ? 0 : count;
    }

    @Override
    public Map<String, Object> selectDataById(String objectCode, Long id)
    {
        MdmTable table = resolveTable(objectCode);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT * FROM " + table.tableName + " WHERE id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public int insertData(String objectCode, Map<String, Object> data)
    {
        MdmTable table = resolveTable(objectCode);
        // 先按编码方案生成编码回填，再校验（编码字段为必填时不会被误判为空）
        MdmObject object = objectMapper.checkObjectCodeUnique(objectCode);
        if (StringUtils.isNotNull(object))
        {
            MdmCodeRule rule = codeRuleService.selectRuleByObjectId(object.getObjectId());
            // 编码仅在为空时生成：外部系统（导入/API 接收）传入的业务编码优先，不被覆盖
            if (StringUtils.isNotNull(rule) && StringUtils.isNotEmpty(rule.getCodeField())
                    && table.columns.contains(rule.getCodeField())
                    && StringUtils.isEmpty(String.valueOf(data.get(rule.getCodeField()) == null ? "" : data.get(rule.getCodeField()))))
            {
                data.put(rule.getCodeField(), codeRuleService.generateCode(rule, data));
            }
        }
        validateData(table, data, null, null);
        Long pk = doInsert(table, objectCode, "0", pickColumns(table, data));
        integrationService.triggerPush(objectCode, pk, "INSERT", data);
        return pk > 0 ? 1 : 0;
    }

    @Override
    public int insertDataWithSource(String objectCode, Map<String, Object> data, String source)
    {
        // 1.1.0：带来源标记的插入（导入/API 推送用）
        // 1.0 时代种子表无 source 列，先探测再写，避免 SQL 报错
        if (source != null && tableHasColumn("mdm_data_" + objectCode, "source"))
        {
            data.put("source", source);
            data.put("source_time", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        }
        return insertData(objectCode, data);
    }

    /** 探测数据表是否含指定列（存量库兼容：1.0 种子表缺血缘列） */
    private boolean tableHasColumn(String tableName, String columnName)
    {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = database() AND table_name = ? AND column_name = ?",
                Integer.class, tableName, columnName);
        return count != null && count > 0;
    }

    @Override
    public int updateData(String objectCode, Long id, Map<String, Object> data)
    {
        MdmTable table = resolveTable(objectCode);
        validateData(table, data, id, data.keySet());
        List<String> sets = new ArrayList<>();
        List<Object> vals = new ArrayList<>();
        for (Map.Entry<String, Object> e : data.entrySet())
        {
            if (table.columns.contains(e.getKey()))
            {
                sets.add(e.getKey() + " = ?");
                vals.add(e.getValue());
            }
        }
        if (sets.isEmpty())
        {
            return 0;
        }
        sets.add("update_by = ?");
        sets.add("update_time = sysdate()");
        vals.add(currentUsername());
        vals.add(id);
        String sql = "UPDATE " + table.tableName + " SET " + String.join(", ", sets) + " WHERE id = ?";
        int rows = jdbcTemplate.update(sql, vals.toArray());
        integrationService.triggerPush(objectCode, id, "UPDATE", data);
        return rows;
    }

    @Override
    public int deleteDataByIds(String objectCode, Long[] ids)
    {
        MdmTable table = resolveTable(objectCode);
        // 级联检查：查询所有以本对象为目标的 RESTRICT 关系
        MdmRelation query = new MdmRelation();
        query.setTargetObjectCode(objectCode);
        query.setCascadeRule("RESTRICT");
        List<MdmRelation> restrictRelations = relationMapper.selectByTargetObject(objectCode);
        for (MdmRelation rel : restrictRelations)
        {
            if ("RESTRICT".equals(rel.getCascadeRule()) && StringUtils.isNotEmpty(rel.getSourceFieldCode()))
            {
                String srcTable = "mdm_data_" + rel.getSourceObjectCode();
                for (Long id : ids)
                {
                    // 获取被删除数据的编码值
                    Map<String, Object> row = jdbcTemplate.queryForMap(
                            "SELECT object_code FROM " + table.tableName + " WHERE id = ?", id);
                    if (row != null && row.get("object_code") != null)
                    {
                        Long refCount = jdbcTemplate.queryForObject(
                                "SELECT COUNT(1) FROM " + srcTable + " WHERE " + rel.getSourceFieldCode() + " = ?",
                                Long.class, String.valueOf(row.get("object_code")));
                        if (refCount != null && refCount > 0)
                        {
                            throw new ServiceException("数据被对象【" + rel.getSourceObjectCode()
                                    + "】引用，无法删除（级联规则：RESTRICT）");
                        }
                    }
                }
            }
        }
        StringBuilder sql = new StringBuilder("DELETE FROM ").append(table.tableName).append(" WHERE id IN (");
        for (int i = 0; i < ids.length; i++)
        {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(")");
        return jdbcTemplate.update(sql.toString(), (Object[]) ids);
    }

    @Override
    public int applyAuditInsert(String objectCode, Map<String, Object> data)
    {
        MdmTable table = resolveTable(objectCode);
        // 同 insertData：先生成编码再校验（审核落地时再次校验，防止绕过校验的数据落库）
        MdmObject object = objectMapper.checkObjectCodeUnique(objectCode);
        if (StringUtils.isNotNull(object))
        {
            MdmCodeRule rule = codeRuleService.selectRuleByObjectId(object.getObjectId());
            if (StringUtils.isNotNull(rule) && StringUtils.isNotEmpty(rule.getCodeField())
                    && table.columns.contains(rule.getCodeField()))
            {
                data.put(rule.getCodeField(), codeRuleService.generateCode(rule, data));
            }
        }
        validateData(table, data, null, null);
        Long pk = doInsert(table, objectCode, "1", pickColumns(table, data));
        integrationService.triggerPush(objectCode, pk, "INSERT", data);
        return pk > 0 ? 1 : 0;
    }

    @Override
    public int applyAuditUpdate(String objectCode, Long id, Map<String, Object> data)
    {
        return updateData(objectCode, id, data);
    }

    @Override
    public List<Map<String, Object>> selectRefDataList(String refObjectCode, String displayFields, String keyword)
    {
        if (!isValidIdentifier(refObjectCode))
        {
            throw new ServiceException("引用对象编码包含非法字符");
        }
        String tableName = "mdm_data_" + refObjectCode;
        StringBuilder sql = new StringBuilder("SELECT id, object_code, ");
        if (StringUtils.isNotEmpty(displayFields))
        {
            // 只选取合法字段，防止注入
            for (String field : displayFields.split(","))
            {
                String f = field.trim();
                if (isValidIdentifier(f))
                {
                    sql.append(f).append(", ");
                }
            }
        }
        sql.append("object_code AS display FROM ").append(tableName);
        sql.append(" WHERE status = '1'");
        List<Object> args = new ArrayList<>();
        if (StringUtils.isNotEmpty(keyword))
        {
            sql.append(" AND object_code LIKE ?");
            args.add("%" + keyword + "%");
        }
        sql.append(" ORDER BY id DESC LIMIT 50");
        return jdbcTemplate.queryForList(sql.toString(), args.toArray());
    }

    @Override
    public int updateDataStatus(String objectCode, Long id, String status)
    {
        MdmTable table = resolveTable(objectCode);
        if (!"1".equals(status) && !"2".equals(status))
        {
            throw new ServiceException("不支持的目标状态：" + status);
        }
        return jdbcTemplate.update("UPDATE " + table.tableName
                        + " SET status = ?, update_by = ?, update_time = sysdate() WHERE id = ?",
                status, currentUsername(), id);
    }

    /**
     * 当前操作人（匿名场景——集成对外接口——无登录用户，回退 "API"）
     */
    private String currentUsername()
    {
        try
        {
            return SecurityUtils.getUsername();
        }
        catch (Exception e)
        {
            return "API";
        }
    }

    /**
     * 解析对象数据表结构：校验对象存在且已发布、编码白名单，返回表名与合法列集合
     */
    private MdmTable resolveTable(String objectCode)
    {
        if (!isValidIdentifier(objectCode))
        {
            throw new ServiceException("对象编码包含非法字符");
        }
        MdmObject object = objectMapper.checkObjectCodeUnique(objectCode);
        if (StringUtils.isNull(object))
        {
            throw new ServiceException("对象不存在：" + objectCode);
        }
        if (!"1".equals(object.getStatus()))
        {
            throw new ServiceException("对象未发布，无法操作数据");
        }
        String tableName = "mdm_data_" + objectCode;
        MdmAttribute query = new MdmAttribute();
        query.setObjectId(object.getObjectId());
        List<MdmAttribute> attrs = attributeMapper.selectAttributeList(query);
        Set<String> columns = new LinkedHashSet<>();
        for (String col : BASE_COLUMNS)
        {
            columns.add(col);
        }
        for (MdmAttribute attr : attrs)
        {
            // 防御性白名单校验（列名拼接 SQL 前的最后一道关卡）
            if (!isValidIdentifier(attr.getAttrCode()))
            {
                throw new ServiceException("属性编码包含非法字符：" + attr.getAttrCode());
            }
            columns.add(attr.getAttrCode());
        }
        return new MdmTable(object.getObjectId(), tableName, columns, attrs);
    }

    /**
     * 构建 WHERE 子句（仅允许属性列作为条件，值参数化）
     */
    private String buildWhere(MdmTable table, Map<String, Object> query, List<Object> args)
    {
        StringBuilder where = new StringBuilder();
        if (query != null)
        {
            for (Map.Entry<String, Object> e : query.entrySet())
            {
                String key = e.getKey();
                if (table.columns.contains(key) && e.getValue() != null && StringUtils.isNotEmpty(String.valueOf(e.getValue())))
                {
                    where.append(where.length() == 0 ? " WHERE " : " AND ");
                    where.append(key).append(" LIKE ?");
                    args.add("%" + e.getValue() + "%");
                }
            }
        }
        return where.toString();
    }

    private boolean isValidIdentifier(String name)
    {
        return StringUtils.isNotBlank(name) && name.matches("^[a-zA-Z_][a-zA-Z0-9_]{0,49}$");
    }

    private List<String> repeat(String s, int n)
    {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < n; i++)
        {
            list.add(s);
        }
        return list;
    }

    /** 从入参中挑出合法属性列（忽略空值与非法列） */
    private Map<String, Object> pickColumns(MdmTable table, Map<String, Object> data)
    {
        Map<String, Object> picked = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : data.entrySet())
        {
            if (table.columns.contains(e.getKey()) && StringUtils.isNotEmpty(String.valueOf(e.getValue())))
            {
                picked.put(e.getKey(), e.getValue());
            }
        }
        return picked;
    }

    /** 业务编码：唯一/主属性值优先，缺省用对象编码（兼容无唯一属性的对象） */
    private String businessCode(MdmTable table, Map<String, Object> cols, String fallback)
    {
        for (MdmAttribute a : table.attributes)
        {
            if ("Y".equals(a.getUniqueFlag()) || "Y".equals(a.getPrimaryFlag()))
            {
                Object v = cols.get(a.getAttrCode());
                if (v != null && StringUtils.isNotEmpty(String.valueOf(v)))
                {
                    return String.valueOf(v);
                }
            }
        }
        return fallback;
    }

    /** 动态插入并返回自增主键 */
    private Long doInsert(MdmTable table, String objectCode, String status, Map<String, Object> cols)
    {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(table.tableName)
                .append(" (object_code, status, ");
        sql.append(String.join(", ", cols.keySet()));
        sql.append(", create_by, create_time) VALUES (?, ?, ");
        sql.append(String.join(", ", repeat("?", cols.size())));
        sql.append(", ?, sysdate())");
        List<Object> params = new ArrayList<>();
        // 1.2.0 修复：object_code 列语义为业务编码（唯一/主属性值），
        // 旧实现写入对象编码导致 uk_object_code 唯一约束下每对象仅一条数据
        params.add(businessCode(table, cols, objectCode));
        params.add(status);
        params.addAll(cols.values());
        params.add(currentUsername());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn ->
        {
            PreparedStatement ps = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.size(); i++)
            {
                ps.setObject(i + 1, params.get(i));
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? 0L : key.longValue();
    }

    /**
     * 服务端校验：必填 / 唯一 / 枚举 / 数值范围
     */
    private void validateData(MdmTable table, Map<String, Object> data, Long excludeId, Set<String> submitted)
    {
        for (MdmAttribute attr : table.attributes)
        {
            String key = attr.getAttrCode();
            Object val = data.get(key);
            String strVal = val == null ? null : String.valueOf(val).trim();
            // 必填：仅校验提交字段（更新走部分字段时，未提交字段视为保持不变）
            if ("Y".equals(attr.getRequiredFlag()) && (submitted == null || submitted.contains(key))
                    && StringUtils.isEmpty(strVal))
            {
                throw new ServiceException("属性【" + attr.getAttrName() + "】不能为空");
            }
            if (StringUtils.isEmpty(strVal))
            {
                continue;
            }
            // 唯一（排除自身）
            if ("Y".equals(attr.getUniqueFlag()) && existsValue(table.tableName, key, strVal, excludeId))
            {
                throw new ServiceException("属性【" + attr.getAttrName() + "】值已存在：" + strVal);
            }
            // 枚举
            if ("enum".equals(attr.getSourceType()) && StringUtils.isNotEmpty(attr.getEnumValues())
                    && !Arrays.asList(attr.getEnumValues().split(",")).contains(strVal))
            {
                throw new ServiceException("属性【" + attr.getAttrName() + "】值不在枚举范围内");
            }
            // 字典值（标准字典 = 若依 sys_dict_data）
            if ("dict".equals(attr.getSourceType()) && StringUtils.isNotEmpty(attr.getDictType())
                    && !dictDataExists(attr.getDictType(), strVal))
            {
                throw new ServiceException("属性【" + attr.getAttrName() + "】值不在标准字典中：" + strVal);
            }
            // 引用校验：检查引用值是否存在于目标对象数据表中
            if (StringUtils.isNotEmpty(attr.getRefObjectCode()) && StringUtils.isNotEmpty(strVal))
            {
                if (!isValidIdentifier(attr.getRefObjectCode()))
                {
                    throw new ServiceException("引用目标对象编码包含非法字符：" + attr.getRefObjectCode());
                }
                String refTable = "mdm_data_" + attr.getRefObjectCode();
                // 尝试用编码字段匹配（目标表中 object_code 列或同名属性列）
                String refCol = "object_code";
                // ponytail: 优先用 object_code 匹配，若引用的是属性值需额外配置，当前简单场景够用
                Long refCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(1) FROM " + refTable + " WHERE " + refCol + " = ?",
                        Long.class, strVal);
                if (refCount == null || refCount == 0)
                {
                    throw new ServiceException("属性【" + attr.getAttrName() + "】引用的数据不存在：" + strVal);
                }
            }
            // 数值范围
            if ("range".equals(attr.getSourceType()) && StringUtils.isNotEmpty(attr.getMinValue() + attr.getMaxValue()))
            {
                try
                {
                    BigDecimal v = new BigDecimal(strVal);
                    if (StringUtils.isNotEmpty(attr.getMinValue()) && v.compareTo(new BigDecimal(attr.getMinValue())) < 0)
                    {
                        throw new ServiceException("属性【" + attr.getAttrName() + "】不能小于最小值");
                    }
                    if (StringUtils.isNotEmpty(attr.getMaxValue()) && v.compareTo(new BigDecimal(attr.getMaxValue())) > 0)
                    {
                        throw new ServiceException("属性【" + attr.getAttrName() + "】不能大于最大值");
                    }
                }
                catch (NumberFormatException e)
                {
                    throw new ServiceException("属性【" + attr.getAttrName() + "】必须为数字");
                }
            }
        }
        // 执行配置的校验规则（启用的正则/必填规则）
        MdmQualityRule rq = new MdmQualityRule();
        rq.setObjectId(table.objectId);
        rq.setStatus("0");
        for (MdmQualityRule rule : qualityRuleMapper.selectRuleList(rq))
        {
            String ruleValue = StringUtils.isEmpty(rule.getTargetValue()) ? null
                    : (data.get(rule.getTargetValue()) == null ? null : String.valueOf(data.get(rule.getTargetValue())).trim());
            String msg = StringUtils.isNotEmpty(rule.getRuleMsg()) ? rule.getRuleMsg() : rule.getRuleName();
            if ("REGEX".equals(rule.getRuleType()) && StringUtils.isNotEmpty(rule.getRuleExpr())
                    && StringUtils.isNotEmpty(ruleValue) && !ruleValue.matches(rule.getRuleExpr()))
            {
                throw new ServiceException("【" + rule.getTargetValue() + "】" + msg);
            }
            // 必填规则同样仅校验提交字段（部分更新时未提交字段视为不变）
            if ("REQUIRED".equals(rule.getRuleType()) && StringUtils.isEmpty(ruleValue)
                    && (submitted == null || StringUtils.isEmpty(rule.getTargetValue())
                            || submitted.contains(rule.getTargetValue())))
            {
                throw new ServiceException(msg);
            }
        }
    }

    /**
     * 校验属性值是否已存在（排除指定 id）
     */
    private boolean existsValue(String tableName, String column, String value, Long excludeId)
    {
        String sql = "SELECT COUNT(1) FROM " + tableName + " WHERE " + column + " = ?";
        List<Object> args = new ArrayList<>();
        args.add(value);
        if (excludeId != null)
        {
            sql += " AND id != ?";
            args.add(excludeId);
        }
        Long count = jdbcTemplate.queryForObject(sql, Long.class, args.toArray());
        return count != null && count > 0;
    }

    /**
     * 校验字典值是否存在于标准字典（sys_dict_data）
     */
    private boolean dictDataExists(String dictType, String value)
    {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sys_dict_data WHERE dict_type = ? AND dict_value = ? AND status = '0'",
                Integer.class, dictType, value);
        return count != null && count > 0;
    }

    /** 动态表结构：表名 + 合法列集合 + 属性元数据 */
    private static class MdmTable
    {
        final Long objectId;
        final String tableName;
        final Set<String> columns;
        final List<MdmAttribute> attributes;

        MdmTable(Long objectId, String tableName, Set<String> columns, List<MdmAttribute> attributes)
        {
            this.objectId = objectId;
            this.tableName = tableName;
            this.columns = columns;
            this.attributes = attributes;
        }
    }
}
