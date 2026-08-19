package com.ruoyi.mdm.coderule.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.redis.RedisCache;
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

    @Autowired
    private RedisCache redisCache;

    @Override
    public MdmCodeRule selectRuleById(Long ruleId)
    {
        return ruleMapper.selectRuleById(ruleId);
    }

    @Override
    public MdmCodeRule selectRuleByObjectId(Long objectId)
    {
        return ruleMapper.selectRuleByObjectId(objectId);
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

    @Override
    public String generateCode(MdmCodeRule mdmCodeRule, Map<String, Object> data)
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
                sb.append(sequence(mdmCodeRule, seg));
            }
            else if ("ATTRIBUTE".equals(seg.getSegType()))
            {
                Object v = data == null ? null : data.get(seg.getSegValue());
                if (v == null)
                {
                    throw new ServiceException("编码段缺少属性值：" + seg.getSegValue());
                }
                sb.append(v);
            }
        }
        return sb.toString();
    }

    /**
     * 流水号：Redis INCR 原子递增，key 含周期前缀实现按日/月/年重置
     */
    private String sequence(MdmCodeRule rule, MdmCodeRuleSegment seg)
    {
        int len = 4;
        try
        {
            len = Integer.parseInt(seg.getSegValue());
        }
        catch (NumberFormatException ignored)
        {
        }
        String period = "";
        String resetType = StringUtils.isNotEmpty(rule.getResetType()) ? rule.getResetType() : "NONE";
        if ("DAY".equals(resetType))
        {
            period = new SimpleDateFormat("yyyyMMdd").format(new Date());
        }
        else if ("MONTH".equals(resetType))
        {
            period = new SimpleDateFormat("yyyyMM").format(new Date());
        }
        else if ("YEAR".equals(resetType))
        {
            period = new SimpleDateFormat("yyyy").format(new Date());
        }
        String key = "mdm:code:" + rule.getObjectId() + ":" + period;
        Long seq = redisCache.increment(key, 1);
        // 周期键设置过期时间（NONE 不过期），避免 key 无限累积
        if ("DAY".equals(resetType))
        {
            redisCache.expire(key, 2, TimeUnit.DAYS);
        }
        else if ("MONTH".equals(resetType))
        {
            redisCache.expire(key, 60, TimeUnit.DAYS);
        }
        else if ("YEAR".equals(resetType))
        {
            redisCache.expire(key, 730, TimeUnit.DAYS);
        }
        String seqStr = String.valueOf(seq);
        if (seqStr.length() > len)
        {
            throw new ServiceException("流水号已超出 " + len + " 位上限");
        }
        return String.format("%0" + len + "d", seq);
    }
}
