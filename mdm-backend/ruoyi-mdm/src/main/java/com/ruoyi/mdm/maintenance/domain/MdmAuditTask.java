package com.ruoyi.mdm.maintenance.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 主数据审核任务 mdm_audit_task
 *
 * @author ruoyi
 */
public class MdmAuditTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 对象ID */
    private Long objectId;

    /** 数据ID（新增时为0） */
    private Long dataId;

    /** 操作类型（INSERT新增/UPDATE修改） */
    private String actionType;

    /** 变更前快照(JSON) */
    private String beforeData;

    /** 变更后快照(JSON) */
    private String afterData;

    /** 状态（0待审核 1通过 2驳回） */
    private String status;

    /** 驳回原因 */
    private String rejectReason;

    /** 提交人 */
    private String submitBy;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    private String auditTime;

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
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

    public String getActionType()
    {
        return actionType;
    }

    public void setActionType(String actionType)
    {
        this.actionType = actionType;
    }

    public String getBeforeData()
    {
        return beforeData;
    }

    public void setBeforeData(String beforeData)
    {
        this.beforeData = beforeData;
    }

    public String getAfterData()
    {
        return afterData;
    }

    public void setAfterData(String afterData)
    {
        this.afterData = afterData;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getRejectReason()
    {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason)
    {
        this.rejectReason = rejectReason;
    }

    public String getSubmitBy()
    {
        return submitBy;
    }

    public void setSubmitBy(String submitBy)
    {
        this.submitBy = submitBy;
    }

    public String getAuditBy()
    {
        return auditBy;
    }

    public void setAuditBy(String auditBy)
    {
        this.auditBy = auditBy;
    }

    public String getAuditTime()
    {
        return auditTime;
    }

    public void setAuditTime(String auditTime)
    {
        this.auditTime = auditTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("taskId", getTaskId())
            .append("objectId", getObjectId())
            .append("dataId", getDataId())
            .append("actionType", getActionType())
            .append("status", getStatus())
            .append("rejectReason", getRejectReason())
            .append("submitBy", getSubmitBy())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .toString();
    }
}
