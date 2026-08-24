package com.ruoyi.mdm.quality.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mdm.model.domain.MdmObject;
import com.ruoyi.mdm.model.mapper.MdmObjectMapper;
import com.ruoyi.mdm.quality.domain.MdmQualityIssue;
import com.ruoyi.mdm.quality.mapper.MdmQualityIssueMapper;
import com.ruoyi.mdm.quality.service.IMdmQualityService;

/**
 * 数据质量 服务层实现
 *
 * @author ruoyi
 */
@Service
public class MdmQualityServiceImpl implements IMdmQualityService
{
    @Autowired
    private MdmQualityIssueMapper issueMapper;

    @Autowired
    private MdmObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<MdmQualityIssue> selectIssueList(MdmQualityIssue mdmQualityIssue)
    {
        return issueMapper.selectIssueList(mdmQualityIssue);
    }

    @Override
    public int handleIssue(MdmQualityIssue mdmQualityIssue)
    {
        if (StringUtils.isNull(mdmQualityIssue.getIssueId())
                || !"1".equals(mdmQualityIssue.getHandleStatus()) && !"2".equals(mdmQualityIssue.getHandleStatus()))
        {
            throw new ServiceException("处理状态不合法");
        }
        mdmQualityIssue.setHandleBy(SecurityUtils.getUsername());
        return issueMapper.updateIssueHandle(mdmQualityIssue);
    }

    @Override
    public List<Map<String, Object>> duplicateCheck(String objectCode, List<String> fields)
    {
        if (fields == null || fields.isEmpty())
        {
            throw new ServiceException("请至少选择一个查重字段");
        }
        if (!objectCode.matches("^[a-zA-Z_][a-zA-Z0-9_]{0,49}$"))
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
            throw new ServiceException("对象未发布，无法执行查重");
        }
        String table = "mdm_data_" + objectCode;
        for (String f : fields)
        {
            if (!f.matches("^[a-zA-Z_][a-zA-Z0-9_]{0,49}$"))
            {
                throw new ServiceException("查重字段包含非法字符：" + f);
            }
        }
        String cols = String.join(", ", fields);
        String sql = "SELECT " + cols + ", COUNT(*) AS cnt FROM " + table
                + " WHERE status = '1' GROUP BY " + cols + " HAVING COUNT(*) > 1";
        List<Map<String, Object>> groups = jdbcTemplate.queryForList(sql);
        if (groups.isEmpty())
        {
            return groups;
        }
        // 自动登记重复问题到台账
        String username = SecurityUtils.getUsername();
        for (Map<String, Object> group : groups)
        {
            StringBuilder cond = new StringBuilder();
            List<Object> args = new ArrayList<>();
            for (String f : fields)
            {
                cond.append(cond.length() == 0 ? "" : " AND ").append(f).append(" = ?");
                args.add(group.get(f));
            }
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT id FROM " + table + " WHERE status = '1' AND " + cond, args.toArray());
            for (Map<String, Object> row : rows)
            {
                MdmQualityIssue issue = new MdmQualityIssue();
                issue.setObjectId(object.getObjectId());
                issue.setDataId(((Number) row.get("id")).longValue());
                issue.setIssueType("DUPLICATE");
                issue.setIssueDesc("查重字段 [" + String.join(",", fields) + "] 值重复：" + group);
                issue.setHandleStatus("0");
                issue.setCreateBy(username);
                issueMapper.insertIssue(issue);
            }
        }
        return groups;
    }

    @Override
    public Map<String, Object> getDashboardData()
    {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 总览卡片
        Long totalIssues = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM mdm_quality_issue", Long.class);
        Long pendingIssues = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM mdm_quality_issue WHERE handle_status = '0'", Long.class);
        Long handledIssues = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM mdm_quality_issue WHERE handle_status IN ('1','2')", Long.class);
        Long totalObjects = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM mdm_object WHERE status = '1'", Long.class);
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("totalIssues", totalIssues);
        overview.put("pendingIssues", pendingIssues);
        overview.put("handledIssues", handledIssues);
        overview.put("totalObjects", totalObjects);
        result.put("overview", overview);

        // 2. 问题趋势（近 30 天，按日）
        List<Map<String, Object>> trend = jdbcTemplate.queryForList(
                "SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS day, COUNT(1) AS cnt "
                + "FROM mdm_quality_issue WHERE create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) "
                + "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d') ORDER BY day");
        result.put("trend", trend);

        // 3. 问题类型分布
        List<Map<String, Object>> typeDist = jdbcTemplate.queryForList(
                "SELECT issue_type AS type, COUNT(1) AS cnt FROM mdm_quality_issue GROUP BY issue_type");
        result.put("typeDist", typeDist);

        // 4. 对象健康度排行（各对象的问题数，Top-10）
        List<Map<String, Object>> objectRank = jdbcTemplate.queryForList(
                "SELECT o.object_code AS objectCode, o.object_name AS objectName, "
                + "COUNT(i.issue_id) AS issueCount "
                + "FROM mdm_object o LEFT JOIN mdm_quality_issue i ON i.object_id = o.object_id "
                + "WHERE o.status = '1' GROUP BY o.object_id, o.object_code, o.object_name "
                + "ORDER BY issueCount DESC LIMIT 10");
        result.put("objectRank", objectRank);

        // 5. 最近问题列表（10 条）
        List<Map<String, Object>> recentIssues = jdbcTemplate.queryForList(
                "SELECT i.issue_id AS issueId, o.object_name AS objectName, i.issue_type AS issueType, "
                + "i.issue_desc AS issueDesc, i.handle_status AS handleStatus, i.create_time AS createTime "
                + "FROM mdm_quality_issue i LEFT JOIN mdm_object o ON o.object_id = i.object_id "
                + "ORDER BY i.create_time DESC LIMIT 10");
        result.put("recentIssues", recentIssues);

        return result;
    }
}
