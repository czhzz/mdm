package com.ruoyi.mdm.relation.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 主数据对象关系定义 mdm_relation
 *
 * @author ruoyi
 */
public class MdmRelation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String sourceObjectCode;
    private String targetObjectCode;
    private String relationType;
    private String sourceFieldCode;
    private String cascadeRule;
    private String isBidirectional;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getSourceObjectCode()
    {
        return sourceObjectCode;
    }

    public void setSourceObjectCode(String sourceObjectCode)
    {
        this.sourceObjectCode = sourceObjectCode;
    }

    public String getTargetObjectCode()
    {
        return targetObjectCode;
    }

    public void setTargetObjectCode(String targetObjectCode)
    {
        this.targetObjectCode = targetObjectCode;
    }

    public String getRelationType()
    {
        return relationType;
    }

    public void setRelationType(String relationType)
    {
        this.relationType = relationType;
    }

    public String getSourceFieldCode()
    {
        return sourceFieldCode;
    }

    public void setSourceFieldCode(String sourceFieldCode)
    {
        this.sourceFieldCode = sourceFieldCode;
    }

    public String getCascadeRule()
    {
        return cascadeRule;
    }

    public void setCascadeRule(String cascadeRule)
    {
        this.cascadeRule = cascadeRule;
    }

    public String getIsBidirectional()
    {
        return isBidirectional;
    }

    public void setIsBidirectional(String isBidirectional)
    {
        this.isBidirectional = isBidirectional;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("sourceObjectCode", getSourceObjectCode())
            .append("targetObjectCode", getTargetObjectCode())
            .append("relationType", getRelationType())
            .append("sourceFieldCode", getSourceFieldCode())
            .append("cascadeRule", getCascadeRule())
            .append("isBidirectional", getIsBidirectional())
            .toString();
    }
}