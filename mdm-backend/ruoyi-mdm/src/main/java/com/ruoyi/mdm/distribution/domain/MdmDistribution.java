package com.ruoyi.mdm.distribution.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 主数据分发配置 mdm_distribution
 *
 * @author ruoyi
 */
public class MdmDistribution extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long distId;
    private Long appId;
    private Long objectId;
    private String triggerType;
    private String channel;
    private String queueName;
    private String endpointUrl;
    private String enabled;
    private String remark;
    /** 关联展示字段（非表列） */
    private String appName;
    private String objectName;

    public Long getDistId()
    {
        return distId;
    }

    public void setDistId(Long distId)
    {
        this.distId = distId;
    }

    public Long getAppId()
    {
        return appId;
    }

    public void setAppId(Long appId)
    {
        this.appId = appId;
    }

    public Long getObjectId()
    {
        return objectId;
    }

    public void setObjectId(Long objectId)
    {
        this.objectId = objectId;
    }

    public String getTriggerType()
    {
        return triggerType;
    }

    public void setTriggerType(String triggerType)
    {
        this.triggerType = triggerType;
    }

    public String getChannel()
    {
        return channel;
    }

    public void setChannel(String channel)
    {
        this.channel = channel;
    }

    public String getQueueName()
    {
        return queueName;
    }

    public void setQueueName(String queueName)
    {
        this.queueName = queueName;
    }

    public String getEndpointUrl()
    {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl)
    {
        this.endpointUrl = endpointUrl;
    }

    public String getEnabled()
    {
        return enabled;
    }

    public void setEnabled(String enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public String getRemark()
    {
        return remark;
    }

    @Override
    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public String getAppName()
    {
        return appName;
    }

    public void setAppName(String appName)
    {
        this.appName = appName;
    }

    public String getObjectName()
    {
        return objectName;
    }

    public void setObjectName(String objectName)
    {
        this.objectName = objectName;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("distId", getDistId())
                .append("appId", getAppId())
                .append("objectId", getObjectId())
                .append("triggerType", getTriggerType())
                .append("channel", getChannel())
                .append("queueName", getQueueName())
                .append("enabled", getEnabled())
                .toString();
    }
}