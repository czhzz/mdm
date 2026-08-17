package com.ruoyi.mdm.model.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 主数据对象 mdm_object
 *
 * @author ruoyi
 */
public class MdmObject extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 对象ID */
    private Long objectId;

    /** 对象编码 */
    @Excel(name = "对象编码")
    private String objectCode;

    /** 对象名称 */
    @Excel(name = "对象名称")
    private String objectName;

    /** 所属分类ID */
    private Long categoryId;

    /** 状态（0未发布 1已发布 2停用） */
    @Excel(name = "状态", readConverterExp = "0=未发布,1=已发布,2=停用")
    private String status;

    /** 模型版本号 */
    @Excel(name = "版本号")
    private String version;

    /** 显示顺序 */
    @Excel(name = "显示顺序")
    private Integer orderNum;

    public Long getObjectId()
    {
        return objectId;
    }

    public void setObjectId(Long objectId)
    {
        this.objectId = objectId;
    }

    @NotBlank(message = "对象编码不能为空")
    @Size(min = 0, max = 50, message = "对象编码长度不能超过50个字符")
    public String getObjectCode()
    {
        return objectCode;
    }

    public void setObjectCode(String objectCode)
    {
        this.objectCode = objectCode;
    }

    @NotBlank(message = "对象名称不能为空")
    @Size(min = 0, max = 100, message = "对象名称长度不能超过100个字符")
    public String getObjectName()
    {
        return objectName;
    }

    public void setObjectName(String objectName)
    {
        this.objectName = objectName;
    }

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
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
            .append("objectId", getObjectId())
            .append("objectCode", getObjectCode())
            .append("objectName", getObjectName())
            .append("categoryId", getCategoryId())
            .append("status", getStatus())
            .append("version", getVersion())
            .append("orderNum", getOrderNum())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
