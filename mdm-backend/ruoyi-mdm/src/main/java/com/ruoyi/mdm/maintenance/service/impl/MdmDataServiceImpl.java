package com.ruoyi.mdm.maintenance.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mdm.coderule.domain.MdmCodeRule;
import com.ruoyi.mdm.coderule.service.IMdmCodeRuleService;
import com.ruoyi.mdm.maintenance.service.IMdmDataService;
import com.ruoyi.mdm.model.domain.MdmAttribute;
import com.ruoyi.mdm.model.domain.MdmObject;
import com.ruoyi.mdm.model.mapper.MdmAttributeMapper;
import com.ruoyi.mdm.model.mapper.MdmObjectMapper;

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
            "create_by", "create_time", "update_by", "update_time", "remark"};

    @Autowired
    private MdmObjectMapper objectMapper;

    @Autowired
    private MdmAttributeMapper attributeMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IMdmCodeRuleService codeRuleService;

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
        validateData(table, data, null);
        // 按编码方案自动生成编码并回填到编码字段
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
        List<String> cols = new ArrayList<>();
        List<Object> vals = new ArrayList<>();
        for (Map.Entry<String, Object> e : data.entrySet())
        {
            if (table.columns.contains(e.getKey()) && StringUtils.isNotEmpty(String.valueOf(e.getValue())))
            {
                cols.add(e.getKey());
                vals.add(e.getValue());
            }
        }
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(table.tableName)
                .append(" (object_code, status, ");
        sql.append(String.join(", ", cols));
        sql.append(", create_by, create_time) VALUES (?, '0', ");
        sql.append(String.join(", ", repeat("?", cols.size())));
        sql.append(", ?, sysdate())");
        List<Object> params = new ArrayList<>();
        params.add(objectCode);
        params.addAll(vals);
        params.add(SecurityUtils.getUsername());
        return jdbcTemplate.update(sql.toString(), params.toArray());
    }

    @Override
    public int updateData(String objectCode, Long id, Map<String, Object> data)
    {
        MdmTable table = resolveTable(objectCode);
        validateData(table, data, id);
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
        vals.add(SecurityUtils.getUsername());
        vals.add(id);
        String sql = "UPDATE " + table.tableName + " SET " + String.join(", ", sets) + " WHERE id = ?";
        return jdbcTemplate.update(sql, vals.toArray());
    }

    @Override
    public int deleteDataByIds(String objectCode, Long[] ids)
    {
        MdmTable table = resolveTable(objectCode);
        StringBuilder sql = new StringBuilder("DELETE FROM ").append(table.tableName).append(" WHERE id IN (");
        for (int i = 0; i < ids.length; i++)
        {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(")");
        return jdbcTemplate.update(sql.toString(), (Object[]) ids);
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
                status, SecurityUtils.getUsername(), id);
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
        return new MdmTable(tableName, columns, attrs);
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

    /**
     * 服务端校验：必填 / 唯一 / 枚举 / 数值范围
     */
    private void validateData(MdmTable table, Map<String, Object> data, Long excludeId)
    {
        for (MdmAttribute attr : table.attributes)
        {
            String key = attr.getAttrCode();
            Object val = data.get(key);
            String strVal = val == null ? null : String.valueOf(val).trim();
            // 必填
            if ("Y".equals(attr.getRequiredFlag()) && StringUtils.isEmpty(strVal))
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
        final String tableName;
        final Set<String> columns;
        final List<MdmAttribute> attributes;

        MdmTable(String tableName, Set<String> columns, List<MdmAttribute> attributes)
        {
            this.tableName = tableName;
            this.columns = columns;
            this.attributes = attributes;
        }
    }
}
