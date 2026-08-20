package com.ruoyi.mdm.quality.domain;

import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 数据质量台账 mdm_quality_issue
 *
 * @author ruoyi
 */
public class MdmQualityIssue extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 问题ID */
    private Long issueId;

    /** 对象ID */
    private Long objectId;

    /** 数据ID */
    private Long dataId;

    /** 问题类型（VALIDATE校验失败/DUPLICATE重复/MISSING缺失） */
    @Excel(name = "问题类型", readConverterExp = "VALIDATE=校验失败,DUPLICATE=重复,MISSING=缺失")
    private String issueType;

    /** 问题描述 */
    private String issueDesc;

    /** 处理状态（0未处理 1已处理 2忽略） */
    @Excel(name = "处理状态", readConverterExp = "0=未处理,1=已处理,2=忽略")
    private String handleStatus;

    /** 处理人 */
    private String handleBy;

    /** 处理时间 */
    private String handleTime;

    public Long getIssueId()
    {
        return issueId;
    }

    public void setIssueId(Long issueId)
    {
        this.issueId = issueId;
    }

    public Long getObjectId()
    {
        return objectId;
    }

    public void setObjectId(Long objectId)
    {
        this.objectId = objectId;
    }

    public Long getDataId()
    {
        return dataId;
    }

    public void setDataId(Long dataId)
    {
        this.dataId = dataId;
    }

    @NotBlank(message = "问题类型不能为空")
    public String getIssueType()
    {
        return issueType;
    }

    public void setIssueType(String issueType)
    {
        this.issueType = issueType;
    }

    public String getIssueDesc()
    {
        return issueDesc;
    }

    public void setIssueDesc(String issueDesc)
    {
        this.issueDesc = issueDesc;
    }

    public String getHandleStatus()
    {
        return handleStatus;
    }

    public void setHandleStatus(String handleStatus)
    {
        this.handleStatus = handleStatus;
    }

    public String getHandleBy()
    {
        return handleBy;
    }

    public void setHandleBy(String handleBy)
    {
        this.handleBy = handleBy;
    }

    public String getHandleTime()
    {
        return handleTime;
    }

    public void setHandleTime(String handleTime)
    {
        this.handleTime = handleTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("issueId", getIssueId())
            .append("objectId", getObjectId())
            .append("dataId", getDataId())
            .append("issueType", getIssueType())
            .append("issueDesc", getIssueDesc())
            .append("handleStatus", getHandleStatus())
            .append("handleBy", getHandleBy())
            .append("handleTime", getHandleTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
