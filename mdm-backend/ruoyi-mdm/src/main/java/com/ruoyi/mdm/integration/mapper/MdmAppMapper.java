package com.ruoyi.mdm.integration.mapper;

import java.util.List;
import com.ruoyi.mdm.integration.domain.MdmApp;

/**
 * 主数据应用凭证 数据层
 *
 * @author ruoyi
 */
public interface MdmAppMapper
{
    public MdmApp selectAppById(Long appId);

    public MdmApp selectAppByAppid(String appid);

    public List<MdmApp> selectAppList(MdmApp mdmApp);

    public int insertApp(MdmApp mdmApp);

    public int updateApp(MdmApp mdmApp);

    public int updateSecret(MdmApp mdmApp);

    public int deleteAppByIds(Long[] appIds);
}
