package com.ruoyi.mdm.lineage.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mdm.lineage.service.IMdmLineageService;
import com.ruoyi.mdm.model.mapper.MdmObjectMapper;

/**
 * 主数据血缘追踪 服务层实现
 *
 * <p>血缘信息不落新表：来源取自动态数据表 `source`/`source_time` 通用列，
 * 去向取自分发记录表 mdm_distribution_record。
 *
 * @author ruoyi
 */
@Service
public class MdmLineageServiceImpl implements IMdmLineageService
{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MdmObjectMapper objectMapper;

    @Override
    public Map<String, Object> getLineage(String objectCode, Long dataId)
    {
        if (!objectCode.matches("^[a-zA-Z_][a-zA-Z0-9_]{0,49}$"))
        {
            throw new ServiceException("对象编码包含非法字符");
        }
        if (objectMapper.checkObjectCodeUnique(objectCode) == null)
        {
            throw new ServiceException("对象不存在：" + objectCode);
        }
        String table = "mdm_data_" + objectCode;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, object_code, source, source_time FROM " + table + " WHERE id = ?", dataId);
        if (rows.isEmpty())
        {
            throw new ServiceException("数据不存在");
        }
        Map<String, Object> row = rows.get(0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("objectCode", objectCode);
        result.put("dataId", dataId);
        result.put("dataCode", row.get("object_code"));

        // 来源节点
        Map<String, Object> sourceNode = new LinkedHashMap<>();
        String source = row.get("source") == null ? null : String.valueOf(row.get("source"));
        sourceNode.put("type", parseSourceType(source));
        sourceNode.put("value", source);
        sourceNode.put("time", row.get("source_time"));
        result.put("source", sourceNode);

        // 去向节点：分发记录中 data_id 匹配且状态为成功的记录
        List<Map<String, Object>> targets = jdbcTemplate.queryForList(
                "SELECT r.app_id AS appId, a.app_name AS appName, r.send_time AS sendTime, r.status "
                + "FROM mdm_distribution_record r "
                + "LEFT JOIN mdm_app a ON a.app_id = r.app_id "
                + "WHERE r.object_code = ? AND r.data_id = ? "
                + "ORDER BY r.send_time DESC", objectCode, dataId);
        List<Map<String, Object>> targetNodes = new ArrayList<>();
        for (Map<String, Object> t : targets)
        {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("appName", t.get("appName"));
            node.put("sendTime", t.get("sendTime"));
            node.put("success", "1".equals(String.valueOf(t.get("status"))));
            targetNodes.add(node);
        }
        result.put("targets", targetNodes);
        return result;
    }

    private String parseSourceType(String source)
    {
        if (source == null || source.isEmpty())
        {
            return "UNKNOWN";
        }
        if (source.startsWith("IMPORT:"))
        {
            return "IMPORT";
        }
        if (source.startsWith("API:"))
        {
            return "API";
        }
        return "MANUAL";
    }
}