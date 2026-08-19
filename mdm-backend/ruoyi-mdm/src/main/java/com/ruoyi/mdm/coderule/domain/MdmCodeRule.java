package com.ruoyi.mdm.coderule.domain;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 主数据编码规则 mdm_code_rule
 *
 * @author ruoyi
 */
public class MdmCodeRule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 规则ID */
    private Long ruleId;

    /** 对象ID */
    private Long objectId;

    /** 规则名称 */
    @Excel(name = "规则名称")
    private String ruleName;

    /** 流水重置周期（NONE/DAY/MONTH/YEAR） */
    @Excel(name = "流水重置周期", readConverterExp = "NONE=不重置,DAY=按日,MONTH=按月,YEAR=按年")
    private String resetType;

    /** 状态（0正常 1停用） */
    private String status;

    /** 编码分段列表 */
    private List<MdmCodeRuleSegment> segments;

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

    public String getResetType()
    {
        return resetType;
    }

    public void setResetType(String resetType)
    {
        this.resetType = resetType;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public List<MdmCodeRuleSegment> getSegments()
    {
        return segments;
    }

    public void setSegments(List<MdmCodeRuleSegment> segments)
    {
        this.segments = segments;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("ruleId", getRuleId())
            .append("objectId", getObjectId())
            .append("ruleName", getRuleName())
            .append("resetType", getResetType())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
