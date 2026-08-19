package com.ruoyi.mdm.coderule.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mdm.coderule.domain.MdmCodeRule;
import com.ruoyi.mdm.coderule.domain.MdmCodeRuleSegment;
import com.ruoyi.mdm.coderule.mapper.MdmCodeRuleMapper;
import com.ruoyi.mdm.coderule.mapper.MdmCodeRuleSegmentMapper;
import com.ruoyi.mdm.coderule.service.IMdmCodeRuleService;

/**
 * 主数据编码规则 服务层实现
 *
 * @author ruoyi
 */
@Service
public class MdmCodeRuleServiceImpl implements IMdmCodeRuleService
{
    @Autowired
    private MdmCodeRuleMapper ruleMapper;

    @Autowired
    private MdmCodeRuleSegmentMapper segmentMapper;

    @Override
    public MdmCodeRule selectRuleById(Long ruleId)
    {
        return ruleMapper.selectRuleById(ruleId);
    }

    @Override
    public List<MdmCodeRule> selectRuleList(MdmCodeRule mdmCodeRule)
    {
        return ruleMapper.selectRuleList(mdmCodeRule);
    }

    @Override
    @Transactional
    public int insertRule(MdmCodeRule mdmCodeRule)
    {
        mdmCodeRule.setCreateBy(SecurityUtils.getUsername());
        int rows = ruleMapper.insertRule(mdmCodeRule);
        insertSegments(mdmCodeRule);
        return rows;
    }

    @Override
    @Transactional
    public int updateRule(MdmCodeRule mdmCodeRule)
    {
        mdmCodeRule.setUpdateBy(SecurityUtils.getUsername());
        int rows = ruleMapper.updateRule(mdmCodeRule);
        // 重写分段
        segmentMapper.deleteSegmentByRuleId(mdmCodeRule.getRuleId());
        insertSegments(mdmCodeRule);
        return rows;
    }

    @Override
    @Transactional
    public int deleteRuleById(Long ruleId)
    {
        segmentMapper.deleteSegmentByRuleId(ruleId);
        return ruleMapper.deleteRuleById(ruleId);
    }

    @Override
    public boolean checkRuleUnique(MdmCodeRule mdmCodeRule)
    {
        Long ruleId = StringUtils.isNull(mdmCodeRule.getRuleId()) ? -1L : mdmCodeRule.getRuleId();
        MdmCodeRule info = ruleMapper.selectRuleByObjectId(mdmCodeRule.getObjectId());
        if (StringUtils.isNotNull(info) && info.getRuleId().longValue() != ruleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public String previewCode(MdmCodeRule mdmCodeRule)
    {
        StringBuilder sb = new StringBuilder();
        if (mdmCodeRule.getSegments() == null)
        {
            return sb.toString();
        }
        for (MdmCodeRuleSegment seg : mdmCodeRule.getSegments())
        {
            if ("CONSTANT".equals(seg.getSegType()))
            {
                sb.append(seg.getSegValue());
            }
            else if ("DATE".equals(seg.getSegType()))
            {
                String fmt = StringUtils.isNotEmpty(seg.getSegValue()) ? seg.getSegValue() : "yyyyMMdd";
                try
                {
                    sb.append(new SimpleDateFormat(fmt).format(new Date()));
                }
                catch (IllegalArgumentException e)
                {
                    throw new ServiceException("日期格式不合法：" + seg.getSegValue());
                }
            }
            else if ("SEQUENCE".equals(seg.getSegType()))
            {
                int len = 4;
                try
                {
                    len = Integer.parseInt(seg.getSegValue());
                }
                catch (NumberFormatException ignored)
                {
                }
                sb.append(String.format("%0" + len + "d", 1));
            }
            else if ("ATTRIBUTE".equals(seg.getSegType()))
            {
                sb.append("[").append(seg.getSegValue()).append("]");
            }
        }
        return sb.toString();
    }

    /**
     * 批量保存分段
     */
    private void insertSegments(MdmCodeRule mdmCodeRule)
    {
        if (mdmCodeRule.getSegments() != null)
        {
            for (MdmCodeRuleSegment seg : mdmCodeRule.getSegments())
            {
                seg.setRuleId(mdmCodeRule.getRuleId());
                segmentMapper.insertSegment(seg);
            }
        }
    }
}
