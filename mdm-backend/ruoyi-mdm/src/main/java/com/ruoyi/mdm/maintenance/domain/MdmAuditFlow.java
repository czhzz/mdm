package com.ruoyi.mdm.maintenance.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 主数据审核流程配置 mdm_audit_flow
 *
 * @author ruoyi
 */
public class MdmAuditFlow extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 流程ID */
    private Long flowId;

    /** 对象ID */
    private Long objectId;

    /** 是否启用（0禁用 1启用） */
    @Excel(name = "是否启用", readConverterExp = "0=禁用,1=启用")
    private String enabled;

    /** 审核角色标识 */
    @Excel(name = "审核角色")
    private String auditRole;

    public Long getFlowId()
    {
        return flowId;
    }

    public void setFlowId(Long flowId)
    {
        this.flowId = flowId;
    }

    public Long getObjectId()
    {
        return objectId;
    }

    public void setObjectId(Long objectId)
    {
        this.objectId = objectId;
    }

    public String getEnabled()
    {
        return enabled;
    }

    public void setEnabled(String enabled)
    {
        this.enabled = enabled;
    }

    public String getAuditRole()
    {
        return auditRole;
    }

    public void setAuditRole(String auditRole)
    {
        this.auditRole = auditRole;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("flowId", getFlowId())
            .append("objectId", getObjectId())
            .append("enabled", getEnabled())
            .append("auditRole", getAuditRole())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
