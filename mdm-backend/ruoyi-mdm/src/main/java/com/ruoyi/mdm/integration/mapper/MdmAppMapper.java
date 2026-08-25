package com.ruoyi.mdm.integration.mapper;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.ruoyi.mdm.integration.domain.MdmApp;

/**
 * 主数据应用凭证 数据层
 *
 * <p>与 distribution 包同名 mapper 共存，@MapperScan 默认类名 bean 冲突，
 * 用显式 Bean 名区分（旧包删除后此注解可一并移除）
 *
 * @author ruoyi
 */
@Repository("integrationMdmAppMapper")
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
