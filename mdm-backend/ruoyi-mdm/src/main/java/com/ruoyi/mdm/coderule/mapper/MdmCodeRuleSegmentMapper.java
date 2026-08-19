package com.ruoyi.mdm.coderule.mapper;

import java.util.List;
import com.ruoyi.mdm.coderule.domain.MdmCodeRuleSegment;

/**
 * 主数据编码规则分段 数据层
 *
 * @author ruoyi
 */
public interface MdmCodeRuleSegmentMapper
{
    /**
     * 查询规则分段列表
     *
     * @param ruleId 规则ID
     * @return 分段集合
     */
    public List<MdmCodeRuleSegment> selectSegmentsByRuleId(Long ruleId);

    /**
     * 新增分段
     *
     * @param segment 分段
     * @return 结果
     */
    public int insertSegment(MdmCodeRuleSegment segment);

    /**
     * 删除规则下所有分段
     *
     * @param ruleId 规则ID
     * @return 结果
     */
    public int deleteSegmentByRuleId(Long ruleId);
}
