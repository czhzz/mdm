package com.ruoyi.mdm.maintenance.service.impl;

import java.util.ArrayList;
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
        Set<String> columns = new LinkedHashSet<>();
        for (String col : BASE_COLUMNS)
        {
            columns.add(col);
        }
        for (MdmAttribute attr : attributeMapper.selectAttributeList(query))
        {
            columns.add(attr.getAttrCode());
        }
        return new MdmTable(tableName, columns);
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

    /** 动态表结构：表名 + 合法列集合 */
    private static class MdmTable
    {
        final String tableName;
        final Set<String> columns;

        MdmTable(String tableName, Set<String> columns)
        {
            this.tableName = tableName;
            this.columns = columns;
        }
    }
}
