package com.ruoyi.mdm.quality.service;

import java.util.List;
import com.ruoyi.mdm.quality.domain.MdmQualityRule;

/**
 * 数据质量校验规则 服务层
 *
 * @author ruoyi
 */
public interface IMdmQualityRuleService
{
    /**
     * 查询校验规则
     *
     * @param ruleId 规则ID
     * @return 校验规则
     */
    public MdmQualityRule selectRuleById(Long ruleId);

    /**
     * 查询校验规则列表
     *
     * @param mdmQualityRule 校验规则
     * @return 校验规则集合
     */
    public List<MdmQualityRule> selectRuleList(MdmQualityRule mdmQualityRule);

    /**
     * 新增校验规则
     *
     * @param mdmQualityRule 校验规则
     * @return 结果
     */
    public int insertRule(MdmQualityRule mdmQualityRule);

    /**
     * 修改校验规则
     *
     * @param mdmQualityRule 校验规则
     * @return 结果
     */
    public int updateRule(MdmQualityRule mdmQualityRule);

    /**
     * 批量删除校验规则
     *
     * @param ruleIds 规则ID数组
     * @return 结果
     */
    public int deleteRuleByIds(Long[] ruleIds);
}
