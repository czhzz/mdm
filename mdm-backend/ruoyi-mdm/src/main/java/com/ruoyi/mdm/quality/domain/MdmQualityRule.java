package com.ruoyi.mdm.quality.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 数据质量校验规则 mdm_quality_rule
 *
 * @author ruoyi
 */
public class MdmQualityRule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 规则ID */
    private Long ruleId;

    /** 对象ID */
    private Long objectId;

    /** 作用目标（OBJECT对象级/ATTRIBUTE属性级） */
    @Excel(name = "作用目标", readConverterExp = "OBJECT=对象级,ATTRIBUTE=属性级")
    private String targetType;

    /** 目标属性编码（属性级时必填） */
    private String targetValue;

    /** 规则类型（REQUIRED必填/REGEX正则/UNIQUE唯一/RANGE范围） */
    @Excel(name = "规则类型", readConverterExp = "REQUIRED=必填,REGEX=正则,UNIQUE=唯一,RANGE=范围")
    private String ruleType;

    /** 规则名称 */
    @Excel(name = "规则名称")
    private String ruleName;

    /** 规则表达式（正则/范围等） */
    private String ruleExpr;

    /** 违规提示信息 */
    private String ruleMsg;

    /** 状态（0启用 1停用） */
    @Excel(name = "状态", readConverterExp = "0=启用,1=停用")
    private String status;

    public Long getRuleId()
    {
        return ruleId;
    }

    public void setRuleId(Long ruleId)
    {
        this.ruleId = ruleId;
    }

    public Long getObjectId()
    {
        return objectId;
    }

    public void setObjectId(Long objectId)
    {
        this.objectId = objectId;
    }

    @NotBlank(message = "作用目标不能为空")
    public String getTargetType()
    {
        return targetType;
    }

    public void setTargetType(String targetType)
    {
        this.targetType = targetType;
    }

    public String getTargetValue()
    {
        return targetValue;
    }

    public void setTargetValue(String targetValue)
    {
        this.targetValue = targetValue;
    }

    @NotBlank(message = "规则类型不能为空")
    public String getRuleType()
    {
        return ruleType;
    }

    public void setRuleType(String ruleType)
    {
        this.ruleType = ruleType;
    }

    @NotBlank(message = "规则名称不能为空")
    @Size(min = 0, max = 100, message = "规则名称长度不能超过100个字符")
    public String getRuleName()
    {
        return ruleName;
    }

    public void setRuleName(String ruleName)
    {
        this.ruleName = ruleName;
    }

    public String getRuleExpr()
    {
        return ruleExpr;
    }

    public void setRuleExpr(String ruleExpr)
    {
        this.ruleExpr = ruleExpr;
    }

    public String getRuleMsg()
    {
        return ruleMsg;
    }

    public void setRuleMsg(String ruleMsg)
    {
        this.ruleMsg = ruleMsg;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("ruleId", getRuleId())
            .append("objectId", getObjectId())
            .append("targetType", getTargetType())
            .append("targetValue", getTargetValue())
            .append("ruleType", getRuleType())
            .append("ruleName", getRuleName())
            .append("ruleExpr", getRuleExpr())
            .append("ruleMsg", getRuleMsg())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
