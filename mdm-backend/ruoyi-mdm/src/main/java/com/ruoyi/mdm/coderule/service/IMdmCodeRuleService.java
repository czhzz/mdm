package com.ruoyi.mdm.coderule.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.mdm.coderule.domain.MdmCodeRule;

/**
 * 主数据编码规则 服务层
 *
 * @author ruoyi
 */
public interface IMdmCodeRuleService
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
     * 按对象查询编码规则（一个对象最多一个方案）
     *
     * @param objectId 对象ID
     * @return 编码规则
     */
    public MdmCodeRule selectRuleByObjectId(Long objectId);

    /**
     * 新增编码规则（含分段）
     *
     * @param mdmCodeRule 编码规则
     * @return 结果
     */
    public int insertRule(MdmCodeRule mdmCodeRule);

    /**
     * 修改编码规则（重写分段）
     *
     * @param mdmCodeRule 编码规则
     * @return 结果
     */
    public int updateRule(MdmCodeRule mdmCodeRule);

    /**
     * 删除编码规则（含分段）
     *
     * @param ruleId 规则ID
     * @return 结果
     */
    public int deleteRuleById(Long ruleId);

    /**
     * 校验同对象规则唯一
     *
     * @param mdmCodeRule 编码规则
     * @return 结果（true唯一 false不唯一）
     */
    public boolean checkRuleUnique(MdmCodeRule mdmCodeRule);

    /**
     * 预览示例编码
     *
     * @param mdmCodeRule 编码规则（含分段）
     * @return 示例编码
     */
    public String previewCode(MdmCodeRule mdmCodeRule);

    /**
     * 生成编码（流水段基于 Redis INCR，按周期重置）
     *
     * @param mdmCodeRule 编码规则（含分段）
     * @param data 数据（ATTRIBUTE 段取值）
     * @return 生成的编码
     */
    public String generateCode(MdmCodeRule mdmCodeRule, Map<String, Object> data);
}
