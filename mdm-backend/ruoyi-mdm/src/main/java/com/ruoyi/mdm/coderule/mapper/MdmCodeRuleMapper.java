package com.ruoyi.mdm.coderule.mapper;

import java.util.List;
import com.ruoyi.mdm.coderule.domain.MdmCodeRule;

/**
 * 主数据编码规则 数据层
 *
 * @author ruoyi
 */
public interface MdmCodeRuleMapper
{
    /**
     * 查询编码规则（含分段）
     *
     * @param ruleId 规则ID
     * @return 编码规则
     */
    public MdmCodeRule selectRuleById(Long ruleId);

    /**
     * 查询编码规则列表
     *
     * @param mdmCodeRule 编码规则
     * @return 编码规则集合
     */
    public List<MdmCodeRule> selectRuleList(MdmCodeRule mdmCodeRule);

    /**
     * 按对象查询规则（同对象唯一校验）
     *
     * @param objectId 对象ID
     * @return 编码规则
     */
    public MdmCodeRule selectRuleByObjectId(Long objectId);

    /**
     * 新增编码规则
     *
     * @param mdmCodeRule 编码规则
     * @return 结果
     */
    public int insertRule(MdmCodeRule mdmCodeRule);

    /**
     * 修改编码规则
     *
     * @param mdmCodeRule 编码规则
     * @return 结果
     */
    public int updateRule(MdmCodeRule mdmCodeRule);

    /**
     * 删除编码规则
     *
     * @param ruleId 规则ID
     * @return 结果
     */
    public int deleteRuleById(Long ruleId);
}
