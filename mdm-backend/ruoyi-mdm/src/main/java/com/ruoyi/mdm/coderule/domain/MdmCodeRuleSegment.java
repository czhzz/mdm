package com.ruoyi.mdm.coderule.domain;

import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 主数据编码规则分段 mdm_code_rule_segment
 *
 * @author ruoyi
 */
public class MdmCodeRuleSegment
{
    private static final long serialVersionUID = 1L;

    /** 分段ID */
    private Long segmentId;

    /** 规则ID */
    private Long ruleId;

    /** 分段类型（CONSTANT常量/DATE日期/SEQUENCE流水/ATTRIBUTE属性值） */
    @NotBlank(message = "分段类型不能为空")
    private String segType;

    /** 分段值（常量值/日期格式/流水位数/属性编码） */
    private String segValue;

    /** 显示顺序 */
    private Integer orderNum;

    public Long getSegmentId()
    {
        return segmentId;
    }

    public void setSegmentId(Long segmentId)
    {
        this.segmentId = segmentId;
    }

    public Long getRuleId()
    {
        return ruleId;
    }

    public void setRuleId(Long ruleId)
    {
        this.ruleId = ruleId;
    }

    public String getSegType()
    {
        return segType;
    }

    public void setSegType(String segType)
    {
        this.segType = segType;
    }

    public String getSegValue()
    {
        return segValue;
    }

    public void setSegValue(String segValue)
    {
        this.segValue = segValue;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("segmentId", getSegmentId())
            .append("ruleId", getRuleId())
            .append("segType", getSegType())
            .append("segValue", getSegValue())
            .append("orderNum", getOrderNum())
            .toString();
    }
}
