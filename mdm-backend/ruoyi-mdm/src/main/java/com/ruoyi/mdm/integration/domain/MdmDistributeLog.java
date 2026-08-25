package com.ruoyi.mdm.integration.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 主数据分发日志 mdm_distribute_log（原 mdm_distribution_record）
 *
 * @author ruoyi
 */
public class MdmDistributeLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long recordId;
    private Long appId;
    /** 应用编码（mdm_app.appid 回填，1.2.0 统一筛选维度） */
    private String appCode;
    private String objectCode;
    private Long dataId;
    private String actionType;
    private String endpointUrl;
    private String payload;
    private String status;
    private String errorMsg;
    private String sendTime;
    private String successTime;
    private String confirmTime;
    private Integer retryCount;
    private String remark;
    /** 关联展示字段（非表列） */
    private String appName;

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    public Long getAppId()
    {
        return appId;
    }

    public void setAppId(Long appId)
    {
        this.appId = appId;
    }

    public String getAppCode()
    {
        return appCode;
    }

    public void setAppCode(String appCode)
    {
        this.appCode = appCode;
    }

    public String getObjectCode()
    {
        return objectCode;
    }

    public void setObjectCode(String objectCode)
    {
        this.objectCode = objectCode;
    }

    public Long getDataId()
    {
        return dataId;
    }

    public void setDataId(Long dataId)
    {
        this.dataId = dataId;
    }

    public String getActionType()
    {
        return actionType;
    }

    public void setActionType(String actionType)
    {
        this.actionType = actionType;
    }

    public String getEndpointUrl()
    {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl)
    {
        this.endpointUrl = endpointUrl;
    }

    public String getPayload()
    {
        return payload;
    }

    public void setPayload(String payload)
    {
        this.payload = payload;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    public String getSendTime()
    {
        return sendTime;
    }

    public void setSendTime(String sendTime)
    {
        this.sendTime = sendTime;
    }

    public String getSuccessTime()
    {
        return successTime;
    }

    public void setSuccessTime(String successTime)
    {
        this.successTime = successTime;
    }

    public String getConfirmTime()
    {
        return confirmTime;
    }

    public void setConfirmTime(String confirmTime)
    {
        this.confirmTime = confirmTime;
    }

    public Integer getRetryCount()
    {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount)
    {
        this.retryCount = retryCount;
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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("recordId", getRecordId())
                .append("appId", getAppId())
                .append("objectCode", getObjectCode())
                .append("dataId", getDataId())
                .append("actionType", getActionType())
                .append("status", getStatus())
                .toString();
    }
}
