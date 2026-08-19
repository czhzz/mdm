package com.ruoyi.mdm.coderule.service;

import java.util.List;
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
}
