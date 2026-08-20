package com.ruoyi.mdm.quality.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mdm.quality.domain.MdmQualityRule;
import com.ruoyi.mdm.quality.mapper.MdmQualityRuleMapper;
import com.ruoyi.mdm.quality.service.IMdmQualityRuleService;

/**
 * 数据质量校验规则 服务层实现
 *
 * @author ruoyi
 */
@Service
public class MdmQualityRuleServiceImpl implements IMdmQualityRuleService
{
    @Autowired
    private MdmQualityRuleMapper ruleMapper;

    @Override
    public MdmQualityRule selectRuleById(Long ruleId)
    {
        return ruleMapper.selectRuleById(ruleId);
    }

    @Override
    public List<MdmQualityRule> selectRuleList(MdmQualityRule mdmQualityRule)
    {
        return ruleMapper.selectRuleList(mdmQualityRule);
    }

    @Override
    public int insertRule(MdmQualityRule mdmQualityRule)
    {
        mdmQualityRule.setCreateBy(SecurityUtils.getUsername());
        return ruleMapper.insertRule(mdmQualityRule);
    }

    @Override
    public int updateRule(MdmQualityRule mdmQualityRule)
    {
        mdmQualityRule.setUpdateBy(SecurityUtils.getUsername());
        return ruleMapper.updateRule(mdmQualityRule);
    }

    @Override
    public int deleteRuleByIds(Long[] ruleIds)
    {
        int rows = 0;
        for (Long ruleId : ruleIds)
        {
            rows += ruleMapper.deleteRuleById(ruleId);
        }
        return rows;
    }
}
