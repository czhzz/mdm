package com.ruoyi.mdm.quality.service.impl;

import java.util.ArrayList;
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
}
