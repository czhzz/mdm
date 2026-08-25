package com.ruoyi.mdm.integration.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.ibatis.type.Alias;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 主数据应用凭证 mdm_app
 *
 * <p>与 distribution 包同名 domain 共存，typeAliasesPackage 注册冲突，
 * 用显式别名区分（旧包删除后此注解可一并移除）
 *
 * @author ruoyi
 */
@Alias("IntegrationMdmApp")
public class MdmApp extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long appId;
    private String appName;
    private String appid;
    private String secret;
    private String enabled;
    private String remark;

    public Long getAppId()
    {
        return appId;
    }

    public void setAppId(Long appId)
    {
        this.appId = appId;
    }

    public String getAppName()
    {
        return appName;
    }

    public void setAppName(String appName)
    {
        this.appName = appName;
    }

    public String getAppid()
    {
        return appid;
    }

    public void setAppid(String appid)
    {
        this.appid = appid;
    }

    public String getSecret()
    {
        return secret;
    }

    public void setSecret(String secret)
    {
        this.secret = secret;
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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("appId", getAppId())
                .append("appName", getAppName())
                .append("appid", getAppid())
                .append("enabled", getEnabled())
                .toString();
    }
}
