package com.ruoyi.mdm.integration.mapper;

import java.util.List;
import com.ruoyi.mdm.integration.domain.MdmQueryApi;

/**
 * 查询接口配置 数据层（mdm_query_api）
 *
 * @author ruoyi
 */
public interface MdmQueryApiMapper
{
    public MdmQueryApi selectById(Long id);

    public MdmQueryApi selectByApiCode(String apiCode);

    public List<MdmQueryApi> selectList(MdmQueryApi mdmQueryApi);

    public int insert(MdmQueryApi mdmQueryApi);

    public int update(MdmQueryApi mdmQueryApi);

    public int deleteByIds(Long[] ids);
}
