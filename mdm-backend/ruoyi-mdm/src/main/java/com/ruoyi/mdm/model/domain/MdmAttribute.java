package com.ruoyi.mdm.model.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 主数据属性 mdm_attribute
 *
 * @author ruoyi
 */
public class MdmAttribute extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 属性ID */
    private Long attrId;

    /** 所属对象ID */
    private Long objectId;

    /** 属性编码 */
    @Excel(name = "属性编码")
    private String attrCode;

    /** 属性名称 */
    @Excel(name = "属性名称")
    private String attrName;

    /** 数据类型（text/number/date/datetime/dict/enum/boolean） */
    @Excel(name = "数据类型", readConverterExp = "text=文本,number=数字,date=日期,datetime=日期时间,dict=字典,enum=枚举,boolean=布尔")
    private String dataType;

    /** 是否必填（Y是 N否） */
    private String requiredFlag;

    /** 是否唯一（Y是 N否） */
    private String uniqueFlag;

    /** 是否主属性（Y是 N否） */
    private String primaryFlag;

    /** 数据源类型（input/dict/enum/range） */
    private String sourceType;

    /** 关联字典类型 */
    private String dictType;

    /** 最小值 */
    private String minValue;

    /** 最大值 */
    private String maxValue;

    /** 枚举值（逗号分隔） */
    private String enumValues;

    /** 默认值 */
    private String defaultValue;

    /** 显示顺序 */
    private Integer orderNum;

    /** 状态（0正常 1停用） */
    private String status;

    public Long getAttrId()
    {
        return attrId;
    }

    public void setAttrId(Long attrId)
    {
        this.attrId = attrId;
    }

    public Long getObjectId()
    {
        return objectId;
    }

    public void setObjectId(Long objectId)
    {
        this.objectId = objectId;
    }

    @NotBlank(message = "属性编码不能为空")
    @Size(min = 0, max = 50, message = "属性编码长度不能超过50个字符")
    public String getAttrCode()
    {
        return attrCode;
    }

    public void setAttrCode(String attrCode)
    {
        this.attrCode = attrCode;
    }

    @NotBlank(message = "属性名称不能为空")
    @Size(min = 0, max = 100, message = "属性名称长度不能超过100个字符")
    public String getAttrName()
    {
        return attrName;
    }

    public void setAttrName(String attrName)
    {
        this.attrName = attrName;
    }

    public String getDataType()
    {
        return dataType;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    public String getRequiredFlag()
    {
        return requiredFlag;
    }

    public void setRequiredFlag(String requiredFlag)
    {
        this.requiredFlag = requiredFlag;
    }

    public String getUniqueFlag()
    {
        return uniqueFlag;
    }

    public void setUniqueFlag(String uniqueFlag)
    {
        this.uniqueFlag = uniqueFlag;
    }

    public String getPrimaryFlag()
    {
        return primaryFlag;
    }

    public void setPrimaryFlag(String primaryFlag)
    {
        this.primaryFlag = primaryFlag;
    }

    public String getSourceType()
    {
        return sourceType;
    }

    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
    }

    public String getDictType()
    {
        return dictType;
    }

    public void setDictType(String dictType)
    {
        this.dictType = dictType;
    }

    public String getMinValue()
    {
        return minValue;
    }

    public void setMinValue(String minValue)
    {
        this.minValue = minValue;
    }

    public String getMaxValue()
    {
        return maxValue;
    }

    public void setMaxValue(String maxValue)
    {
        this.maxValue = maxValue;
    }

    public String getEnumValues()
    {
        return enumValues;
    }

    public void setEnumValues(String enumValues)
    {
        this.enumValues = enumValues;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
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
            .append("attrId", getAttrId())
            .append("objectId", getObjectId())
            .append("attrCode", getAttrCode())
            .append("attrName", getAttrName())
            .append("dataType", getDataType())
            .append("requiredFlag", getRequiredFlag())
            .append("uniqueFlag", getUniqueFlag())
            .append("primaryFlag", getPrimaryFlag())
            .append("sourceType", getSourceType())
            .append("dictType", getDictType())
            .append("minValue", getMinValue())
            .append("maxValue", getMaxValue())
            .append("enumValues", getEnumValues())
            .append("defaultValue", getDefaultValue())
            .append("orderNum", getOrderNum())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
